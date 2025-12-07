package com.example.iontemplate.data;

import com.ionapi.database.annotations.*;
import java.math.BigDecimal;
import java.util.UUID;

@Table("player_balances")
@Cacheable(ttl = 30, maxSize = 1000)
public class PlayerBalance {
    
    @PrimaryKey
    private UUID uuid;
    
    @Column(nullable = false)
    private BigDecimal balance;
    
    public PlayerBalance() {}
    
    public PlayerBalance(UUID uuid, BigDecimal balance) {
        this.uuid = uuid;
        this.balance = balance;
    }
    
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
