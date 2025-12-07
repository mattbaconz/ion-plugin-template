# 🚀 IonAPI Plugin Template

A production-ready Minecraft plugin template showcasing [IonAPI v1.2.0](https://github.com/mattbaconz/IonAPI) features and best practices.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Paper](https://img.shields.io/badge/Paper-1.20.4-blue.svg)](https://papermc.io/)
[![IonAPI](https://img.shields.io/badge/IonAPI-1.2.0-green.svg)](https://github.com/mattbaconz/IonAPI)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## ✨ Features Demonstrated

### 💾 Database & Persistence
- **ORM System** - Entity mapping with `@Table`, `@PrimaryKey`, `@Column` annotations
- **Caching** - Automatic entity caching with `@Cacheable` for 10-50x performance boost
- **Async Operations** - Non-blocking database queries with CompletableFuture
- **SQLite & MySQL** - Switchable database backends via configuration

### 💰 Economy System
- **Full Economy Provider** - Complete implementation with transactions
- **Vault Integration** - Compatible with Vault-dependent plugins
- **Transaction API** - Fluent builder pattern for complex operations
- **Async by Default** - All economy operations are non-blocking

### 🎨 User Interface
- **GUI Framework** - Inventory-based menus with click handlers
- **Shop System** - Item purchasing with economy integration
- **Live Scoreboard** - Auto-updating sidebar with placeholders
- **MiniMessage** - Modern text formatting with gradients and colors

### ⚡ Performance & Utilities
- **Cooldown Manager** - Thread-safe player cooldown tracking
- **Rate Limiter** - Spam prevention with sliding window algorithm
- **Hot-Reload Config** - Live configuration updates without restart
- **Metrics Tracking** - Built-in performance monitoring

### 🔗 Additional Features
- **Redis Pub/Sub** - Cross-server messaging (optional)
- **Task Scheduler** - Unified async/sync task API
- **Warp System** - Save and teleport to custom locations
- **Leaderboards** - Top players by kills, deaths, and K/D ratio
- **BossBar Manager** - Progress bars, health bars, and notifications
- **Messages System** - Externalized messages with MiniMessage support

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/spawn` | Teleport to spawn (30s cooldown) | `iontemplate.spawn` |
| `/balance [player]` | Check balance | `iontemplate.balance` |
| `/pay <player> <amount>` | Transfer money (5s cooldown) | `iontemplate.pay` |
| `/shop` | Open shop GUI | `iontemplate.shop` |
| `/stats` | View your statistics | `iontemplate.stats` |
| `/scoreboard` | Toggle scoreboard display | `iontemplate.scoreboard` |
| `/warp [list\|set\|delete] [name]` | Manage and teleport to warps | `iontemplate.warp` |
| `/leaderboard [kills\|deaths\|kdr]` | View top players | `iontemplate.leaderboard` |

## 🛠️ Building

### Prerequisites
- Java 21 or higher
- Gradle 8.0+ (wrapper included)
- IonAPI 1.2.0 (see build steps)

### Build Steps

#### Option 1: Local Build (Recommended)

1. **Clone both repositories**
```bash
# Clone IonAPI
git clone https://github.com/mattbaconz/IonAPI.git
cd IonAPI
./gradlew publishToMavenLocal
cd ..

# Clone template
git clone https://github.com/mattbaconz/ion-plugin-template.git
cd ion-plugin-template
```

2. **Build the plugin**
```bash
./gradlew shadowJar
```

Output: `build/libs/IonTemplatePlugin-1.0.0.jar` (402KB)

#### Option 2: GitHub Actions

The repository includes a GitHub Actions workflow that automatically:
1. Checks out IonAPI from the main repository
2. Builds and publishes IonAPI to local Maven
3. Builds the template plugin
4. Uploads the JAR as an artifact

Simply push to `main` or `develop` branch to trigger the build.

> **Note**: IonAPI must be available in the [main repository](https://github.com/mattbaconz/IonAPI) for CI/CD builds to work.

### 🐳 Quick Start with Docker

Test the plugin with MySQL and Redis instantly:

```bash
# Start services
docker-compose up -d

# Build and deploy
./gradlew shadowJar
docker-compose restart paper

# View logs
docker-compose logs -f paper
```

See [DOCKER.md](DOCKER.md) for detailed Docker setup.

## ⚙️ Configuration

Edit `plugins/IonTemplatePlugin/config.yml`:

```yaml
# Database settings
database:
  type: sqlite  # sqlite or mysql
  mysql:
    host: localhost
    port: 3306
    database: minecraft
    username: root
    password: ""

# Economy settings
economy:
  starting-balance: 1000.0
  currency-symbol: "$"

# Cooldowns (seconds)
cooldowns:
  teleport: 30
  pay: 5

# Rate limits
rate-limits:
  chat:
    max-messages: 5
    window-seconds: 10

# Redis (optional)
redis:
  enabled: false
  host: localhost
  port: 6379
```

Changes apply instantly via hot-reload!

## 📁 Project Structure

```
src/main/java/com/example/iontemplate/
├── IonTemplatePlugin.java          # Main plugin class
├── command/                        # Command handlers (8 commands)
│   ├── BalanceCommand.java
│   ├── PayCommand.java
│   ├── ShopCommand.java
│   ├── SpawnCommand.java
│   ├── StatsCommand.java
│   ├── ScoreboardCommand.java
│   ├── WarpCommand.java            # NEW: Warp management
│   └── LeaderboardCommand.java     # NEW: Top players
├── data/                           # Database entities
│   ├── PlayerData.java             # Player stats (@Cacheable)
│   ├── PlayerBalance.java          # Economy balances
│   └── Warp.java                   # NEW: Saved locations
├── economy/                        # Economy implementation
│   └── TemplateEconomyProvider.java
├── gui/                            # GUI menus
│   └── ShopGui.java
├── listener/                       # Event listeners
│   └── PlayerListener.java
└── manager/                        # Feature managers
    ├── ScoreboardManager.java
    └── BossBarManager.java         # NEW: BossBar API
```

## 🎯 Code Examples

### Database Entity with Caching
```java
@Table("player_data")
@Cacheable(ttl = 60, maxSize = 500)  // Cache for 60 seconds
public class PlayerData {
    @PrimaryKey
    private UUID uuid;
    
    @Column(name = "player_name", nullable = false)
    private String name;
    
    @Column(defaultValue = "0")
    private int kills;
    
    // Getters/setters...
}
```

### Async Database Operations
```java
database.findAsync(PlayerData.class, uuid).thenAccept(opt -> {
    if (opt.isPresent()) {
        PlayerData data = opt.get();
        data.addKill();
        database.saveAsync(data);
    }
});
```

### Economy Transactions
```java
IonEconomy.transaction(player.getUniqueId())
    .withdraw(BigDecimal.valueOf(100))
    .reason("Shop purchase")
    .commit()
    .thenAccept(result -> {
        if (result.isSuccess()) {
            player.sendMessage("Purchase complete!");
        }
    });
```

### GUI with Click Handlers
```java
new IonGuiBuilder()
    .title("<gold><bold>Shop Menu")
    .rows(3)
    .item(10, diamondSword, click -> {
        purchaseItem(click.getPlayer(), "diamond_sword");
    })
    .build()
    .open(player);
```

## 📊 Performance

- **Database**: 10-50x faster with automatic caching
- **JAR Size**: 402KB (includes all dependencies)
- **Memory**: ~5MB runtime footprint
- **Startup**: <100ms initialization

## 🔧 Customization

### Reduce JAR Size

Comment out unused modules in `build.gradle.kts`:

```kotlin
dependencies {
    // Remove Redis if not needed (-15KB + Jedis)
    // implementation("com.ionapi:ion-redis:1.2.0")
    
    // Remove database if not needed (-53KB + HikariCP)
    // implementation("com.ionapi:ion-database:1.2.0")
}
```

### Enable Relocation (Production)

Uncomment in `build.gradle.kts` to avoid conflicts:

```kotlin
tasks.shadowJar {
    relocate("com.ionapi", "${project.group}.libs.ionapi")
}
```

## 🐛 Troubleshooting

### IDE Shows Errors

The IDE may not see IonAPI dependencies. This is normal - the code compiles fine with Gradle.

**Fix**: Reload Gradle project or run:
```bash
./gradlew --refresh-dependencies build
```

### Database Connection Issues

**SQLite**: Ensure `plugins/IonTemplatePlugin/` folder exists  
**MySQL**: Verify credentials and database exists

### Redis Connection Failed

Redis is optional. Set `redis.enabled: false` in config if not using.

## 📚 Resources

- **IonAPI Repository**: [github.com/mattbaconz/IonAPI](https://github.com/mattbaconz/IonAPI)
- **IonAPI Documentation**: [View Docs](https://github.com/mattbaconz/IonAPI#readme)
- **Template Repository**: [github.com/mattbaconz/ion-plugin-template](https://github.com/mattbaconz/ion-plugin-template)
- **Discord Support**: [discord.com/invite/VQjTVKjs46](https://discord.com/invite/VQjTVKjs46)
- **Report Issues**: [GitHub Issues](https://github.com/mattbaconz/ion-plugin-template/issues)

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This template is licensed under the MIT License - see [LICENSE](LICENSE) for details.

## 💖 Support the Project

If you find this template helpful:
- ⭐ **Star the repository** on [GitHub](https://github.com/mattbaconz/ion-plugin-template)
- 🐛 **Report bugs** via [GitHub Issues](https://github.com/mattbaconz/ion-plugin-template/issues)
- 💬 **Join our community** on [Discord](https://discord.com/invite/VQjTVKjs46)
- ☕ **Support development**:
  - [Ko-fi](https://ko-fi.com/mbczishim/tip)
  - [PayPal](https://www.paypal.com/paypalme/MatthewWatuna)

## 👨‍💻 Author

**mattbaconz**
- GitHub: [@mattbaconz](https://github.com/mattbaconz)
- Discord: [Join Server](https://discord.com/invite/VQjTVKjs46)

---

**Built with ❤️ using [IonAPI](https://github.com/mattbaconz/IonAPI)**
