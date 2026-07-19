package org.notriju.discordwhitelist.commands;

import org.notriju.discordwhitelist.discord.DiscordManager;
import org.notriju.discordwhitelist.storage.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final DiscordManager discordManager;

    public ReloadCommand(JavaPlugin plugin, ConfigManager configManager, DiscordManager discordManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.discordManager = discordManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("discordwhitelist.reload")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            configManager.reload();
            if (discordManager != null) {
                discordManager.reload();
            }
            sender.sendMessage("DiscordWhitelist configuration reloaded.");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return Collections.emptyList();
    }
}
