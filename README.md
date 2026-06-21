# EnderChest

A lightweight Bukkit/Spigot/Paper plugin that gives every player a custom, GUI-based Ender Chest — fully configurable title, size, messages, and open sound, with reliable per-player persistence to disk.

---

## ✨ Features

- **Custom GUI Ender Chest** — opens a virtual chest instead of the vanilla one, with a fully configurable title (supports `%player%` placeholder and `&`-color codes via Adventure's legacy serializer).
- **Adjustable size** — set the chest size from 1 to 5 rows (9 to 45 slots) in the config.
- **Per-player persistence** — contents are saved to `plugins/ender-chest/data/<player-uuid>.yml` and restored automatically, independent of the vanilla ender chest.
- **Vanilla ender chest interception** — right-clicking a real ender chest block opens the custom GUI instead of the default one.
- **Configurable open sound** — choose any `Sound` enum value, with volume and pitch control, and a safe fallback if an invalid sound name is set.
- **Configurable messages** — opening message and reload confirmation message, both with placeholder support.
- **Safe data migration** — if you change the chest size in the config, existing saved inventories are automatically resized without data loss (within the new bounds).
- **Reload command** — apply config changes without restarting the server.
- **Graceful shutdown** — all open inventories are saved automatically on plugin disable / server stop.

---

## 📋 Requirements

- A Paper/Spigot (or fork) server, **1.19+** recommended (uses the Adventure API `Component` for inventory titles).
- Java 17+ (depending on your server version).

---

## 📦 Installation

1. Download the latest `EnderChest.jar` from the [Releases](../../releases) page (or build it yourself — see below).
2. Drop the `.jar` into your server's `plugins/` folder.
3. Start (or restart) the server. The plugin will automatically generate its config at:
   ```
   plugins/ender-chest/configvk.yml
   ```
4. Edit the config to your liking, then run the reload command (see below) or restart the server.

---

## 🛠️ Building from Source

```bash
git clone https://github.com/BenyVK/enderchestMC.git
cd enderchestMC
mvn clean package
```

The compiled `.jar` will be in the `target/` directory.

---

## 🎮 Commands

> Exact command name/aliases are defined in `plugin.yml` — adjust the examples below if you've customized them.

| Command | Description |
|---|---|
| `/enderchest` | Opens your personal custom Ender Chest GUI. |
| `/enderchest reload` | Reloads the configuration and re-loads all saved ender chest data from disk. |

You can also simply **right-click a vanilla Ender Chest block** in the world — the plugin intercepts the interaction and opens the custom GUI instead.

---

## ⚙️ Configuration

Default `configvk.yml`:

```yaml
enderchest-title: "&5&lEnder Chest &7(&f%player%&7)"

message:
  opening: "&aOpening your &f%title%&a..."
  reload: "&aPlugin reloaded successfully!"

size: 3
size-comment: "// 1 Size or 5 Maxmin"

sound:
  enabled: true
  name: "BLOCK_ENDER_CHEST_OPEN"
  volume: 1.0
  pitch: 1.0
```

| Key | Description |
|---|---|
| `enderchest-title` | Inventory title. Supports `&` color codes and the `%player%` placeholder. |
| `message.opening` | Message sent to the player when the chest opens. Supports `%player%` and `%title%`. |
| `message.reload` | Message sent to the command sender after `/enderchest reload`. |
| `size` | Number of rows (1–5). Values outside this range are clamped, with a console warning if too high. |
| `sound.enabled` | Whether a sound plays when the chest opens. |
| `sound.name` | Any valid Bukkit [`Sound`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html) enum name. Falls back to `BLOCK_ENDER_CHEST_OPEN` if invalid. |
| `sound.volume` / `sound.pitch` | Standard Bukkit sound volume/pitch values. |

---

## 💾 Data Storage

Each player's ender chest contents are stored independently in:

```
plugins/ender-chest/data/<player-uuid>.yml
```

This data is loaded into memory on startup, kept in sync while the chest GUI is open, and written to disk whenever a player closes the inventory or the server shuts down.

---

## 🗺️ Roadmap

- [ ] Permission node support (currently open to all players)
- [ ] Per-world or per-group chest sizes
- [ ] PlaceholderAPI support

Contributions and suggestions are welcome — feel free to open an issue or pull request.

---

## 📺 Tutorial

A video walkthrough is coming soon on the **BenyVKx** YouTube channel.

---

## 📄 License

https://github.com/BenyVK/enderchestMC/blob/main/LICENSE

---

## 👤 Credits

| | |
|---|---|
| **Plugin** | Ender Chest |
| **Author** | Benyamin Gharri |
| **YouTube** | [BenyVKx](https://www.youtube.com/@BenyVKx) |
| **Tutorial Video** | Coming soon |
| **Persian README.md** | [README.FA.md](https://github.com/BenyVK/enderchestMC/blob/main/README.FA.md) |
