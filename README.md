# DiscordWhitelist
**DiscordWhitelist** is a lightweight Paper/Spigot plugin that lets players whitelist themselves by sending their Minecraft username in a configured Discord channel. The plugin connects directly to Discord using JDA and executes `whitelist add <username>` on the server console when a user registers.

**Status:** Buildable in this repository (see `target/discordwhitelist-1.0.0.jar`).

**Key features**
- **Single JAR:** Everything runs inside the plugin; drop the jar in `plugins/` and restart.
- **Direct Discord connection:** Uses JDA (no external helper processes).
- **Discord ID-based deduplication:** Users are identified by their Discord user ID only.
- **YAML storage:** All persistent data is stored in `players.yml` and `config.yml`.

**Compatibility**
- Java 21
- Paper (modern 1.20+ / 1.21 builds) — plugin compiled against a recent Paper API version

---

**Installation**

1. Drop the built jar into your server `plugins/` folder:

	 ```bash
	      discordwhitelist-1.0.0.jar
	 ```

2. Start the server once to generate configuration files.
3. Edit the configuration at `plugins/DiscordWhitelist/config.yml` and set your Discord bot token and channel ID.
4. Restart the server.

---

**Configuration**

The default `config.yml` (created automatically) contains:

```
discord:
	token: "YOUR_BOT_TOKEN"
	channel-id: "123456789012345678"

messages:
	success: |
		✅ You have been successfully whitelisted!
		Please do not send another message in the whitelist channel.

	already-whitelisted: |
		❌ You have already been whitelisted with %ign%.
		Please do not send another message in the whitelist channel.
```

Notes:
- Do not hardcode the token or channel ID in the plugin source — use `config.yml`.
- The plugin creates `players.yml` automatically to persist registrations.

---

**Discord behaviour & usage**

- The plugin listens in exactly one channel (by ID). Messages from other channels are ignored.
- Only normal user messages are processed. Messages from bots, webhooks, or system messages are ignored.
- Treat the entire message content as the Minecraft username (do not modify capitalization or perform Mojang checks). Leading/trailing whitespace is trimmed.
- If a Discord user registers for the first time, the plugin runs the Bukkit command `whitelist add <username>` as the server console. On success it reacts ✅ and sends a DM to the user with the configured success message.
- If the Discord user is already registered (by Discord ID), the plugin reacts ❌ and sends a DM with the configured already-whitelisted message. No public messages are posted.

---

**Data storage: `players.yml`**

Example:

```
123456789012345678:
	ign: RijuHatesHer_

987654321098765432:
	ign: Steve
```

Keys are Discord user IDs and the `ign` value is the Minecraft username provided by the user. The file is saved immediately after each successful registration.

---

**Admin command**

Reload configuration (reloads `config.yml` and reconnects to Discord if required):

```
/discordwhitelist reload
```

Permission: `discordwhitelist.reload`

---

**Logging**

When someone registers the plugin logs a concise block, for example:

```
[DiscordWhitelist]
Discord: Username#1234
Discord ID: 123456789012345678
Minecraft: RijuHatesHer_
Status: SUCCESS
```

For duplicate attempts `Status: ALREADY REGISTERED` is logged.

---

**Development & build**

Build with Maven (requires Java 21):

```bash
mvn package
```

The shaded jar is produced in `target/discordwhitelist-1.0.0.jar`.

---

**Notes & troubleshooting**

- If the configured token is invalid the plugin disables itself and prints a clear error.
- The plugin attempts to handle Discord-related errors gracefully (missing permissions, DM disabled, failed reactions). It will not crash the Minecraft server.
- If you change the bot token or channel ID, use `/discordwhitelist reload` to reconnect.

---

**License**

This project follows the repository's `LICENSE` file.

---

If you'd like, I can also add a short example showing how to create a Discord bot and obtain a token, or add a sample `players.yml` with more comments.
