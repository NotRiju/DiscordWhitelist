package org.notriju.discordwhitelist;

import org.notriju.discordwhitelist.commands.ReloadCommand;
import org.notriju.discordwhitelist.discord.DiscordManager;
import org.notriju.discordwhitelist.storage.ConfigManager;
import org.notriju.discordwhitelist.storage.PlayerStorage;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiscordWhitelistPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private PlayerStorage playerStorage;
    private DiscordManager discordManager;
    private ReloadCommand reloadCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.load();

        playerStorage = new PlayerStorage(this);
        playerStorage.load();

        reloadCommand = new ReloadCommand(this, configManager, discordManager);
        var command = getCommand("discordwhitelist");
        if (command != null) {
            command.setExecutor(reloadCommand);
            command.setTabCompleter(reloadCommand);
        }

        discordManager = new DiscordManager(this, configManager, playerStorage);
        discordManager.initialize();
    }

    @Override
    public void onDisable() {
        if (discordManager != null) {
            discordManager.shutdown();
        }
    }
}
