package org.notriju.discordwhitelist.discord;

import org.notriju.discordwhitelist.storage.ConfigManager;
import org.notriju.discordwhitelist.storage.PlayerStorage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DiscordManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final PlayerStorage playerStorage;
    private JDA jda;
    private String lastToken = null;
    private String lastChannelId = null;

    public DiscordManager(JavaPlugin plugin, ConfigManager configManager, PlayerStorage playerStorage) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerStorage = playerStorage;
    }

    public void initialize() {
        connect(true);
    }

    public void reload() {
        connect(false);
    }

    private void connect(boolean initialStart) {
        String token = configManager.getDiscordToken();
        String channelId = configManager.getChannelId();

        if (token == null || token.isBlank() || token.equals("YOUR_BOT_TOKEN")) {
            plugin.getLogger().warning("Discord token is not configured. Disabling DiscordWhitelist.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!initialStart && token.equals(lastToken) && channelId.equals(lastChannelId) && jda != null) {
            return;
        }

        shutdown();
        lastToken = token;
        lastChannelId = channelId;

        plugin.getLogger().info("[DiscordWhitelist] Connecting to Discord...");

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                    .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .addEventListeners(new DiscordListener())
                    .build();

            jda.awaitReady();

            plugin.getLogger().info("[DiscordWhitelist] Connected as " + jda.getSelfUser().getName());
            plugin.getLogger().info("[DiscordWhitelist] Listening in channel: " + channelId);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to Discord. Please verify the bot token.", exception);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdownNow();
            jda = null;
        }
    }

    private class DiscordListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.isWebhookMessage() || event.getAuthor().isBot() || event.getAuthor().isSystem()) {
                return;
            }

            if (!Objects.equals(event.getChannel().getId(), configManager.getChannelId())) {
                return;
            }

            Message message = event.getMessage();
            String rawUsername = message.getContentStripped();
            if (rawUsername == null || rawUsername.isBlank()) {
                return;
            }

            User user = event.getAuthor();
            String discordUserId = user.getId();
            String minecraftUsername = rawUsername.strip();

            if (playerStorage.isRegistered(discordUserId)) {
                message.addReaction(Emoji.fromUnicode("❌")).queue();
                CompletableFuture.runAsync(() -> {
                    try {
                        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(
                                "❌ You have already been whitelisted with:\n"
                                        + playerStorage.getIgn(discordUserId)
                                        + "\n\nPlease do not send another message in the whitelist channel."
                        ).queue());
                    } catch (Exception exception) {
                        plugin.getLogger().log(Level.WARNING, "Failed to send DM to Discord user " + user.getId(), exception);
                    }
                });
                plugin.getLogger().info("[DiscordWhitelist]\nDiscord: " + user.getEffectiveName() + "\nDiscord ID: " + discordUserId + "\nMinecraft: " + minecraftUsername + "\nStatus: ALREADY REGISTERED");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + minecraftUsername);
                    if (success) {
                        playerStorage.register(discordUserId, minecraftUsername);
                        message.addReaction(Emoji.fromUnicode("✅")).queue();
                        CompletableFuture.runAsync(() -> {
                            try {
                                user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(
                                        "✅ You have been successfully whitelisted!\n\nPlease do not send another message in the whitelist channel."
                                ).queue());
                            } catch (Exception exception) {
                                plugin.getLogger().log(Level.WARNING, "Failed to send DM to Discord user " + user.getId(), exception);
                            }
                        });
                        plugin.getLogger().info("[DiscordWhitelist]\nDiscord: " + user.getEffectiveName() + "\nDiscord ID: " + discordUserId + "\nMinecraft: " + minecraftUsername + "\nStatus: SUCCESS");
                    } else {
                        plugin.getLogger().warning("Whitelist command failed for Discord user " + discordUserId + " with username " + minecraftUsername);
                    }
                } catch (Exception exception) {
                    plugin.getLogger().log(Level.WARNING, "Failed to execute whitelist command", exception);
                }
            });
        }
    }
}
