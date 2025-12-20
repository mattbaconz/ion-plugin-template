package com.example.iontemplate;

import com.example.iontemplate.command.*;
import com.example.iontemplate.data.PlayerBalance;
import com.example.iontemplate.data.PlayerData;
import com.example.iontemplate.economy.TemplateEconomyProvider;
import com.example.iontemplate.gui.ShopGui;
import com.example.iontemplate.listener.PlayerListener;
import com.example.iontemplate.listener.GuiProtectionListener;
import com.example.iontemplate.manager.ScoreboardManager;
import com.ionapi.api.util.CooldownManager;
import com.ionapi.api.util.Metrics;
import com.ionapi.api.util.RateLimiter;
import com.ionapi.config.HotReloadConfig;
import com.ionapi.database.DatabaseType;
import com.ionapi.database.IonDatabase;
import com.ionapi.database.IonDatabaseBuilder;
import com.ionapi.economy.IonEconomy;
import com.ionapi.redis.IonRedis;
import com.ionapi.redis.IonRedisBuilder;
import com.ionapi.paper.IonPaperPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class IonTemplatePlugin extends IonPaperPlugin {

    private IonDatabase database;
    private IonRedis redis;
    private HotReloadConfig hotConfig;
    private TemplateEconomyProvider economyProvider;
    private ScoreboardManager scoreboardManager;

    private CooldownManager teleportCooldowns;
    private CooldownManager payCooldowns;
    private RateLimiter chatLimiter;

    private Location spawnLocation;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize database
        initDatabase();

        // Initialize Redis (optional)
        initRedis();

        // Initialize utilities
        initUtilities();

        // Initialize economy
        initEconomy();

        // Initialize managers
        scoreboardManager = new ScoreboardManager(this);

        // Hot-reload config
        hotConfig = HotReloadConfig.create(this, "config.yml")
                .onReload(this::loadSettings)
                .start();

        // Load initial settings
        loadSettings(getConfig());

        // Register commands
        registerCommands();

        // Register listeners
        getServer().getPluginManager().registerEvents(
                new PlayerListener(this, chatLimiter), this);
        getServer().getPluginManager().registerEvents(
                new GuiProtectionListener(), this);

        getLogger().info("IonTemplate Plugin enabled!");
        getLogger().info("Database: " + getConfig().getString("database.type", "sqlite"));
        getLogger().info("Redis: " + (redis != null ? "connected" : "disabled"));
    }

    @Override
    public void onDisable() {
        if (hotConfig != null)
            hotConfig.stop();
        if (scoreboardManager != null)
            scoreboardManager.cleanup();
        if (redis != null)
            redis.close();
        if (database != null)
            database.disconnect();

        // Log metrics
        getLogger().info("Total player joins: " + Metrics.getCount("player.join"));
        getLogger().info("Total player kills: " + Metrics.getCount("player.kills"));

        getLogger().info("IonTemplate Plugin disabled!");
    }

    private void initDatabase() {
        String type = getConfig().getString("database.type", "sqlite");

        if ("mysql".equalsIgnoreCase(type)) {
            database = new IonDatabaseBuilder()
                    .type(DatabaseType.MYSQL)
                    .host(getConfig().getString("database.mysql.host", "localhost"))
                    .port(getConfig().getInt("database.mysql.port", 3306))
                    .database(getConfig().getString("database.mysql.database", "minecraft"))
                    .username(getConfig().getString("database.mysql.username", "root"))
                    .password(getConfig().getString("database.mysql.password", ""))
                    .build();
        } else {
            database = IonDatabase.sqlite(getDataFolder().toPath().resolve("data.db").toString())
                    .build();
        }

        try {
            database.createTable(PlayerData.class);
            database.createTable(PlayerBalance.class);
            database.createTable(com.example.iontemplate.data.Warp.class);
        } catch (Exception e) {
            getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }

    private void initRedis() {
        if (!getConfig().getBoolean("redis.enabled", false))
            return;

        try {
            redis = IonRedisBuilder.create()
                    .host(getConfig().getString("redis.host", "localhost"))
                    .port(getConfig().getInt("redis.port", 6379))
                    .password(getConfig().getString("redis.password", ""))
                    .build();

            // Subscribe to global events
            redis.subscribe("global-broadcast", message -> {
                Bukkit.broadcast(net.kyori.adventure.text.Component.text("[Global] " + message.data()));
            });
        } catch (Exception e) {
            getLogger().warning("Failed to connect to Redis: " + e.getMessage());
        }
    }

    private void initUtilities() {
        int chatMaxMessages = getConfig().getInt("rate-limits.chat.max-messages", 5);
        int chatWindowSeconds = getConfig().getInt("rate-limits.chat.window-seconds", 10);

        teleportCooldowns = CooldownManager.create("teleport");
        payCooldowns = CooldownManager.create("pay");
        chatLimiter = RateLimiter.create("chat", chatMaxMessages, chatWindowSeconds, TimeUnit.SECONDS);
    }

    private void initEconomy() {
        BigDecimal startingBalance = BigDecimal.valueOf(
                getConfig().getDouble("economy.starting-balance", 1000.0));
        economyProvider = new TemplateEconomyProvider(database, startingBalance);
        IonEconomy.setProvider(economyProvider);
    }

    private void registerCommands() {
        ShopGui shopGui = new ShopGui(getConfig().getConfigurationSection("shop"));
        com.example.iontemplate.gui.MainMenuGui mainMenu = new com.example.iontemplate.gui.MainMenuGui(this);

        getCommand("menu").setExecutor(new MenuCommand(mainMenu));
        getCommand("spawn").setExecutor(new SpawnCommand(
                teleportCooldowns, spawnLocation, getConfig().getInt("cooldowns.teleport", 30)));
        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("pay").setExecutor(new PayCommand(
                payCooldowns, getConfig().getInt("cooldowns.pay", 5)));
        getCommand("shop").setExecutor(new ShopCommand(shopGui));
        getCommand("stats").setExecutor(new StatsCommand());
        getCommand("scoreboard").setExecutor(new ScoreboardCommand(scoreboardManager));
        getCommand("warp").setExecutor(new com.example.iontemplate.command.WarpCommand(database));
        getCommand("leaderboard").setExecutor(new com.example.iontemplate.command.LeaderboardCommand());
        getCommand("npc").setExecutor(new NpcCommand(this));

        EcoCommand ecoCommand = new EcoCommand();
        getCommand("eco").setExecutor(ecoCommand);
        getCommand("eco").setTabCompleter(ecoCommand);
    }

    private void loadSettings(FileConfiguration cfg) {
        // Load spawn location
        String worldName = cfg.getString("spawn.world", "world");
        double x = cfg.getDouble("spawn.x", 0.5);
        double y = cfg.getDouble("spawn.y", 64.0);
        double z = cfg.getDouble("spawn.z", 0.5);
        float yaw = (float) cfg.getDouble("spawn.yaw", 0.0);
        float pitch = (float) cfg.getDouble("spawn.pitch", 0.0);

        var world = Bukkit.getWorld(worldName);
        if (world != null) {
            spawnLocation = new Location(world, x, y, z, yaw, pitch);
        }

        getLogger().info("Config reloaded!");
    }

    // Public getters for other classes
    public IonDatabase getDatabase() {
        return database;
    }

    public IonRedis getRedis() {
        return redis;
    }

    public TemplateEconomyProvider getEconomyProvider() {
        return economyProvider;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    // Stats helpers
    public int getPlayerKills(UUID uuid) {
        try {
            PlayerData data = database.find(PlayerData.class, uuid);
            return data != null ? data.getKills() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getPlayerDeaths(UUID uuid) {
        try {
            PlayerData data = database.find(PlayerData.class, uuid);
            return data != null ? data.getDeaths() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public double getPlayerKDR(UUID uuid) {
        try {
            PlayerData data = database.find(PlayerData.class, uuid);
            return data != null ? data.getKDR() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
