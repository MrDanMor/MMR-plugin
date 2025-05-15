package core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import core.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

public class GameSession implements CommandExecutor {
    private static final String PLAYER_TEAM_NAME = "player";
    private static final String SPECTATOR_TEAM_NAME = "spectator";
    private static int imposterPercentage = 25;
    private static int neutralPercentage = 20;

    private final Plugin plugin;
    private final RolesManager rolesManager;
    private final ParticipantManager pManager;
    private boolean isGameRunning = false;

    public GameSession(Plugin plugin, RolesManager rolesManager, ParticipantManager pManager) {
        this.plugin = plugin;
        this.rolesManager = rolesManager;
        this.pManager = pManager;
        this.plugin.getCommand("game").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        if (!(sender instanceof Player) || !sender.hasPermission("admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to manage the game!");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                startGame();
                break;
            case "stop":
                stopGame();
                break;
            case "status":
                sender.sendMessage(getGameStatus());
                break;
            default:
                sendUsage(sender);
                break;
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage: /game <start|stop|status>");
    }

    public void startGame() {
        if (isGameRunning) {
            Bukkit.broadcastMessage(ChatColor.RED + "Game is already running!");
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team playerTeam = scoreboard.getTeam(PLAYER_TEAM_NAME);

        if (playerTeam == null || playerTeam.getEntries().isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "No players in team '" + PLAYER_TEAM_NAME + "'!");
            return;
        }

        List<Player> players = getOnlineTeamPlayers(playerTeam);
        if (players.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "No online players in team!");
            return;
        }

        rolesManager.clearRoles();
        assignRoles(players);
        isGameRunning = true;

        Bukkit.broadcastMessage(ChatColor.GOLD + "Game started with " + players.size() + " players!");
    }

    public void stopGame() {
        if (!isGameRunning) {
            Bukkit.broadcastMessage(ChatColor.RED + "Game was already stopped!");
            return;
        }

        rolesManager.clearRoles();
        isGameRunning = false;
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Game has been stopped!");
    }

    private List<Player> getOnlineTeamPlayers(Team team) {
        List<Player> players = new ArrayList<>();
        for (String entry : team.getEntries()) {
            Player player = Bukkit.getPlayer(entry);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    private void assignRoles(List<Player> players) {
        int imposterAmount = Math.round(players.size() * imposterPercentage/100);
        int neutralAmount = Math.round(players.size() * neutralPercentage/100);
        try {
            rolesManager.assignAllRoles(players, imposterAmount, neutralAmount);
        } catch (Exception e) {
            stopGame();
            Bukkit.broadcastMessage(ChatColor.GOLD + "Game was stopped due to error in roles assignment!");
        }
        
    }

    public String getGameStatus() {
        return ChatColor.YELLOW + "Game status: " + 
               (isGameRunning ? ChatColor.GREEN + "RUNNING" : ChatColor.RED + "STOPPED");
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public ParticipantManager getParticipantManager() {
        return pManager;
    }

    public static String getPlayerTeamName() {
        return PLAYER_TEAM_NAME;
    }

    public static String getSpectatorTeamName() {
        return SPECTATOR_TEAM_NAME;
    }
}