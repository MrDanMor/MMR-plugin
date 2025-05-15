package core.basic;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import core.Plugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public interface IMessageSender {
    Plugin getPlugin();

    default void sendActionBar(Player player, String message) {
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
        new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
    }

    default void scheduleActionBar(Player player, String message, int seconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    sendActionBar(player, message);
                }
            }
        }.runTaskLater(getPlugin(), seconds * 20L);
    }
}
