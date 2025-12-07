# 🤖 IonAPI v1.2.0 - Complete AI Development Guide

> **Purpose**: Comprehensive all-in-one guide for AI assistants to build Minecraft plugins with IonAPI.
> Copy this file to any project using IonAPI for instant AI understanding.

**Version**: 1.2.0  
**Last Updated**: December 7, 2024  
**Author**: mattbaconz  
**Repository**: https://github.com/mattbaconz/IonAPI  
**JitPack**: https://jitpack.io/#mattbaconz/IonAPI

---

## 📋 Quick Reference

### Available Modules (16 total)
| Module | Description | Size |
|--------|-------------|------|
| `ion-api` | Core plugin API, utilities | 23.8 KB |
| `ion-core` | Platform abstraction (Paper/Folia) | 8.2 KB |
| `ion-database` | Async ORM + Caching + Batch ops | 52.7 KB |
| `ion-economy` | Economy system + Vault hook | 18.5 KB |
| `ion-redis` | Redis pub/sub + KV storage | 15.3 KB |
| `ion-gui` | Inventory GUI framework | 22.1 KB |
| `ion-item` | ItemStack builder | 12.4 KB |
| `ion-ui` | Scoreboard + BossBar | 10.5 KB |
| `ion-tasks` | Unified scheduler (Bukkit/Folia) | 9.8 KB |
| `ion-proxy` | Cross-server messaging | 11.2 KB |
| `ion-npc` | Packet NPCs | 14.6 KB |
| `ion-placeholder` | PlaceholderAPI bridge | 6.3 KB |
| `ion-inject` | Dependency injection | 8.1 KB |
| `ion-compat` | Version compatibility | 5.4 KB |
| `ion-test` | Testing framework | 7.2 KB |
| **Total** | All modules combined | **273 KB** |

---

## 🚀 Installation (JitPack)

### Gradle (Kotlin DSL) - Recommended
```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("com.github.mattbaconz:IonAPI:1.2.0")
}

tasks.shadowJar {
    archiveClassifier.set("")
    // ⚠️ CRITICAL: Always relocate to avoid conflicts with other plugins
    relocate("com.ionapi", "${project.group}.libs.ionapi")
    minimize()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.mattbaconz</groupId>
        <artifactId>IonAPI</artifactId>
        <version>1.2.0</version>
    </dependency>
</dependencies>
```

---

## ⚡ v1.2.0 New Features

### CooldownManager
Thread-safe player cooldown management with automatic cleanup.
```java
CooldownManager cooldowns = CooldownManager.create("teleport");

if (cooldowns.isOnCooldown(player.getUniqueId())) {
    long remaining = cooldowns.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
    player.sendMessage("<red>Wait " + remaining + "s!");
    return;
}

// Perform action...
cooldowns.setCooldown(player.getUniqueId(), 30, TimeUnit.SECONDS);

// Other methods
cooldowns.clearCooldown(player.getUniqueId());
cooldowns.clearAll();
```

### RateLimiter
Sliding window rate limiting for spam prevention.
```java
// Allow 5 messages per 10 seconds
RateLimiter limiter = RateLimiter.create("chat", 5, 10, TimeUnit.SECONDS);

if (!limiter.tryAcquire(player.getUniqueId())) {
    player.sendMessage("<red>Slow down!");
    return;
}

// Other methods
int remaining = limiter.getRemainingPermits(player.getUniqueId());
long resetTime = limiter.getResetTime(player.getUniqueId());
limiter.reset(player.getUniqueId());
```


### MessageBuilder
Fluent MiniMessage builder with templates and placeholders.
```java
// Simple message
MessageBuilder.of("<green>Hello, <player>!")
    .placeholder("player", player.getName())
    .send(player);

// Title with subtitle
MessageBuilder.of("<gold><bold>LEVEL UP!")
    .subtitle("<gray>Level <level>")
    .placeholder("level", "10")
    .fadeIn(10).stay(70).fadeOut(20)
    .sendTitle(player);

// Action bar
MessageBuilder.of("<red>❤ <health>/<max>")
    .placeholder("health", "15")
    .placeholder("max", "20")
    .sendActionBar(player);

// Broadcast
MessageBuilder.of("<yellow>[Server] <message>")
    .placeholder("message", "Welcome!")
    .broadcast();

// Reusable template
MessageBuilder template = MessageBuilder.of("<prefix> <message>")
    .placeholder("prefix", "<gold>[Shop]");
template.placeholder("message", "Item purchased!").send(player);
```

### IonScoreboard
Easy scoreboard creation with MiniMessage and dynamic placeholders.
```java
IonScoreboard board = IonScoreboard.builder()
    .title("<gradient:gold:yellow><bold>My Server")
    .line(15, "<gray>Welcome, <white>{player}")
    .line(14, "")
    .line(13, "<gold>Coins: <yellow>{coins}")
    .line(12, "<aqua>Kills: <white>{kills}")
    .line(11, "")
    .line(10, "<gray>play.myserver.com")
    .placeholder("player", p -> p.getName())
    .placeholder("coins", p -> String.valueOf(getCoins(p)))
    .placeholder("kills", p -> String.valueOf(getKills(p)))
    .build();

board.show(player);
board.update(player); // Refresh placeholders
board.hide(player);
```

### IonBossBar
Boss bar management with MiniMessage and dynamic updates.
```java
IonBossBar bar = IonBossBar.builder()
    .name("event")  // Unique identifier
    .title("<gradient:red:orange>Event: {progress}%")
    .color(BossBar.Color.RED)
    .style(BossBar.Style.SEGMENTED_10)
    .progress(0.5f)
    .placeholder("progress", p -> String.valueOf((int)(bar.getProgress() * 100)))
    .build();

bar.show(player);
bar.setProgress(0.75f);
bar.setTitle("<green>Event Complete!");
bar.setColor(BossBar.Color.GREEN);
bar.hide(player);

// Retrieve by name
IonBossBar existing = IonBossBar.get("event");
```


### BatchOperation (10-50x faster bulk operations)
```java
List<PlayerStats> stats = new ArrayList<>();
// ... populate list with 1000+ records

BatchOperation.BatchResult result = database.batch(PlayerStats.class)
    .insertAll(stats)
    .batchSize(500)  // Process 500 at a time
    .execute();

System.out.println("Inserted: " + result.insertedCount());
System.out.println("Time: " + result.executionTimeMs() + "ms");

// Async execution
database.batch(PlayerStats.class)
    .insertAll(stats)
    .executeAsync()
    .thenAccept(r -> System.out.println("Done: " + r.insertedCount()));

// Update and delete
database.batch(PlayerStats.class)
    .updateAll(modifiedStats)
    .deleteAll(oldStats)
    .execute();
```

### Metrics
Lightweight performance monitoring system.
```java
// Counters
Metrics.increment("player.join");
Metrics.increment("player.join", 5);  // Add 5
long joins = Metrics.getCount("player.join");

// Gauges (current values)
Metrics.gauge("players.online", Bukkit.getOnlinePlayers().size());
long online = Metrics.getGauge("players.online");

// Timing
String result = Metrics.time("database.query", () -> {
    return db.findAll(PlayerData.class);
});

// Timing statistics
double avgTime = Metrics.getAverageTime("database.query");
long minTime = Metrics.getMinTime("database.query");
long maxTime = Metrics.getMaxTime("database.query");
long totalTime = Metrics.getTotalTime("database.query");
long callCount = Metrics.getTimingCount("database.query");

// Reset
Metrics.reset("player.join");
Metrics.resetAll();
```

### ReflectionCache (Automatic - No Code Needed)
Entity metadata is automatically cached for 10-50x faster ORM operations.
- Eliminates repeated reflection calls
- Automatic field discovery
- Column name resolution
- Primary key detection
- Zero configuration required

---

## 💾 Database ORM

### Basic Entity
```java
@Table("players")
public class PlayerData {
    @PrimaryKey
    private UUID uuid;
    
    @Column(name = "player_name", nullable = false, length = 16)
    private String name;
    
    @Column(defaultValue = "0")
    private int level;
    
    @Column(name = "last_login")
    private long lastLogin;
    
    // Getters/setters required
}
```


### Entity with Caching
```java
@Table("player_settings")
@Cacheable(ttl = 60, maxSize = 500)  // Cache 60 seconds, max 500 entries
public class PlayerSettings {
    @PrimaryKey
    private UUID playerId;
    
    @Column
    private boolean notifications;
    
    @Column(length = 5, defaultValue = "'en'")
    private String language;
}
```

### Entity Relationships
```java
// Parent entity
@Table("guilds")
public class Guild {
    @PrimaryKey
    private UUID id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "guildId", fetch = FetchType.LAZY)
    private List<GuildMember> members;
}

// Child entity
@Table("guild_members")
public class GuildMember {
    @PrimaryKey
    private UUID id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guild_id", referencedColumnName = "id")
    private UUID guildId;
    
    @Column(name = "player_id")
    private UUID playerId;
    
    @Column(name = "rank")
    private String rank;
}
```

### Database Operations
```java
// Initialize SQLite
IonDatabase db = IonDatabaseBuilder.sqlite(plugin)
    .file("data.db")
    .build();

// Initialize MySQL
IonDatabase db = IonDatabaseBuilder.mysql(plugin)
    .host("localhost")
    .port(3306)
    .database("minecraft")
    .username("user")
    .password("pass")
    .build();

// Register entities
db.register(PlayerData.class);
db.register(Guild.class);

// CRUD Operations
PlayerData data = new PlayerData(uuid, "Player123", 5);
db.save(data);                              // Insert or update
PlayerData found = db.find(PlayerData.class, uuid);  // Find by PK
db.delete(data);                            // Delete

// Query Builder
List<PlayerData> results = db.query(PlayerData.class)
    .where("level", ">", 10)
    .where("name", "LIKE", "Player%")
    .orderBy("level", "DESC")
    .limit(10)
    .offset(0)
    .execute();

// Single result
PlayerData first = db.query(PlayerData.class)
    .where("name", "Player123")
    .first();

// Async operations (RECOMMENDED)
db.findAsync(PlayerData.class, uuid)
    .thenAccept(data -> {
        // Process on async thread
    });

db.saveAsync(data).thenRun(() -> {
    // Saved successfully
});

// Transactions
try (Transaction tx = db.beginTransaction()) {
    PlayerData player = db.find(PlayerData.class, uuid);
    player.setLevel(player.getLevel() + 1);
    db.save(player);
    tx.commit();  // All or nothing
} catch (Exception e) {
    // Automatic rollback
}
```


---

## 💰 Economy System

### Basic Usage
```java
// Check balance
IonEconomy.getBalance(player.getUniqueId()).thenAccept(balance -> {
    player.sendMessage("Balance: " + IonEconomy.format(balance));
});

// Check if has enough
IonEconomy.has(player.getUniqueId(), 100).thenAccept(hasEnough -> {
    if (hasEnough) {
        // Can afford
    }
});

// Withdraw
IonEconomy.withdraw(player.getUniqueId(), 100).thenAccept(result -> {
    if (result.isSuccess()) {
        player.sendMessage("<green>Purchase complete!");
    } else {
        player.sendMessage("<red>Insufficient funds!");
    }
});

// Deposit
IonEconomy.deposit(player.getUniqueId(), 50);

// Transfer
IonEconomy.transfer(sender.getUniqueId(), receiver.getUniqueId(), 
    BigDecimal.valueOf(100));
```

### Fluent Transaction API
```java
IonEconomy.transaction(player.getUniqueId())
    .withdraw(BigDecimal.valueOf(100))
    .reason("Shop purchase: Diamond Sword")
    .commit()
    .thenAccept(result -> {
        if (result.isSuccess()) {
            player.sendMessage("<green>Transaction complete!");
            player.sendMessage("New balance: " + IonEconomy.format(result.getNewBalance()));
        } else {
            switch (result.getType()) {
                case INSUFFICIENT_FUNDS -> player.sendMessage("<red>Not enough money!");
                case ACCOUNT_NOT_FOUND -> player.sendMessage("<red>Account not found!");
                default -> player.sendMessage("<red>Transaction failed!");
            }
        }
    });
```

### Admin Commands
```
/ion eco set <player> <amount>   - Set balance
/ion eco give <player> <amount>  - Give money
/ion eco take <player> <amount>  - Take money
/ion eco debug <player>          - View database state
```

### Vault Integration
```java
// In your plugin's onEnable()
if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
    Economy vaultEconomy = new IonEconomyVaultHook(economyProvider);
    Bukkit.getServicesManager().register(
        Economy.class, vaultEconomy, this, ServicePriority.Highest
    );
}
```

---

## 🔴 Redis Integration

### Setup
```java
IonRedis redis = IonRedisBuilder.create()
    .host("localhost")
    .port(6379)
    .password("secret")  // Optional
    .database(0)
    .timeout(5000)
    .ssl(false)
    .build();
```

### Pub/Sub Messaging
```java
// Subscribe to channel
redis.subscribe("player-events", message -> {
    String data = message.data();
    long age = message.getAge();  // Milliseconds since received
    Bukkit.broadcastMessage("Event: " + data);
});

// Publish message
redis.publish("player-events", "Player joined: " + player.getName());

// Unsubscribe
redis.unsubscribe("player-events");
```


### Key-Value Storage
```java
// Set value
redis.set("player:" + uuid, playerData);

// Set with TTL (expires in 1 hour)
redis.set("session:" + uuid, sessionData, 3600);

// Get value
redis.get("player:" + uuid).thenAccept(data -> {
    if (data != null) {
        processData(data);
    }
});

// Delete
redis.delete("player:" + uuid);

// Check existence
redis.exists("player:" + uuid).thenAccept(exists -> {
    if (exists) { /* Key exists */ }
});

// Set expiration
redis.expire("player:" + uuid, 3600);

// Get TTL
redis.ttl("player:" + uuid).thenAccept(seconds -> {
    getLogger().info("Expires in " + seconds + " seconds");
});
```

### Health Monitoring
```java
if (redis.isConnected()) {
    RedisStats stats = redis.getStats();
    getLogger().info("Published: " + stats.messagesPublished());
    getLogger().info("Received: " + stats.messagesReceived());
    getLogger().info("Subscriptions: " + stats.activeSubscriptions());
    getLogger().info("Uptime: " + stats.connectionUptime() + "ms");
}
```

---

## 🔥 Hot-Reload Config

```java
HotReloadConfig config = HotReloadConfig.create(this, "config.yml")
    .onReload(cfg -> {
        welcomeMessage = cfg.getString("welcome-message");
        maxPlayers = cfg.getInt("max-players");
        getLogger().info("Config reloaded!");
    })
    .start();

// Edit config.yml while server is running - changes apply instantly!

// Multiple handlers
config.onReload("messages", cfg -> loadMessages(cfg));
config.onReload("limits", cfg -> loadLimits(cfg));

// Remove handler
config.removeHandler("messages");

// Manual reload
config.reload();

// Stop watching
config.stop();
```

---

## 🎨 Item Builder

```java
ItemStack sword = IonItem.builder(Material.DIAMOND_SWORD)
    .name("<gradient:red:blue>Legendary Sword")
    .lore(
        "<gray>Forged in dragon fire",
        "",
        "<gold>⚔ Damage: +50",
        "<aqua>✦ Speed: +10%",
        "",
        "<yellow>Right-click to activate"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .enchant(Enchantment.UNBREAKING, 3)
    .unbreakable()
    .glow()  // Enchantment glow without visible enchants
    .customModelData(12345)
    .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
    .build();

// Simple item
ItemStack glass = IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " ");

// Skull
ItemStack skull = IonItem.skull(player.getUniqueId())
    .name("<gold>" + player.getName())
    .build();
```


---

## 📦 GUI System

### Basic GUI
```java
IonGui.builder()
    .title("<gold><bold>✨ Shop Menu")
    .rows(3)
    .item(10, diamondItem, click -> {
        Player player = click.getPlayer();
        if (buyItem(player, 100)) {
            player.sendMessage("<green>✓ Purchased!");
            click.close();
        } else {
            player.sendMessage("<red>✗ Not enough money!");
        }
    })
    .item(12, emeraldItem, click -> { /* ... */ })
    .item(14, goldItem, click -> { /* ... */ })
    .fillBorder(IonItem.of(Material.GRAY_STAINED_GLASS_PANE, " "))
    .build()
    .open(player);
```

### Paginated GUI
```java
List<ItemStack> items = getShopItems();  // Many items

IonGui.paginated()
    .title("<gold>Shop - Page {page}/{pages}")
    .rows(6)
    .items(items)
    .itemsPerPage(45)
    .onClick((click, item) -> {
        buyItem(click.getPlayer(), item);
    })
    .previousButton(45, IonItem.of(Material.ARROW, "<red>← Previous"))
    .nextButton(53, IonItem.of(Material.ARROW, "<green>Next →"))
    .build()
    .open(player);
```

### Security Features (Built-in)
```java
// Default: All item manipulation DISABLED (safe)
IonGui.builder()
    .title("Safe GUI")
    // allowTake = false (default)
    // allowPlace = false (default)
    // allowDrag = false (default)
    .build();

// Shift-click and number keys ALWAYS blocked to prevent duping
// Bottom inventory clicks ALWAYS cancelled

// Only enable if you NEED to give items:
IonGui.builder()
    .title("Rewards")
    .allowTake(true)  // ⚠️ Use with caution
    .build();
```

---

## 🔗 Task Chains (Folia Compatible)

```java
TaskChain.create(plugin)
    .async(() -> database.loadPlayerData(uuid))
    .syncAt(player, data -> {
        player.setLevel(data.level);
        player.sendMessage("<green>✓ Data loaded!");
    })
    .delay(2, TimeUnit.SECONDS)
    .syncAt(player, () -> player.sendMessage("<gold>Welcome back!"))
    .exceptionally(ex -> {
        player.sendMessage("<red>✗ Failed to load data!");
        ex.printStackTrace();
    })
    .execute();

// Simple async
IonTasks.runAsync(plugin, () -> {
    // Heavy operation
});

// Simple sync
IonTasks.runSync(plugin, () -> {
    // Main thread operation
});

// Delayed
IonTasks.runLater(plugin, () -> {
    // Runs after 20 ticks
}, 20);

// Repeating
IonTasks.runTimer(plugin, () -> {
    // Runs every 20 ticks
}, 0, 20);
```


---

## 📊 Complete Plugin Example

```java
public class MyPlugin extends JavaPlugin implements IonPlugin {
    
    private IonDatabase database;
    private IonRedis redis;
    private HotReloadConfig config;
    private CooldownManager teleportCooldowns;
    private RateLimiter chatLimiter;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Database
        database = IonDatabaseBuilder.sqlite(this)
            .file("data.db")
            .build();
        database.register(PlayerData.class);
        
        // Redis (optional)
        if (getConfig().getBoolean("redis.enabled")) {
            redis = IonRedisBuilder.create()
                .host(getConfig().getString("redis.host"))
                .port(getConfig().getInt("redis.port"))
                .password(getConfig().getString("redis.password"))
                .build();
            redis.subscribe("global-chat", this::handleGlobalChat);
        }
        
        // Hot-reload config
        config = HotReloadConfig.create(this, "config.yml")
            .onReload(this::loadSettings)
            .start();
        
        // Utilities
        teleportCooldowns = CooldownManager.create("teleport");
        chatLimiter = RateLimiter.create("chat", 5, 10, TimeUnit.SECONDS);
        
        // Economy
        EconomyProvider economyProvider = new MyEconomyProvider(this, database);
        IonEconomy.setProvider(economyProvider);
        
        // Commands & Events
        getCommand("spawn").setExecutor(this::onSpawnCommand);
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("Plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        if (config != null) config.stop();
        if (redis != null) redis.close();
        if (database != null) database.close();
    }
    
    private boolean onSpawnCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        
        if (teleportCooldowns.isOnCooldown(player.getUniqueId())) {
            long remaining = teleportCooldowns.getRemainingTime(player.getUniqueId(), TimeUnit.SECONDS);
            MessageBuilder.of("<red>Wait <time>s before teleporting again!")
                .placeholder("time", String.valueOf(remaining))
                .send(player);
            return true;
        }
        
        player.teleport(getSpawnLocation());
        teleportCooldowns.setCooldown(player.getUniqueId(), 30, TimeUnit.SECONDS);
        MessageBuilder.of("<green>Teleported to spawn!").send(player);
        return true;
    }
    
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!chatLimiter.tryAcquire(player.getUniqueId())) {
            event.setCancelled(true);
            MessageBuilder.of("<red>Slow down! You're sending messages too fast.")
                .send(player);
        }
    }
    
    private void handleGlobalChat(RedisMessage message) {
        Bukkit.broadcast(Component.text("[Global] " + message.data()));
    }
    
    private void loadSettings(FileConfiguration cfg) {
        // Reload settings
    }
}
```


---

## 🎯 Common Patterns

### Pattern 1: Shop System with Economy
```java
public void buyItem(Player player, ItemStack item, double price) {
    IonEconomy.has(player.getUniqueId(), price).thenAccept(hasEnough -> {
        if (!hasEnough) {
            MessageBuilder.of("<red>Not enough money! Need <gold>$<price>")
                .placeholder("price", String.valueOf(price))
                .send(player);
            return;
        }
        
        IonEconomy.transaction(player.getUniqueId())
            .withdraw(price)
            .reason("Shop purchase: " + item.getType())
            .commit()
            .thenAccept(result -> {
                if (result.isSuccess()) {
                    player.getInventory().addItem(item);
                    MessageBuilder.of("<green>Purchased! New balance: <gold>$<balance>")
                        .placeholder("balance", IonEconomy.format(result.getNewBalance()))
                        .send(player);
                }
            });
    });
}
```

### Pattern 2: Cross-Server Player Tracking
```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    
    // Publish join event to all servers
    redis.publish("player-join", player.getName() + ":" + getServerName());
    
    // Store player location
    redis.set("player:" + player.getUniqueId() + ":server", getServerName(), 3600);
}

public void findPlayer(CommandSender sender, String playerName) {
    redis.get("player:" + playerName + ":server").thenAccept(server -> {
        if (server != null) {
            sender.sendMessage(playerName + " is on " + server);
        } else {
            sender.sendMessage(playerName + " is offline");
        }
    });
}
```

### Pattern 3: Cached Player Settings
```java
@Table("player_settings")
@Cacheable(ttl = 60, maxSize = 500)
public class PlayerSettings {
    @PrimaryKey
    private UUID playerId;
    
    @Column
    private boolean notifications = true;
    
    @Column
    private String language = "en";
}

// First call hits database, subsequent calls use cache
public PlayerSettings getSettings(UUID playerId) {
    return database.query(PlayerSettings.class)
        .where("player_id", playerId)
        .first();
}
```

### Pattern 4: Scoreboard with Live Updates
```java
private final Map<UUID, IonScoreboard> scoreboards = new HashMap<>();

public void showScoreboard(Player player) {
    IonScoreboard board = IonScoreboard.builder()
        .title("<gradient:gold:yellow><bold>My Server")
        .line(15, "<white>{player}")
        .line(14, "")
        .line(13, "<gold>Coins: <yellow>{coins}")
        .line(12, "<red>Kills: <white>{kills}")
        .line(11, "<green>Online: <white>{online}")
        .placeholder("player", p -> p.getName())
        .placeholder("coins", p -> String.valueOf(getCoins(p)))
        .placeholder("kills", p -> String.valueOf(getKills(p)))
        .placeholder("online", p -> String.valueOf(Bukkit.getOnlinePlayers().size()))
        .build();
    
    board.show(player);
    scoreboards.put(player.getUniqueId(), board);
}

// Update every second
IonTasks.runTimer(plugin, () -> {
    scoreboards.forEach((uuid, board) -> {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) board.update(player);
    });
}, 0, 20);
```


---

## 🔒 Security Best Practices

### GUI Security (Duping Prevention)
```java
// ✅ SAFE - Default behavior blocks all item manipulation
IonGui.builder()
    .title("Shop")
    .item(10, item, click -> { /* handle */ })
    .build();

// ⚠️ CAUTION - Only use when giving items to players
IonGui.builder()
    .title("Rewards")
    .allowTake(true)
    .build();
```

### Database Security (SQL Injection Prevention)
```java
// ✅ SAFE - Uses parameterized queries
db.query(PlayerData.class)
    .where("name", "LIKE", userInput)
    .execute();

// ❌ UNSAFE - Never concatenate user input
db.execute("SELECT * FROM players WHERE name = '" + userInput + "'");
```

### Economy Security
```java
// ✅ SAFE - Use BigDecimal for precision
BigDecimal amount = BigDecimal.valueOf(100.50);

// ❌ UNSAFE - Floating point errors
double amount = 100.50;  // Can cause precision issues
```

### Rate Limiting
```java
// Always rate limit user actions
RateLimiter limiter = RateLimiter.create("action", 5, 10, TimeUnit.SECONDS);
if (!limiter.tryAcquire(player.getUniqueId())) {
    player.sendMessage("<red>Slow down!");
    return;
}
```

---

## ✅ Best Practices Checklist

### Database
- [ ] Always use async operations for database calls
- [ ] Use transactions for multiple related operations
- [ ] Use `@Cacheable` for frequently accessed entities
- [ ] Use `BatchOperation` for bulk inserts/updates
- [ ] Close database connection in `onDisable()`

### Performance
- [ ] Use `Metrics.time()` to monitor slow operations
- [ ] Use `ReflectionCache` (automatic) for ORM performance
- [ ] Use `BatchOperation` for 1000+ records
- [ ] Avoid blocking the main thread

### Security
- [ ] Never concatenate user input in SQL
- [ ] Use default GUI settings (no allowTake/allowPlace)
- [ ] Rate limit all user actions
- [ ] Validate all user input
- [ ] Check permissions before sensitive operations

### Code Quality
- [ ] Always relocate IonAPI when shading
- [ ] Use MiniMessage format (`<green>` not `§a`)
- [ ] Close resources in `onDisable()`
- [ ] Handle async exceptions with `.exceptionally()`

---

## 🐛 Troubleshooting

### Database Issues
| Problem | Solution |
|---------|----------|
| Connection timeout | Check database server is running |
| SQL syntax error | Use QueryBuilder, not raw SQL |
| Entity not saving | Ensure getters/setters exist |
| Cache not working | Check `@Cacheable` annotation |

### Economy Issues
| Problem | Solution |
|---------|----------|
| Vault not hooking | Add `depend: [Vault]` to plugin.yml |
| Precision errors | Use `BigDecimal.valueOf()` |
| Account not found | Create account on first join |

### Redis Issues
| Problem | Solution |
|---------|----------|
| Connection refused | Check Redis server is running |
| Messages not received | Subscribe before publishing |
| Timeout | Increase timeout in builder |

### GUI Issues
| Problem | Solution |
|---------|----------|
| Items disappearing | Don't use `allowTake(true)` |
| Duping | Use default settings |
| Click not working | Check slot number is correct |


---

## 📚 API Quick Reference

### CooldownManager
```java
CooldownManager.create(name)
.setCooldown(uuid, duration, timeUnit)
.isOnCooldown(uuid) -> boolean
.getRemainingTime(uuid, timeUnit) -> long
.clearCooldown(uuid)
.clearAll()
```

### RateLimiter
```java
RateLimiter.create(name, maxRequests, windowDuration, timeUnit)
.tryAcquire(uuid) -> boolean
.getRemainingPermits(uuid) -> int
.getResetTime(uuid) -> long
.reset(uuid)
```

### MessageBuilder
```java
MessageBuilder.of(message)
.placeholder(key, value)
.send(player)
.sendTitle(player)
.sendActionBar(player)
.broadcast()
.subtitle(text)
.fadeIn(ticks).stay(ticks).fadeOut(ticks)
```

### IonScoreboard
```java
IonScoreboard.builder()
.title(text)
.line(slot, text)
.placeholder(key, function)
.build()
.show(player)
.update(player)
.hide(player)
```

### IonBossBar
```java
IonBossBar.builder()
.name(id)
.title(text)
.color(BossBar.Color)
.style(BossBar.Style)
.progress(0.0-1.0)
.placeholder(key, function)
.build()
.show(player)
.hide(player)
.setProgress(value)
.setTitle(text)
```

### BatchOperation
```java
database.batch(EntityClass.class)
.insertAll(list)
.updateAll(list)
.deleteAll(list)
.batchSize(size)
.execute() -> BatchResult
.executeAsync() -> CompletableFuture<BatchResult>
```

### Metrics
```java
Metrics.increment(name)
Metrics.increment(name, amount)
Metrics.gauge(name, value)
Metrics.time(name, supplier) -> T
Metrics.getCount(name) -> long
Metrics.getGauge(name) -> long
Metrics.getAverageTime(name) -> double
Metrics.getMinTime(name) -> long
Metrics.getMaxTime(name) -> long
```

---

## 🔗 Resources

- **GitHub**: https://github.com/mattbaconz/IonAPI
- **JitPack**: https://jitpack.io/#mattbaconz/IonAPI
- **Discord**: https://discord.com/invite/VQjTVKjs46
- **Documentation**: https://github.com/mattbaconz/IonAPI/tree/main/docs
- **Examples**: https://github.com/mattbaconz/IonAPI/tree/main/examples
- **Ko-fi**: https://ko-fi.com/mbczishim/tip
- **PayPal**: https://www.paypal.com/paypalme/MatthewWatuna

---

## 📦 Version History

| Version | Date | Highlights |
|---------|------|------------|
| 1.2.0 | 2024-12-07 | Performance (10-50x faster ORM), 8 new utilities |
| 1.1.0 | 2024-12-06 | Economy, Redis, ORM relationships, Security fixes |
| 1.0.0 | 2024-12-06 | Initial release with 13 modules |

---

**Last Updated**: v1.2.0 (December 7, 2024)  
**AI Guide Version**: 3.0
