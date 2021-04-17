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

package kr.kieran.collectors.listener;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.model.Collector;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import java.util.Collection;
import java.util.Random;

public class ContentsListeners implements Listener
{

    private final CollectorsPlugin plugin;

    public ContentsListeners(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void creature(CreatureSpawnEvent event)
    {
        // Check the creature came from a spawner
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) return;

        // Args
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
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
        event.setCancelled(true);

        // Calculate the mob drops for the entity
        LootTable lootTable = LootTables.valueOf(type.name()).getLootTable();
        Collection<ItemStack> drops = lootTable.populateLoot(new Random(), new LootContext.Builder(entity.getLocation()).lootedEntity(entity).build());

        // Add to the collector
        for (ItemStack drop : drops)
        {
            Material material = drop.getType();
            collector.setMaterialAmount(material, collector.getMaterialAmount(material) + drop.getAmount());
        }

        // Save
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> plugin.getCollectorManager().save(collector, after -> {}));
    }

}
