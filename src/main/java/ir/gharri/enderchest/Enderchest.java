package ir.gharri.enderchest;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.EnderChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Enderchest extends JavaPlugin implements Listener {

    private String inventoryTitleRaw;
    private FileConfiguration config;
    private File configFile;
    private int chestSize;
    private String soundName;
    private float soundVolume;
    private float soundPitch;
    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedContents = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "╔══════════════════════════════════════╗");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "║                                      ║");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "║     §bEnderChest §fby §3Benyamin Gharri§b    §f" + ChatColor.AQUA + "║");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "║                                      ║");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "║   §fVersion: §b" + getDescription().getVersion() + "§f                       " + ChatColor.AQUA + "║");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "║   §fStatus: §a  Enabled§f                  " + ChatColor.AQUA + "║");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "║                                      ║");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "╚══════════════════════════════════════╝");

        createConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        loadSavedEnderChests();

        getLogger().info("§aEnderchest Plugin Enabled! §fCreated by §3Benyamin Gharri");
    }

    @Override
    public void onDisable() {
        for (Map.Entry<UUID, Inventory> entry : openInventories.entrySet()) {
            saveEnderChest(entry.getKey(), entry.getValue().getContents());
        }
        openInventories.clear();
        saveAllEnderChests();

        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "╔══════════════════════════════════════╗");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "║   §bEnderChest Plugin §c  Disabled§b       §f" + ChatColor.AQUA + "║");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "╚══════════════════════════════════════╝");
        getLogger().info("§cEnderchest Plugin Disabled!");
    }

    private void createConfig() {
        File pluginFolder = new File("plugins/ender-chest");
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        configFile = new File(pluginFolder, "configvk.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);

                config.set("enderchest-title", "&5&lEnder Chest &7(&f%player%&7)");

                config.set("message.opening", "&aOpening your &f%title%&a...");
                config.set("message.reload", "&aPlugin reloaded successfully!");

                config.set("size", 3);
                config.set("size-comment", "// 1 Size or 5 Maxmin");

                config.set("sound.enabled", true);
                config.set("sound.name", "BLOCK_ENDER_CHEST_OPEN");
                config.set("sound.volume", 1.0);
                config.set("sound.pitch", 1.0);

                config.save(configFile);
                getLogger().info("Config created at plugins/ender-chest/configvk.yml!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        inventoryTitleRaw = config.getString("enderchest-title", "&5&lEnder Chest");

        int sizeRows = config.getInt("size", 3);
        if (sizeRows < 1) sizeRows = 1;
        if (sizeRows > 5) {
            sizeRows = 5;
            getLogger().warning("Size cannot be more than 5! Setting to 5.");
        }
        chestSize = sizeRows * 9;

        soundName = config.getString("sound.name", "BLOCK_ENDER_CHEST_OPEN");
        soundVolume = (float) config.getDouble("sound.volume", 1.0);
        soundPitch = (float) config.getDouble("sound.pitch", 1.0);
    }

    private Component getPlayerTitle(Player player) {
        String playerName = player.getName();
        String titleWithPlayer = inventoryTitleRaw.replace("%player%", playerName);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(
                titleWithPlayer.replace('&', '§')
        );
    }

    private String getMessageWithPlaceholders(String message, Player player, String title) {
        String playerName = player.getName();
        message = message.replace("%player%", playerName);
        message = message.replace("%title%", title);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private void playOpenSound(Player player) {
        if (config.getBoolean("sound.enabled", true)) {
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, soundVolume, soundPitch);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid sound name: " + soundName);
                player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, soundVolume, soundPitch);
            }
        }
    }

    private void loadSavedEnderChests() {
        File dataFolder = new File("plugins/ender-chest/data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            getLogger().info("Created data folder at plugins/ender-chest/data/");
            return;
        }

        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String uuidStr = file.getName().replace(".yml", "");
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    YamlConfiguration data = YamlConfiguration.loadConfiguration(file);

                    ItemStack[] contents = new ItemStack[chestSize];
                    for (int i = 0; i < chestSize; i++) {
                        if (data.contains("slot_" + i)) {
                            contents[i] = data.getItemStack("slot_" + i);
                        }
                    }
                    savedContents.put(uuid, contents);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid UUID file: " + file.getName());
                }
            }
        }
        getLogger().info("Loaded " + savedContents.size() + " ender chests from plugins/ender-chest/data/");
    }

    private void saveEnderChest(UUID uuid, ItemStack[] contents) {
        savedContents.put(uuid, contents.clone());

        File dataFolder = new File("plugins/ender-chest/data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File file = new File(dataFolder, uuid.toString() + ".yml");
        YamlConfiguration data = new YamlConfiguration();

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                data.set("slot_" + i, contents[i]);
            }
        }

        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAllEnderChests() {
        File dataFolder = new File("plugins/ender-chest/data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        for (Map.Entry<UUID, ItemStack[]> entry : savedContents.entrySet()) {
            File file = new File(dataFolder, entry.getKey().toString() + ".yml");
            YamlConfiguration data = new YamlConfiguration();

            ItemStack[] contents = entry.getValue();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    data.set("slot_" + i, contents[i]);
                }
            }

            try {
                data.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ItemStack[] getEnderChestContents(UUID uuid) {
        if (savedContents.containsKey(uuid)) {
            ItemStack[] contents = savedContents.get(uuid);
            if (contents.length != chestSize) {
                ItemStack[] newContents = new ItemStack[chestSize];
                System.arraycopy(contents, 0, newContents, 0, Math.min(contents.length, chestSize));
                savedContents.put(uuid, newContents);
                return newContents;
            }
            return contents;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            ItemStack[] original = player.getEnderChest().getContents();
            ItemStack[] newContents = new ItemStack[chestSize];
            System.arraycopy(original, 0, newContents, 0, Math.min(original.length, chestSize));
            return newContents;
        }

        return new ItemStack[chestSize];
    }

    private void openEnderChest(Player player) {
        UUID uuid = player.getUniqueId();

        playOpenSound(player);

        Component titleComponent = getPlayerTitle(player);
        String coloredTitle = ChatColor.translateAlternateColorCodes('&',
                inventoryTitleRaw.replace("%player%", player.getName()));

        ItemStack[] contents = getEnderChestContents(uuid);
        Inventory customInv = Bukkit.createInventory(null, chestSize, titleComponent);
        customInv.setContents(contents);

        openInventories.put(uuid, customInv);
        player.openInventory(customInv);

        String msg = config.getString("message.opening", "&aOpening your &f%title%&a...");
        msg = getMessageWithPlaceholders(msg, player, coloredTitle);
        player.sendMessage(msg);
    }

    private void saveAndClose(Player player, Inventory inventory) {
        if (inventory != null && openInventories.containsKey(player.getUniqueId())) {
            saveEnderChest(player.getUniqueId(), inventory.getContents());
            openInventories.remove(player.getUniqueId());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            loadConfig();
            savedContents.clear();
            loadSavedEnderChests();

            String msg = config.getString("message.reload", "&aPlugin reloaded successfully!");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        openEnderChest(player);

        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Inventory inv = event.getInventory();

            if (openInventories.containsKey(player.getUniqueId()) &&
                    openInventories.get(player.getUniqueId()).equals(inv)) {
                saveAndClose(player, inv);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null &&
                    event.getClickedBlock().getState() instanceof EnderChest) {

                event.setCancelled(true);
                openEnderChest(player);
            }
        }
    }
}