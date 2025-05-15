package core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;

import core.Participant;
import core.Plugin;
import core.basic.IPlayerInteractions;

public class ParticipantEventListener implements Listener, IPlayerInteractions {
    private final Plugin plugin;

    public ParticipantEventListener(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerValid(player)) return;

        Participant participant = plugin.getGameSession().getParticipantManager().getParticipant(player);
        if (participant == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ParticipantInteractAirEvent interactAirEvent = new ParticipantInteractAirEvent(participant, event.getAction());
            plugin.getServer().getPluginManager().callEvent(interactAirEvent);

            if (interactAirEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player aPlayer = (Player)event.getDamager();
        Player tPlayer = (Player)event.getEntity();
        if (!isPlayerValid(aPlayer)) return;
        if (!isPlayerValid(tPlayer)) return;

        Participant attacker = plugin.getGameSession().getParticipantManager().getParticipant(aPlayer);
        Participant target = plugin.getGameSession().getParticipantManager().getParticipant(tPlayer);
        
        if (attacker != null && target != null) {
            ParticipantInteractParticipantEvent interactEvent = new ParticipantInteractParticipantEvent(attacker, target);
            plugin.getServer().getPluginManager().callEvent(interactEvent);

            if (interactEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (!isPlayerValid(victim)) return;
        if (!isPlayerValid(killer)) return;

        Participant killerParticipant = plugin.getGameSession().getParticipantManager().getParticipant(killer);
        Participant victimParticipant = plugin.getGameSession().getParticipantManager().getParticipant(victim);
        
        if (killerParticipant != null && victimParticipant != null) {
            ParticipantKillParticipantEvent killEvent = new ParticipantKillParticipantEvent(killerParticipant, victimParticipant);
            plugin.getServer().getPluginManager().callEvent(killEvent);
        }
    }
}
