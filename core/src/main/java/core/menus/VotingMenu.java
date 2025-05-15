package core.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VotingMenu implements CommandExecutor, Listener {
    private static final String MENU_TITLE = "Voting Menu";
    private Inventory votingMenu;
    private final Map<UUID, Inventory> playerMenus = new HashMap<>();
    private static final int SINGLE_CHEST_SIZE = 27;
    private static final int DOUBLE_CHEST_SIZE = 54;
    private static final int MAX_PLAYERS = 36;

    private static final int SKIP_SLOT = 18;
    private static final int CLOSE_SLOT = 26;
    private static final int SPECIAL_SLOT = 22;
    private static final int SKIP_SLOT_DOUBLE = 45;
    private static final int CLOSE_SLOT_DOUBLE = 53;
    private static final int SPECIAL_SLOT_DOUBLE = 49;

    public VotingMenu(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /voting-menu <create|delete|open> [player]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (!sender.hasPermission("votingmenu.create")) {
                    sender.sendMessage("You don't have permission!");
                    return true;
                }
                if (!createVotingMenu()) {
                    sender.sendMessage(ChatColor.RED + "Too many players online (max 36)!");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Voting menu created!");
                }
                return true;

            case "delete":
                if (!sender.hasPermission("votingmenu.delete")) {
                    sender.sendMessage("You don't have permission!");
                    return true;
                }
                deleteVotingMenu();
                sender.sendMessage(ChatColor.GREEN + "Voting menu deleted!");
                return true;

            case "open":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /voting-menu open <player|@s>");
                    return true;
                }
                Player target = resolveTarget(sender, args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                openVotingMenu(target);
                return true;

            case "update":
            if (!sender.hasPermission("votingmenu.update")) {
                sender.sendMessage("You don't have permission!");
                return true;
            }
                updateDeadPlayers(votingMenu);
                sender.sendMessage(ChatColor.GREEN + "Voting menu updated!");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand!");
                return false;                
        }
    }

    private Player resolveTarget(CommandSender sender, String target) {
        if (target.equalsIgnoreCase("@s") && sender instanceof Player) {
            return (Player) sender;
        }
        return Bukkit.getPlayer(target);
    }

    public boolean createVotingMenu() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.size() > MAX_PLAYERS) {
            return false;
        }

        onlinePlayers.sort(Comparator.comparing(Player::getName));

        // Determine inventory size
        int size = onlinePlayers.size() <= 9 ? SINGLE_CHEST_SIZE : DOUBLE_CHEST_SIZE;
        votingMenu = Bukkit.createInventory(null, size, MENU_TITLE);

        // Add player heads
        for (int i = 0; i < onlinePlayers.size(); i++) {
            if (i >= (size - 9))
                break; // Leave last row for controls
            votingMenu.setItem(i, createPlayerHead(onlinePlayers.get(i)));
        }

        // Add control items
        addControlItems(votingMenu);
        return true;
    }

    private void addControlItems(Inventory menu) {
        int size = menu.getSize();

        // Add skip item (flower pot)
        ItemStack skipItem = new ItemStack(Material.FLOWER_POT);
        ItemMeta skipMeta = skipItem.getItemMeta();
        skipMeta.setDisplayName(ChatColor.YELLOW + "Skip Vote");
        skipMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to skip voting"));
        skipItem.setItemMeta(skipMeta);

        int skipSlot = size == SINGLE_CHEST_SIZE ? SKIP_SLOT : SKIP_SLOT_DOUBLE;
        menu.setItem(skipSlot, skipItem);

        // Add close item (barrier)
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close Menu");
        closeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to close"));
        closeItem.setItemMeta(closeMeta);

        int closeSlot = size == SINGLE_CHEST_SIZE ? CLOSE_SLOT : CLOSE_SLOT_DOUBLE;
        menu.setItem(closeSlot, closeItem);
    }

    public void addSpecialItem(Inventory menu) {
        int size = menu.getSize();

        // Add special item (eye of ender)
        ItemStack specialItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta specialMeta = specialItem.getItemMeta();
        specialMeta.setDisplayName(ChatColor.YELLOW + "Use Ability");
        specialMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to use"));
        specialItem.setItemMeta(specialMeta);

        int specialSlot = size == SINGLE_CHEST_SIZE ? SPECIAL_SLOT : SPECIAL_SLOT_DOUBLE;
        menu.setItem(specialSlot, specialItem);
    }

    public void deleteVotingMenu() {
        votingMenu = null;
        playerMenus.clear();
    }

    public void openVotingMenu(Player player) {
        if (votingMenu == null) {
            player.sendMessage(ChatColor.RED + "Voting menu hasn't been created yet!");
            return;
        }

        // Create personal copy with updated dead players
        Inventory personalMenu = Bukkit.createInventory(null, votingMenu.getSize(), MENU_TITLE);
        personalMenu.setContents(votingMenu.getContents());
        updateDeadPlayers(personalMenu);
        playerMenus.put(player.getUniqueId(), personalMenu);

        if (player.getScoreboardTags().contains("test")) {
            addSpecialItem(personalMenu);
        }

        player.openInventory(personalMenu);
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(player.getName());
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to vote for this player"));
        head.setItemMeta(meta);
        return head;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        if (!MENU_TITLE.equals(event.getView().getTitle()))
            return;

        event.setCancelled(true);

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || clickedInventory != event.getView().getTopInventory())
            return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null)
            return;
        
        int slot = event.getRawSlot();

        // Handle close item
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
        }

        if (player.getScoreboardTags().contains("voted"))
            return;

        // Handle interactable items
        if (slot == SKIP_SLOT) {
            player.performCommand("function game:main/voting/count_vote");
        } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
            player.performCommand("function game:main/voting/give_vote with entity "
            + clickedItem.getItemMeta().getDisplayName());
            player.performCommand("function game:main/voting/count_vote");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (MENU_TITLE.equals(event.getView().getTitle())) {
            event.setCancelled(true);
        }
    }
    
    public void updateDeadPlayers(Inventory menu) {
        for (int i = 0; i < menu.getSize(); i++) {
            ItemStack item = menu.getItem(i);
            if (item == null || item.getType() != Material.PLAYER_HEAD) {
                continue;
            }

            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null || !meta.hasOwner()) {
                continue;
            }

            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            OfflinePlayer skullOwner = meta.getOwningPlayer();
            if (skullOwner == null) {
                continue;
            }

            // Check if player is online and has "dead" tag or is offline
            boolean isDead = !skullOwner.isOnline() ||
                (skullOwner.isOnline() &&
                skullOwner.getPlayer().getScoreboardTags().contains("dead"));

            if (isDead) {
                // Create new redstone dust item with same amount and metadata
                ItemStack bone = new ItemStack(Material.BONE, item.getAmount());
                ItemMeta boneMeta = bone.getItemMeta();

                // Copy display name if exists
                if (skullMeta.hasDisplayName()) {
                    boneMeta.setDisplayName(skullMeta.getDisplayName());
                }

                // Set description
                boneMeta.setLore(Arrays.asList(ChatColor.GRAY + "Press F"));

                // Copy other item flags
                skullMeta.getItemFlags().forEach(boneMeta::addItemFlags);

                bone.setItemMeta(boneMeta);
                menu.setItem(i, bone);
            }
        }
    }
}