package com.example.iontemplate.economy;

import com.example.iontemplate.data.PlayerBalance;
import com.ionapi.database.IonDatabase;
import com.ionapi.economy.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TemplateEconomyProvider implements EconomyProvider {
    
    private final IonDatabase database;
    private final BigDecimal startingBalance;
    private final Currency defaultCurrency;
    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.00");
    
    public TemplateEconomyProvider(IonDatabase database, BigDecimal startingBalance) {
        this.database = database;
        this.startingBalance = startingBalance;
        this.defaultCurrency = new SimpleCurrency("coins", "Coins", "$");
    }
    
    @Override
    public String getName() {
        return "IonTemplateEconomy";
    }
    
    @Override
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }
    
    @Override
    public CompletableFuture<BigDecimal> getBalance(UUID uuid) {
        return database.findAsync(PlayerBalance.class, uuid)
            .thenApply(opt -> opt.map(PlayerBalance::getBalance).orElse(BigDecimal.ZERO));
    }
    
    @Override
    public CompletableFuture<BigDecimal> getBalance(UUID uuid, Currency currency) {
        return getBalance(uuid);
    }
    
    @Override
    public CompletableFuture<Boolean> has(UUID uuid, BigDecimal amount) {
        return getBalance(uuid).thenApply(balance -> balance.compareTo(amount) >= 0);
    }
    
    @Override
    public CompletableFuture<Boolean> has(UUID uuid, Currency currency, BigDecimal amount) {
        return has(uuid, amount);
    }
    
    @Override
    public CompletableFuture<TransactionResult> deposit(UUID uuid, BigDecimal amount) {
        return getOrCreateAccount(uuid).thenCompose(balance -> {
            BigDecimal newBalance = balance.getBalance().add(amount);
            balance.setBalance(newBalance);
            return database.saveAsync(balance)
                .thenApply(v -> TransactionResult.success(amount, newBalance));
        });
    }
    
    @Override
    public CompletableFuture<TransactionResult> deposit(UUID uuid, Currency currency, BigDecimal amount) {
        return deposit(uuid, amount);
    }
    
    @Override
    public CompletableFuture<TransactionResult> withdraw(UUID uuid, BigDecimal amount) {
        return getOrCreateAccount(uuid).thenCompose(balance -> {
            if (balance.getBalance().compareTo(amount) < 0) {
                return CompletableFuture.completedFuture(
                    TransactionResult.failure(TransactionResult.ResultType.INSUFFICIENT_FUNDS, 
                        balance.getBalance(), "Insufficient funds")
                );
            }
            BigDecimal newBalance = balance.getBalance().subtract(amount);
            balance.setBalance(newBalance);
            return database.saveAsync(balance)
                .thenApply(v -> TransactionResult.success(amount, newBalance));
        });
    }
    
    @Override
    public CompletableFuture<TransactionResult> withdraw(UUID uuid, Currency currency, BigDecimal amount) {
        return withdraw(uuid, amount);
    }
    
    @Override
    public CompletableFuture<TransactionResult> setBalance(UUID uuid, BigDecimal amount) {
        return getOrCreateAccount(uuid).thenCompose(balance -> {
            balance.setBalance(amount);
            return database.saveAsync(balance)
                .thenApply(v -> TransactionResult.success(amount, amount));
        });
    }
    
    @Override
    public CompletableFuture<TransactionResult> transfer(UUID from, UUID to, BigDecimal amount) {
        return withdraw(from, amount).thenCompose(result -> {
            if (!result.isSuccess()) {
                return CompletableFuture.completedFuture(result);
            }
            return deposit(to, amount);
        });
    }
    
    @Override
    public String format(BigDecimal amount) {
        return defaultCurrency.getSymbol() + FORMAT.format(amount);
    }
    
    @Override
    public String format(BigDecimal amount, Currency currency) {
        return currency.getSymbol() + FORMAT.format(amount);
    }
    
    @Override
    public EconomyTransaction transaction(UUID uuid) {
        return new SimpleTransaction(this, uuid);
    }
    
    private CompletableFuture<PlayerBalance> getOrCreateAccount(UUID uuid) {
        return database.findAsync(PlayerBalance.class, uuid)
            .thenCompose(opt -> {
                if (opt.isPresent()) {
                    return CompletableFuture.completedFuture(opt.get());
                }
                PlayerBalance newBalance = new PlayerBalance(uuid, startingBalance);
                return database.saveAsync(newBalance).thenApply(v -> newBalance);
            });
    }
    
    public CompletableFuture<Void> createAccount(UUID uuid) {
        return getOrCreateAccount(uuid).thenApply(b -> null);
    }
    
    // Simple Currency implementation
    private static class SimpleCurrency implements Currency {
        private final String id;
        private final String name;
        private final String symbol;
        
        SimpleCurrency(String id, String name, String symbol) {
            this.id = id;
            this.name = name;
            this.symbol = symbol;
        }
        
        @Override public String getId() { return id; }
        @Override public String getSingularName() { return name; }
        @Override public String getPluralName() { return name; }
        @Override public String getSymbol() { return symbol; }
        @Override public int getDecimalPlaces() { return 2; }
        @Override public String format(BigDecimal amount) { return symbol + FORMAT.format(amount); }
        @Override public boolean isDefault() { return true; }
    }
    
    // Simple transaction implementation
    private static class SimpleTransaction implements EconomyTransaction {
        private final TemplateEconomyProvider provider;
        private final UUID uuid;
        private BigDecimal amount = BigDecimal.ZERO;
        private boolean isWithdraw = false;
        @SuppressWarnings("unused")
        private String reason = ""; // Reserved for future transaction logging
        
        SimpleTransaction(TemplateEconomyProvider provider, UUID uuid) {
            this.provider = provider;
            this.uuid = uuid;
        }
        
        @Override
        public EconomyTransaction withdraw(BigDecimal amount) {
            this.amount = amount;
            this.isWithdraw = true;
            return this;
        }
        
        @Override
        public EconomyTransaction deposit(BigDecimal amount) {
            this.amount = amount;
            this.isWithdraw = false;
            return this;
        }
        
        @Override
        public EconomyTransaction currency(Currency currency) {
            return this;
        }
        
        @Override
        public EconomyTransaction reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        @Override
        public CompletableFuture<TransactionResult> commit() {
            if (isWithdraw) {
                return provider.withdraw(uuid, amount);
            } else {
                return provider.deposit(uuid, amount);
            }
        }
        
        @Override
        public CompletableFuture<TransactionResult> preview() {
            return provider.getBalance(uuid).thenApply(balance -> {
                BigDecimal newBalance = isWithdraw ? balance.subtract(amount) : balance.add(amount);
                if (isWithdraw && balance.compareTo(amount) < 0) {
                    return TransactionResult.failure(TransactionResult.ResultType.INSUFFICIENT_FUNDS, 
                        balance, "Insufficient funds");
                }
                return TransactionResult.success(amount, newBalance);
            });
        }
    }
}
