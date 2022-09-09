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

import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import kr.kieran.collectors.CollectorsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public class OptimisedContentsListener extends AbstractContentsListeners
{

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public OptimisedContentsListener(CollectorsPlugin plugin)
    {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void spawner(PreSpawnerSpawnEvent event)
    {
        // TODO: Figure out how to get the LootTable with just EntityType
        this.spawnerEvent(event, event.getType(), null, event.getSpawnLocation());
    }

    @Override
    protected Collection<ItemStack> getItemsFromMob(EntityType entityType, Location location, Entity entity)
    {
        Collection<ItemStack> items = new ArrayList<>();

        // Calculate drop
        String path = "drops." + entityType.name();
        if (!plugin.getConfig().isSet(path)) return items;
        Material drop = Material.getMaterial(plugin.getConfig().getString(path + ".material"));
        int amount = RANDOM.nextInt(plugin.getConfig().getInt(path + ".range.min"), plugin.getConfig().getInt(path + ".range.max"));
        items.add(new ItemStack(drop, amount));
        // TODO: At the moment this only supports one item added at a time, make it so you can add multiple

        return items;
    }

}
