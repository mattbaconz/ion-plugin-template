d# IonAPI Template Plugin

A comprehensive template showcasing IonAPI v1.2.0 features for Minecraft plugin development.

## Features Demonstrated

- **Database ORM** - SQLite/MySQL with caching and async operations
- **Economy System** - Full economy with transactions and transfers
- **GUI Framework** - Shop GUI with item purchasing
- **Scoreboard** - Live-updating scoreboard with placeholders
- **Cooldowns** - Thread-safe cooldown management
- **Rate Limiting** - Chat spam prevention
- **Hot-Reload Config** - Live config reloading
- **Redis** - Optional pub/sub messaging
- **Metrics** - Performance tracking
- **MessageBuilder** - MiniMessage formatting

## Commands

| Command | Description |
|---------|-------------|
| `/spawn` | Teleport to spawn (with cooldown) |
| `/balance [player]` | Check balance |
| `/pay <player> <amount>` | Pay another player |
| `/shop` | Open shop GUI |
| `/stats` | View your stats |
| `/scoreboard` | Toggle scoreboard |

## Building

```bash
./gradlew shadowJar
```

Output: `build/libs/IonTemplatePlugin-1.0.0.jar`

## Configuration

Edit `plugins/IonTemplatePlugin/config.yml` - changes apply instantly via hot-reload.

## Project Structure

```
src/main/java/com/example/iontemplate/
├── IonTemplatePlugin.java      # Main plugin class
├── command/                    # Command handlers
├── data/                       # Database entities
├── economy/                    # Economy provider
├── gui/                        # GUI menus
├── listener/                   # Event listeners
└── manager/                    # Managers (scoreboard)
```

## Requirements

- Java 21+
- Paper 1.20.4+
- IonAPI 1.2.0 (published to mavenLocal)

## Note on Shading

For production use, enable relocation in `build.gradle.kts` to avoid conflicts:

```kotlin
tasks.shadowJar {
    relocate("com.ionapi", "${project.group}.libs.ionapi")
}
```
