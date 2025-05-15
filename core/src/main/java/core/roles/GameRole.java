package core.roles;

import org.bukkit.event.Listener;

import core.Participant;
import core.Plugin;
import core.basic.IMessageSender;

public abstract class GameRole implements Listener, IMessageSender {
    public abstract String getRoleName();
    public abstract RoleGroup getRoleGroup();
    public abstract boolean isRoleUnique();
    public abstract void assign(Participant participant);
    public abstract void remove(Participant participant);

    private final Plugin plugin;

    public GameRole(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}