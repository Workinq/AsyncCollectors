package kr.kieran.collectors.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import kr.kieran.collectors.CollectorsPlugin;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MoneyManager
{

    private final CollectorsPlugin plugin;
    private final Cache<UUID, Double> owedMoney;

    public MoneyManager(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
        this.owedMoney = CacheBuilder.newBuilder().expireAfterWrite(plugin.getConfig().getInt("collector.sell-timeout"), TimeUnit.SECONDS).build();
    }

    public void queueMoney(UUID uniqueId, double amount)
    {
        try
        {
            this.owedMoney.put(uniqueId, this.owedMoney.get(uniqueId, () -> 0.0d) + amount);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException("Failed to queue money owed for '" + uniqueId.toString() + "': " + e.getMessage());
        }
    }

    public void execute(UUID uniqueId)
    {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(uniqueId);
        if (!owedMoney.asMap().containsKey(uniqueId) || !player.hasPlayedBefore()) return;
        Double amount = this.owedMoney.getIfPresent(uniqueId);
        if (amount == null) return;
        plugin.getEconomy().depositPlayer(player, amount);
        this.owedMoney.invalidate(uniqueId);
    }

    // DISABLE
    public void disable()
    {
        this.owedMoney.asMap().keySet().forEach(this::execute);
        this.owedMoney.invalidateAll();
    }

}
