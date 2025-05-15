package core.roles.imposter;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import core.Participant;
import core.Plugin;
import core.basic.IPlayerInteractions;
import core.items.SpecialItem;
import core.roles.GameRole;
import core.roles.RoleGroup;

import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class Ghost extends GameRole implements IPlayerInteractions {
    public static final String ROLE_NAME = "Ghost";
    public static final int INVISIBILITY_COOLDOWN = 30;
    public static final int KILL_COOLDOWN = 30;
    public static final int EFFECT_DURATION = 10;
    public static final int POTION_SLOT = 1;
    public static final int SWORD_SLOT = 0;
    
    public final Plugin plugin;
    public final GhostPotion ghostPotion;
    public final GhostSword ghostSword;

    public Ghost(Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ghostPotion = new GhostPotion(plugin, POTION_SLOT, "ghost_potion");
        this.ghostSword = new GhostSword(plugin, SWORD_SLOT, "ghost_sword");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        ghostSword.giveItem(player);
        ghostPotion.giveItem(player);
    }
    
    @Override
    public void remove(Participant participant) {
        Player player = participant.getPlayer();
        participant.clearCommonItems();
        ghostSword.removeItem(player);
        ghostPotion.removeItem(player);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!(event.getEntity() instanceof Player))
            return;
        Player target = (Player) event.getEntity();

        if (!isPlayerValid(target)) return;

        if (!ghostSword.isThisItem(item))
            return;
        if (ghostSword.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + ghostSword.getRemainingCooldown() + " seconds left");
            return;
        }

        ghostSword.setCooldown(player, KILL_COOLDOWN);
        killPlayer(target);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (ghostSword.isThisItem(item) && ghostSword.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + ghostSword.getRemainingCooldown() + " seconds left");
            return;
        }

        if (!ghostPotion.isThisItem(item))
            return;
        if (ghostPotion.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + ghostPotion.getRemainingCooldown() + " seconds left");
            return;
        }
        
        ghostPotion.setCooldown(player, INVISIBILITY_COOLDOWN);    
        activateInvisibility(player);
    }

    private void activateInvisibility(Player player) {
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.INVISIBILITY,
            EFFECT_DURATION * 20,
            0,
            false,
            false
        ));

        player.addPotionEffect(new PotionEffect(
            PotionEffectType.SPEED,
            EFFECT_DURATION * 20,
            2,
            false,
            false
        ));
        
        sendActionBar(player, "&cYou have turned invisible for " + EFFECT_DURATION + " seconds!");
        scheduleActionBar(player, "&cYour invisibility wore off!", EFFECT_DURATION);
    }
}

class GhostSword extends SpecialItem {
    public GhostSword(Plugin plugin, int cooldown, String tagName) {
        super(plugin, cooldown, tagName);
    }

    @Override
    public Material getActiveMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public Material getCooldownMaterial() {
        return Material.BOOK;
    }

    @Override
    public ItemMeta getActiveItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Sharp Sword");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Hit a player to kill him instatly!",
            ChatColor.GRAY + "Cooldown is " + Ghost.KILL_COOLDOWN + "s"
        ));
        
        return meta;
    }

    @Override
    public ItemMeta getCooldownItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Sword Tome");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Your ability is on cooldown!",
            ChatColor.GRAY + "Left-click to show the remaining time"
        ));
        
        return meta;
    }
}

class GhostPotion extends SpecialItem {
    public GhostPotion(Plugin plugin, int cooldown, String tagName) {
        super(plugin, cooldown, tagName);
    }

    @Override
    public Material getActiveMaterial() {
        return Material.DRAGON_BREATH;
    }

    @Override
    public Material getCooldownMaterial() {
        return Material.BOOK;
    }

    @Override
    public ItemMeta getActiveItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Invisibility Potion");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click to turn invisible for " + Ghost.EFFECT_DURATION + "s",
            ChatColor.GRAY + "Cooldown is " + Ghost.INVISIBILITY_COOLDOWN + "s"
        ));
        
        return meta;
    }

    @Override
    public ItemMeta getCooldownItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Invisibility Tome");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Your ability is on cooldown!",
            ChatColor.GRAY + "Right-click to show the remaining time"
        ));
        
        return meta;
    }
}