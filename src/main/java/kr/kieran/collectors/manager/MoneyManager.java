/*
 * This file is part of AsyncCollectors, licensed under the MIT License.
 *
 * Copyright (c) Workinq (Kieraaaan) <kieran@kieraaan.me>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
        Double amount = owedMoney.getIfPresent(uniqueId);
        if (amount == null) return;
        plugin.getEconomy().depositPlayer(player, amount);
        owedMoney.invalidate(uniqueId);
    }

    public void pay(UUID uniqueId, double amount)
    {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(uniqueId);
        if (!player.hasPlayedBefore()) return;
        plugin.getEconomy().depositPlayer(player, amount);
    }

    // DISABLE
    public void disable()
    {
        owedMoney.asMap().keySet().forEach(this::execute);
        owedMoney.invalidateAll();
    }

}
