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

package kr.kieran.collectors.listener.contents;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.model.Collector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractContentsListeners implements Listener
{

    public final CollectorsPlugin plugin;
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public AbstractContentsListeners(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void event(Cancellable cancellable, EntityType type, Location location)
    {
        // Args
        long chunkId = location.getChunk().getChunkKey();

        // If the chunk isn't tracked cancel the event
        if (!plugin.getChunkManager().canUseChunk(chunkId))
        {
            cancellable.setCancelled(true);
            return;
        }

        // Get the collector, if any, from the chunk
        Collector collector = plugin.getCollectorManager().getById(chunkId);
        if (collector == null) return;
        cancellable.setCancelled(true);

        // Calculate drop
        String path = "drops." + type.name();
        if (!plugin.getConfig().isSet(path)) return;
        Material drop = Material.getMaterial(plugin.getConfig().getString(path + ".material"));
        int amount = RANDOM.nextInt(plugin.getConfig().getInt(path + ".range.min"), plugin.getConfig().getInt(path + ".range.max"));

        // Add to collector
        collector.setMaterialAmount(drop, collector.getMaterialAmount(drop) + amount);

        // Save
        plugin.newChain()
                .async(() -> plugin.getCollectorManager().save(collector))
                .execute();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void death(EntityDeathEvent event)
    {
        // Args
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        long chunkId = entity.getChunk().getChunkKey();

        // If the chunk isn't tracked cancel the event
        if (!plugin.getChunkManager().canUseChunk(chunkId))
        {
            event.setCancelled(true);
            return;
        }

        // Get the collector, if any, from the chunk
        Collector collector = plugin.getCollectorManager().getById(chunkId);
        if (collector == null) return;

        // Add to the collector
        List<ItemStack> drops = event.getDrops();
        for (ItemStack drop : drops)
        {
            Material material = drop.getType();
            collector.setMaterialAmount(material, collector.getMaterialAmount(material) + drop.getAmount());
        }
        event.getDrops().clear();

        // Save
        plugin.newChain()
                .async(() -> plugin.getCollectorManager().save(collector))
                .execute();
    }

}
