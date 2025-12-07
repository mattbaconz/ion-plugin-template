package com.example.iontemplate.data;

import com.ionapi.database.annotations.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

@Table("warps")
public class Warp {
    
    @PrimaryKey
    private UUID id;
    
    @Column(name = "name", nullable = false, unique = true, length = 32)
    private String name;
    
    @Column(name = "world", nullable = false)
    private String world;
    
    @Column(name = "x", nullable = false)
    private double x;
    
    @Column(name = "y", nullable = false)
    private double y;
    
    @Column(name = "z", nullable = false)
    private double z;
    
    @Column(name = "yaw")
    private float yaw;
    
    @Column(name = "pitch")
    private float pitch;
    
    @Column(name = "created_by")
    private UUID createdBy;
    
    @Column(name = "created_at")
    private long createdAt;
    
    public Warp() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
    }
    
    public Warp(String name, Location location, UUID createdBy) {
        this();
        this.name = name;
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.createdBy = createdBy;
    }
    
    public Location toLocation() {
        var world = Bukkit.getWorld(this.world);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    
    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
