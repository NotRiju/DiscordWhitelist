package org.notriju.discordwhitelist.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.contains("discord.token") || config.getString("discord.token", "").isBlank()) {
            config.set("discord.token", "YOUR_BOT_TOKEN");
        }
        if (!config.contains("discord.channel-id") || config.getString("discord.channel-id", "").isBlank()) {
            config.set("discord.channel-id", "123456789012345678");
        }
        if (!config.isConfigurationSection("messages")) {
            config.createSection("messages");
        }
        if (!config.contains("messages.success")) {
            config.set("messages.success", "✅ You have been successfully whitelisted!\nPlease do not send another message in the whitelist channel.");
        }
        if (!config.contains("messages.already-whitelisted")) {
            config.set("messages.already-whitelisted", "❌ You have already been whitelisted with %ign%.\nPlease do not send another message in the whitelist channel.");
        }
        save();
    }

    public void reload() {
        load();
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config.yml", exception);
        }
    }

    public String getDiscordToken() {
        return config.getString("discord.token", "");
    }

    public String getChannelId() {
        return config.getString("discord.channel-id", "");
    }

    public String getSuccessMessage() {
        return config.getString("messages.success", "");
    }

    public String getAlreadyWhitelistedMessage() {
        return config.getString("messages.already-whitelisted", "");
    }
}
