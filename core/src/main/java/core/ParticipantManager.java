package core;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;

import core.roles.GameRole;

public class ParticipantManager {
    private final Map<Player, Participant> participants = new WeakHashMap<>();
    private final Plugin plugin;

    public ParticipantManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public Participant createParticipant(Player player, GameRole role) {
        Participant participant = new Participant(plugin, player, role);
        participants.put(player, participant);
        return participant;
    }
    
    public Participant getParticipant(Player player) {
        return participants.get(player);
    }
    
    public void removeParticipant(Player player) {
        participants.remove(player);
    }
    
    public boolean isParticipant(Player player) {
        return participants.containsKey(player);
    }
}
