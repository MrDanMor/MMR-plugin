package core.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import core.Plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public abstract class SpecialItem extends GameItem {
    public abstract Material getActiveMaterial();
    public abstract Material getCooldownMaterial();
    public abstract ItemMeta getActiveItemLore();
    public abstract ItemMeta getCooldownItemLore();

    private final NamespacedKey specialTag;
    private final Plugin plugin;
    private final int slot;

    private boolean onCooldown = false;
    private long cooldownEndTime = 0;

    public SpecialItem(Plugin plugin, int slot, String tagName) {
        super(plugin);
        this.plugin = plugin;
        this.slot = slot;
        this.specialTag = new NamespacedKey(plugin, tagName);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void giveItem(Player player) {
        if (!player.isOnline() || player == null)
            return;
        onCooldown = false;
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null && !item.getType().isAir())
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        player.getInventory().setItem(slot, new ItemStack(getActiveMaterial()));

        item = player.getInventory().getItem(slot);
        ItemMeta meta = getActiveItemLore();
        item.setItemMeta(meta);

        setSpecialTag(item);
    }

    public int removeItem(Player player) {
        int amount = 0;
        PlayerInventory inventory = player.getInventory();
    
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (isThisItem(item)) {
                amount += item.getAmount();
                inventory.setItem(i, null);
            }
        }

        player.updateInventory();
        return amount;
    }

    public boolean isOnCooldown() {
        return onCooldown;
    }

    public int getRemainingCooldown() {
        if (!onCooldown) return 0;
        return (int) Math.max(0, (cooldownEndTime - System.currentTimeMillis()) / 1000);
    }

    public boolean setCooldown(Player player, int seconds) {
        if (seconds < 0)
            return false;
        onCooldown = true;
        updateItemState(player);

        cooldownEndTime = System.currentTimeMillis() + (seconds * 1000L);

        new BukkitRunnable() {
            @Override
            public void run() {
                onCooldown = false;
                updateItemState(player);
            }
        }.runTaskLater(plugin, seconds * 20L);

        return true;
    }

    private void updateItemState(Player player) {
        ItemStack item = player.getInventory().getItem(slot);
        if (!hasSpecialTag(item))
            return;
        
        Material newMaterial = onCooldown ? getCooldownMaterial() : getActiveMaterial();
        ItemStack newItem = new ItemStack(newMaterial);
        
        ItemMeta meta = onCooldown ? getCooldownItemLore() : getActiveItemLore();
        newItem.setItemMeta(meta);

        setSpecialTag(newItem);

        player.getInventory().setItem(slot, newItem);
        player.updateInventory();
    }

    protected void setSpecialTag(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(specialTag, PersistentDataType.BYTE, (byte)1);
        meta.getPersistentDataContainer().set(getPluginItemTag(), PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);
    }

    protected boolean hasSpecialTag(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(specialTag, PersistentDataType.BYTE);
    }

    public boolean isThisItem(ItemStack item) {
        if (item == null || item.getType().isAir())
            return false;

        return hasSpecialTag(item);
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (isThisItem(event.getCurrentItem()) || isThisItem(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleItemDrop(PlayerDropItemEvent event) {
        if (isThisItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleHandSwap(PlayerSwapHandItemsEvent event) {
        if (isThisItem(event.getOffHandItem()) || isThisItem(event.getMainHandItem())) {
            event.setCancelled(true);
        }
    }
}