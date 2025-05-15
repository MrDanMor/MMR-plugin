package core.roles.imposter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import core.Participant;
import core.Plugin;
import core.basic.IPlayerInteractions;
import core.items.SpecialItem;
import core.roles.GameRole;
import core.roles.RoleGroup;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.Set;

public class Bomber extends GameRole implements IPlayerInteractions {
    public static final String ROLE_NAME = "Bomber";
    public static final int BOMB_COOLDOWN = 30;
    public static final int REVEAL_COOLDOWN = 20;
    public static final int PASS_COOLDOWN = 3;
    public static final int BOMB_TIMER = 30;
    public static final int TOME_SLOT = 0;
    public static final int BOMB_SLOT = 4;
    
    private final Plugin plugin;
    private final SpecialItem bomberTome;
    private final SpecialItem bombItem;

    private boolean cancelDelayedEvents = false;
    private Set<Player> currentBombHolders;

    public Bomber(Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.bomberTome = new BomberTome(plugin, TOME_SLOT, "bomber_tome");
        this.bombItem = new BombItem(plugin, BOMB_SLOT, "bomb_item");
    }

    @Override
    public String getRoleName() {
        return ROLE_NAME;
    }

    @Override
    public RoleGroup getRoleGroup() {
        return RoleGroup.IMPOSTER;
    }

    @Override
    public boolean isRoleUnique() {
        return true;
    }

    @Override
    public void assign(Participant participant) {
        Player player = participant.getPlayer();
        bomberTome.giveItem(player);
    }
    
    @Override
    public void remove(Participant participant) {
        participant.clearCommonItems();
        bomberTome.removeItem(participant.getPlayer());
        cancelDelayedEvents = true;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            bombItem.removeItem(onlinePlayer);
        }
    }

    public void giveBomb(Player target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (cancelDelayedEvents) return;
                bombItem.giveItem(target);
                currentBombHolders.add(target);
            }
        }.runTaskLater(plugin, REVEAL_COOLDOWN * 20L);

        scheduleBombDetonation();
    }

    public void passBomb(Player player, Player target) {
        bombItem.removeItem(player);
        bombItem.giveItem(target);
        bombItem.setCooldown(target, PASS_COOLDOWN);
        currentBombHolders.remove(player);
        currentBombHolders.add(target);
    }

    public void scheduleBombDetonation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (cancelDelayedEvents) return;
                if (currentBombHolders.isEmpty()) return;
                
                for (Player player : currentBombHolders) {
                    if (!isPlayerValid(player)) return;
                    killPlayer(player);
                }
            }
        }.runTaskLater(plugin, BOMB_TIMER * 20L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;
        Player player = event.getPlayer();

        if (!isPlayerValid(player)) return;

        ItemStack item = player.getInventory().getItem(event.getHand());

        if (bomberTome.isThisItem(item) && bomberTome.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + bomberTome.getRemainingCooldown() + " seconds left");
        }
        if (bombItem.isThisItem(item)) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + bombItem.getRemainingCooldown() + " seconds left");
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Player)) return;

        Player target = (Player) entity;
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!isPlayerValid(target)) return;

        if (bomberTome.isThisItem(item)) handleBomberRightClick(player, target);
        if (bombItem.isThisItem(item)) handlePlayerRightClick(player, target);;
    }

    public void handleBomberRightClick(Player player, Player target) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!bomberTome.isThisItem(item)) return;

        if (bomberTome.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + bomberTome.getRemainingCooldown() + " seconds left");
            return;
        }
        
        bomberTome.setCooldown(player, BOMB_COOLDOWN);
        giveBomb(target);
        sendActionBar(player, "&cYou have given a bomb to a player!");
    }

    
    public void handlePlayerRightClick(Player player, Player target) {
        if (bombItem.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + bombItem.getRemainingCooldown() + " seconds left");
            return;
        }

        passBomb(player, target);
    }
}

class BomberTome extends SpecialItem {
    public BomberTome(Plugin plugin, int cooldown, String tagName) {
        super(plugin, cooldown, tagName);
    }

    @Override
    public Material getActiveMaterial() {
        return Material.BOOK;
    }

    @Override
    public Material getCooldownMaterial() {
        return Material.BOOK;
    }

    @Override
    public ItemMeta getActiveItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Bomb Tome");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click a player to give him a bomb!",
            ChatColor.GRAY + "Cooldown is " + Bomber.BOMB_COOLDOWN + "s"
        ));
        
        return meta;
    }

    @Override
    public ItemMeta getCooldownItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Bomb Tome");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Your ability is on cooldown!",
            ChatColor.GRAY + "Right-click to show the remaining time"
        ));
        
        return meta;
    }
}

class BombItem extends SpecialItem {
    public BombItem(Plugin plugin, int cooldown, String tagName) {
        super(plugin, cooldown, tagName);
    }

    @Override
    public Material getActiveMaterial() {
        return Material.TNT;
    }

    @Override
    public Material getCooldownMaterial() {
        return Material.TNT;
    }

    @Override
    public ItemMeta getActiveItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Bomb");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click a player to give him a bomb!",
            ChatColor.GRAY + "You have " + Bomber.BOMB_TIMER + "s left!"
        ));
        
        return meta;
    }

    @Override
    public ItemMeta getCooldownItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Bomb");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Your ability is on cooldown!",
            ChatColor.GRAY + "Right-click to show the remaining time"
        ));
        
        return meta;
    }
}