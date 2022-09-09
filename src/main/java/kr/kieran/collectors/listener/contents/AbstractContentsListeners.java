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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public abstract class AbstractContentsListeners implements Listener
{

    public final CollectorsPlugin plugin;

    public AbstractContentsListeners(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void spawnerEvent(Cancellable cancellable, EntityType entityType, Entity entity, Location location)
    {
        // Args
        long chunkId = location.getChunk().getChunkKey();

        // If the chunk isn't tracked cancel the event
        if (plugin.getChunkManager().isChunkBusy(chunkId))
        {
            cancellable.setCancelled(true);
            return;
        }

        // Get the collector, if any, from the chunk
        Collector collector = plugin.getCollectorManager().getById(chunkId);
        if (collector == null) return;
        cancellable.setCancelled(true);

        // Fill collector
        this.populateCollector(chunkId, collector, this.getItemsFromMob(entityType, location, entity));
    }

    protected abstract Collection<ItemStack> getItemsFromMob(EntityType entityType, Location location, Entity entity);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void spawn(EntitySpawnEvent event)
    {
        // Args
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        long chunkId = entity.getChunk().getChunkKey();

        if (plugin.getChunkManager().isChunkBusy(chunkId))
        {
            event.setCancelled(true);
            return;
        }

        Collector collector = plugin.getCollectorManager().getById(chunkId);
        if (collector == null) return;

        event.setCancelled(true);
        // TODO: Creature spawning isn't being cancelled

        // Fill collector
        this.populateCollector(chunkId, collector, this.getItemsFromMob(entity.getType(), event.getLocation(), entity));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void death(EntityDeathEvent event)
    {
        // Args
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        long chunkId = entity.getChunk().getChunkKey();

        // If the chunk is busy don't alter any behaviour
        if (plugin.getChunkManager().isChunkBusy(chunkId)) return;

        // Get the collector, if any, from the chunk
        Collector collector = plugin.getCollectorManager().getById(chunkId);
        if (collector == null) return;

        // Add to the collector
        this.populateCollector(chunkId, collector, event.getDrops());
        event.getDrops().clear();
        event.setDroppedExp(0);

        // TODO: Make collectors store EXP too
    }

    private void populateCollector(long chunkId, Collector collector, Collection<ItemStack> items)
    {
        for (ItemStack item : items)
        {
            collector.setMaterialAmount(item.getType(), collector.getMaterialAmount(item.getType()) + item.getAmount());
        }

        // Save the collector items
        plugin.getSaveTask().queueSave(chunkId);
    }

}
