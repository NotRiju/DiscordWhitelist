package org.notriju.discordwhitelist.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PlayerStorage {

    private final JavaPlugin plugin;
    private final File storageFile;
    private final Map<String, String> registeredPlayers = new HashMap<>();

    public PlayerStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        if (!storageFile.exists()) {
            save();
            return;
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(storageFile);
        for (String key : configuration.getKeys(false)) {
            if (configuration.isConfigurationSection(key)) {
                String ign = configuration.getString(key + ".ign", "");
                if (!ign.isBlank()) {
                    registeredPlayers.put(key, ign);
                }
            }
        }
    }

    public boolean isRegistered(String discordUserId) {
        return registeredPlayers.containsKey(discordUserId);
    }

    public String getIgn(String discordUserId) {
        return registeredPlayers.get(discordUserId);
    }

    public void register(String discordUserId, String ign) {
        registeredPlayers.put(discordUserId, ign);
        save();
    }

    public void save() {
        FileConfiguration configuration = new YamlConfiguration();
        for (Map.Entry<String, String> entry : registeredPlayers.entrySet()) {
            configuration.set(entry.getKey() + ".ign", entry.getValue());
        }
        try {
            configuration.save(storageFile);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save players.yml", exception);
        }
    }
}
