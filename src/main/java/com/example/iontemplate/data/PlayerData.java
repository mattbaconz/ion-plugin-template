package com.example.iontemplate.data;

import com.ionapi.database.annotations.*;
import java.util.UUID;

@Table("player_data")
@Cacheable(ttl = 60, maxSize = 500)
public class PlayerData {
    
    @PrimaryKey
    private UUID uuid;
    
    @Column(name = "player_name", nullable = false, length = 16)
    private String name;
    
    @Column(defaultValue = "0")
    private int kills;
    
    @Column(defaultValue = "0")
    private int deaths;
    
    @Column(name = "play_time", defaultValue = "0")
    private long playTime;
    
    @Column(name = "last_login")
    private long lastLogin;
    
    @Column(name = "first_join")
    private long firstJoin;
    
    public PlayerData() {}
    
    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.kills = 0;
        this.deaths = 0;
        this.playTime = 0;
        this.lastLogin = System.currentTimeMillis();
        this.firstJoin = System.currentTimeMillis();
    }
    
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void addKill() { this.kills++; }
    
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void addDeath() { this.deaths++; }
    
    public long getPlayTime() { return playTime; }
    public void setPlayTime(long playTime) { this.playTime = playTime; }
    public void addPlayTime(long time) { this.playTime += time; }
    
    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
    
    public long getFirstJoin() { return firstJoin; }
    public void setFirstJoin(long firstJoin) { this.firstJoin = firstJoin; }
    
    public double getKDR() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }
}
