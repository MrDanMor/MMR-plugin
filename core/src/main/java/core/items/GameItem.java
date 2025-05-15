package core.items;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import core.Plugin;

public abstract class GameItem implements Listener {
    private static final String COMMON_TAG_NAME = "MMR_Item";
    private final NamespacedKey COMMON_TAG;

    public GameItem(Plugin plugin) {
        this.COMMON_TAG = new NamespacedKey(plugin, COMMON_TAG_NAME);
    }

    public NamespacedKey getPluginItemTag() {
        return COMMON_TAG;
    }

    public boolean hasPluginItemTag(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(COMMON_TAG, PersistentDataType.BYTE);
    }
}
