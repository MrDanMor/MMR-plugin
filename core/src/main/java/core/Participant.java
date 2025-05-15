package core;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import core.basic.IMessageSender;
import core.basic.IPlayerInteractions;
import core.items.GameItem;
import core.roles.GameRole;

public class Participant extends GameItem implements IMessageSender, IPlayerInteractions{
    private final Plugin plugin;
    private final Player player;
    private final GameRole role;

    private List<UUID> killedPlayers = Collections.emptyList();

    public Participant(Plugin plugin, Player player, GameRole role) {
        super(plugin);
        this.plugin = plugin;
        this.player = player;
        this.role = role;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    public Player getPlayer() {
        return player;
    }

    public String getRole() {
        return role.getRoleName();
    }

    public List<UUID> getFragsList() {
        return killedPlayers;
    }

    public void clearFragsList() {
        killedPlayers.clear();
    }

    public void clearCommonItems() {
        for (ItemStack item : player.getInventory()) {
            if (hasPluginItemTag(item)) continue;
            player.getInventory().remove(item);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getGameSession().isGameRunning())
            return;
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != player) {
            killPlayer(victim);
            return;
        }

        killedPlayers.add(victim.getUniqueId());
        plugin.getLogger().info(killer.getName() + " killed " + victim.getName());
    }

    @EventHandler
    public void handleQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getGameSession().isGameRunning())
            return;
        if (!isPlayerValid(player))
            return;
        killPlayer(player);
    }

    @EventHandler
    public void handleKick(PlayerKickEvent event) {
        if (!plugin.getGameSession().isGameRunning())
            return;
        Player player = event.getPlayer();
        if (!isPlayerValid(player))
            return;
        killPlayer(player);
    }

    @EventHandler
    public void handleLogin(PlayerLoginEvent event) {
        if (!plugin.getGameSession().isGameRunning())
            return;
        Player player = event.getPlayer();
        if (!isPlayerValid(player)) 
            return;
        sendActionBar(player, "&cThe game has already started, you have to wait...");
        killPlayer(player);
    }
}
