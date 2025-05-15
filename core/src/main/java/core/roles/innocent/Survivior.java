package core.roles.innocent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import core.Participant;
import core.Plugin;
import core.basic.IPlayerInteractions;
import core.roles.GameRole;
import core.roles.RoleGroup;

public class Survivior extends GameRole implements IPlayerInteractions{
    public static final String ROLE_NAME = "Survivior";
    
    public final Plugin plugin;

    public Survivior(Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getRoleName() {
        return ROLE_NAME;
    }

    @Override
    public RoleGroup getRoleGroup() {
        return RoleGroup.INNOCENT;
    }

     @Override
    public boolean isRoleUnique() {
        return false;
    }

    @Override
    public void assign(Participant participant) {
        Player player = participant.getPlayer();
        player.getInventory().addItem(new ItemStack(Material.PORKCHOP, 10));
    }
    
    @Override
    public void remove(Participant participant) {
        participant.clearCommonItems();
    }
}
