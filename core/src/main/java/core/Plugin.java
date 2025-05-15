package core;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import core.menus.VotingMenu;
import core.roles.RolesManager;

public class Plugin extends JavaPlugin {
    public static Plugin instance;
    private GameSession gameSession;
    private RolesManager rolesManager;
    private ParticipantManager participantManager;

    public static Plugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        this.rolesManager = new RolesManager(this);
        this.participantManager = new ParticipantManager(this);
        this.gameSession = new GameSession(this, rolesManager, participantManager);

        PluginCommand votingMenu = getCommand("voting-menu");
        votingMenu.setExecutor(new VotingMenu(this));

        PluginCommand game = getCommand("game");
        game.setExecutor(gameSession);

        getLogger().info("MMR core plugin is enabled!");
    }

    @Override
    public void onDisable() {
        if (gameSession.isGameRunning()) {
            gameSession.stopGame();
        }

        getLogger().info("MMR core plugin is disabled!");
    }

    public GameSession getGameSession() {
        return gameSession;
    }
}
