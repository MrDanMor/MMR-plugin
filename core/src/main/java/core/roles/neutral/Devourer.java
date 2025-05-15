package core.roles.neutral;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Devourer extends GameRole implements IPlayerInteractions {
    public static final String ROLE_NAME = "Devourer";
    public static final int EATING_COOLDOWN = 30;
    public static final int EFFECT_COOLDOWN = 10;
    public static final int EFFECT_DURATION = 10;
    public static final int BOWL_SLOT = 0;
    
    public final Plugin plugin;
    public final DevourerBowl devourerBowl;

    private final Set<Player> swallowedPlayers = new HashSet<>();

    public Devourer(Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.devourerBowl = new DevourerBowl(plugin, BOWL_SLOT, "devourer_bowl");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getRoleName() {
        return ROLE_NAME;
    }

    @Override
    public RoleGroup getRoleGroup() {
        return RoleGroup.NEUTRAL;
    }

    @Override
    public boolean isRoleUnique() {
        return true;
    }

    @Override
    public void assign(Participant participant) {
        Player player = participant.getPlayer();
        devourerBowl.giveItem(player);
    }
    
    @Override
    public void remove(Participant participant) {
        Player player = participant.getPlayer();
        participant.clearCommonItems();
        devourerBowl.removeItem(player);
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (!(entity instanceof Player)) return;

        Player target = (Player) entity;
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!isPlayerValid(target)) return;

        if (!devourerBowl.isThisItem(item)) return;

        if (devourerBowl.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + devourerBowl.getRemainingCooldown() + " seconds left");
            return;
        }
        
        devourerBowl.setCooldown(player, EATING_COOLDOWN);    
        EatPlayer(player, target);
    }

    private void EatPlayer(Player player, Player target) {
        target.setGameMode(GameMode.SPECTATOR);
        target.setSpectatorTarget(player);
        swallowedPlayers.add(target);
        
        target.addPotionEffect(new PotionEffect(
            PotionEffectType.BLINDNESS,
            Integer.MAX_VALUE,
            2,
            false,
            false
        ));
        sendActionBar(player, "&cYou have been eaten! No one can hear you from outside...");
    }

    public void FreeAllPlayers() {
        for (Player target : swallowedPlayers) {
            FreePlayer(target);
        }
        swallowedPlayers.clear();
    }

    private void FreePlayer(Player target) {
        if (swallowedPlayers.contains(target)) {
            target.setGameMode(GameMode.SURVIVAL);
            sendActionBar(target,
            "&cYou have been freed!");
            swallowedPlayers.remove(target);
            target.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (swallowedPlayers.contains(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't teleport while swallowed!");
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (swallowedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }
}

class DevourerBowl extends SpecialItem {
    public DevourerBowl(Plugin plugin, int cooldown, String tagName) {
        super(plugin, cooldown, tagName);
    }

    @Override
    public Material getActiveMaterial() {
        return Material.BOWL;
    }

    @Override
    public Material getCooldownMaterial() {
        return Material.BOOK;
    }

    @Override
    public ItemMeta getActiveItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Bottomless Bowl");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click a player to eat him!",
            ChatColor.GRAY + "Cooldown is " + Devourer.EATING_COOLDOWN + "s"
        ));
        
        return meta;
    }

    @Override
    public ItemMeta getCooldownItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Bowl Tome");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Your ability is on cooldown!",
            ChatColor.GRAY + "Right-click to show the remaining time"
        ));
        
        return meta;
    }
}