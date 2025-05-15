package core.roles.innocent;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import core.Participant;
import core.Plugin;
import core.basic.IPlayerInteractions;
import core.events.ParticipantInteractAirEvent;
import core.events.ParticipantInteractParticipantEvent;
import core.items.SpecialItem;
import core.roles.GameRole;
import core.roles.RoleGroup;
import net.md_5.bungee.api.ChatColor;

public class Detective extends GameRole implements IPlayerInteractions {
    public static final String ROLE_NAME = "Detective";
    public static final int CHECK_COOLDOWN = 30;
    public static final int TOME_SLOT = 0;
    
    public final Plugin plugin;
    public final DetectiveTome detectiveTome;

    public Detective(Plugin plugin) {
        super(plugin);
        this.plugin = plugin;
        this.detectiveTome = new DetectiveTome(plugin, TOME_SLOT, "detective_tome");
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
        return true;
    }

    @Override
    public void assign(Participant participant) {
        Player player = participant.getPlayer();
        detectiveTome.giveItem(player);
    }
    
    @Override
    public void remove(Participant participant) {
        participant.clearCommonItems();
        detectiveTome.removeItem(participant.getPlayer());
    }

    @EventHandler
    public void onInteractPlayer(ParticipantInteractParticipantEvent event) {
        Player player = event.getParticipant().getPlayer();

        ItemStack item = event.getActiveItem();
        
        if (!detectiveTome.isThisItem(item)) return;

        if (detectiveTome.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + detectiveTome.getRemainingCooldown() + " seconds left");
            return;
        }

        if (event.getTarget().getFragsList().isEmpty()) {
            sendActionBar(player,
            ChatColor.RED + "This player has KILLED in this round!");
        } else {
            sendActionBar(player,
            ChatColor.GRAY + "This player hasn't kill anyone yet");
        }
    }

    @EventHandler
    public void onInteractAir(ParticipantInteractAirEvent event) {
        Player player = event.getParticipant().getPlayer();
        ItemStack item = event.getActiveItem();

        if (detectiveTome.isThisItem(item) && detectiveTome.isOnCooldown()) {
            sendActionBar(player,
            ChatColor.GRAY + "There are " + detectiveTome.getRemainingCooldown() + " seconds left");
        }
    }
}

class DetectiveTome extends SpecialItem {
    public DetectiveTome(Plugin plugin, int slot, String tagName) {
        super(plugin, slot, tagName);
    }

    @Override
    public Material getActiveMaterial() {
        return Material.BOOK;
    }

    @Override
    public Material getCooldownMaterial() {
        return Material.BOOK;
    }

    @Override
    public ItemMeta getActiveItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Detective Tome");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click a player to see if he has killed in this round!",
            ChatColor.GRAY + "Cooldown is " + Detective.CHECK_COOLDOWN + "s"
        ));
        
        return meta;
    }

    @Override
    public ItemMeta getCooldownItemLore() {
        ItemMeta meta = new ItemStack(getActiveMaterial()).getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Detective Tome");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Your ability is on cooldown!",
            ChatColor.GRAY + "Right-click to show the remaining time"
        ));
        
        return meta;
    }
}