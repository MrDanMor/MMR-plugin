package core.roles;

import org.bukkit.entity.Player;

import core.Participant;
import core.Plugin;
import core.roles.imposter.*;
import core.roles.innocent.*;
import core.roles.neutral.*;

import java.util.*;

public class RolesManager {
    private final Map<Participant, GameRole> rolesInGame = new HashMap<>();
    private final Map<String, GameRole> roleRegistry = new HashMap<>();

    private List<String> imposterRegistry = new ArrayList<>();
    private List<String> innocentRegistry = new ArrayList<>();
    private List<String> neutralRegistry = new ArrayList<>();

    private final Plugin plugin;

    public RolesManager(Plugin plugin) {
        this.plugin = plugin;

        registerRole(new Bomber(this.plugin));
        registerRole(new Ghost(this.plugin));
        registerRole(new Detective(this.plugin));
        registerRole(new Survivior(this.plugin));
        registerRole(new Devourer(this.plugin));
    }

    public Map<Participant, GameRole> getRolesInGame() {
        return rolesInGame;
    }

    public void registerRole(GameRole role) {
        String roleId = role.getRoleName();
        roleRegistry.put(roleId, role);
    }

    public void assignRole(Participant participant, String roleId) {
        Player player = participant.getPlayer();
        GameRole role = roleRegistry.get(roleId);
        if (role != null) {
            rolesInGame.put(participant, role);
        }
        player.getInventory().setHeldItemSlot(4);
    }

    public void removeRole(Participant participant) {
        GameRole role = rolesInGame.get(participant);
        if (role != null) {
            role.remove(participant);
        }
        rolesInGame.remove(participant);
    }

    public void clearRoles() {
        rolesInGame.forEach((participant, role) -> role.remove(participant));
        rolesInGame.clear();
    }

    public GameRole getPlayerRole(Participant participant) {
        return rolesInGame.get(participant);
    }

    public void assignAllRoles(List<Player> players, int imposterAmount, int neutralAmount) {
        validateRoleDistribution(players.size(), imposterAmount, neutralAmount);
        sortRolesRegistry();

        Collections.shuffle(players);

        int impostersAssigned = 0;
        int neutralsAssigned = 0;
        List<String> currentRoleList;

        for (Player player : players) {
            if (impostersAssigned < imposterAmount && !imposterRegistry.isEmpty()) {
                currentRoleList = imposterRegistry;
                impostersAssigned++;
            } else if (neutralsAssigned < neutralAmount && !neutralRegistry.isEmpty()) {
                currentRoleList = neutralRegistry;
                neutralsAssigned++;
            } else {
                currentRoleList = innocentRegistry;
            }

            if (currentRoleList.isEmpty()) {
                plugin.getLogger().warning("At some point the role list became empty!");
                return;
            }

            Random random = new Random(System.currentTimeMillis());
            int i = random.nextInt() % currentRoleList.size();

            String roleId = currentRoleList.get(i);
            GameRole role = roleRegistry.get(roleId);

            Participant p = plugin.getGameSession().getParticipantManager().createParticipant(player, role);
            assignRole(p, roleId);
            role.assign(p);

            if (role.isRoleUnique()) currentRoleList.remove(i);
        }
    }

    private boolean validateRoleDistribution(int totalPlayers, int imposters, int neutrals) {
        return imposters + neutrals <= totalPlayers 
            && imposters <= imposterRegistry.size()
            && neutrals <= neutralRegistry.size();
    }

    public void sortRolesRegistry() {
        imposterRegistry.clear();
        innocentRegistry.clear();
        neutralRegistry.clear();

        for (Map.Entry<String, GameRole> entry : roleRegistry.entrySet()) {
            String roleId = entry.getKey();
            RoleGroup group = entry.getValue().getRoleGroup();

            switch (group) {
                case IMPOSTER:
                    imposterRegistry.add(roleId);
                    break;
                case INNOCENT:
                    innocentRegistry.add(roleId);
                    break;
                case NEUTRAL:
                    neutralRegistry.add(roleId);
                    break;
                default:
                    plugin.getLogger().warning("Unknown role group: " + group + " for role: " + roleId);
            }
        }
    }

    public boolean hasRole(Participant participant, String roleId) {
        GameRole role = rolesInGame.get(participant);
        return role != null && role.getRoleName().equalsIgnoreCase(roleId);
    }
}