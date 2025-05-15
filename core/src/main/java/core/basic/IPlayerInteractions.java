package core.basic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import core.GameSession;

public interface IPlayerInteractions {
    default boolean isPlayerValid(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team currentTeam = scoreboard.getEntryTeam(player.getName());
        
        String player_team = GameSession.getPlayerTeamName();
        return currentTeam != null && currentTeam.getName().equalsIgnoreCase(player_team);
    }

    default void killPlayer(Player player) {
        player.getPlayer().getInventory().clear();
        player.getPlayer().setHealth(0);

        transferToSpectator(player);
    }

    default void killPlayerByPlayer(Player player, Player target) {
        target.getPlayer().getInventory().clear();
        target.damage(target.getHealth() * 2, player);

        transferToSpectator(target);
    }

    default void transferToSpectator(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String spectator_team = GameSession.getSpectatorTeamName();
        Team spectatorTeam = scoreboard.getTeam(spectator_team);

        if (spectatorTeam == null) {
            spectatorTeam = scoreboard.registerNewTeam(spectator_team);
            spectatorTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            spectatorTeam.setCanSeeFriendlyInvisibles(true);
        }

        spectatorTeam.addEntry(player.getName());
    }
}
