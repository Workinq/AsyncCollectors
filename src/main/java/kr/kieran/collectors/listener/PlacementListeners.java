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
import kr.kieran.collectors.util.Color;
import kr.kieran.collectors.util.SerializationUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlacementListeners implements Listener
{

    private final CollectorsPlugin plugin;

    public PlacementListeners(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    // LISTENER: COLLECTOR CREATE
    @EventHandler(ignoreCancelled = true)
    public void collectorPlace(BlockPlaceEvent event)
    {
        // Args
        Player player = event.getPlayer();
        Location location = event.getBlockPlaced().getLocation();
        Chunk chunk = location.getChunk();
        long chunkId = chunk.getChunkKey();

        // Check
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.getMaterial(plugin.getConfig().getString("collector.item.material"))) return;
        if (item.getDurability() != plugin.getConfig().getInt("collectors.item.data")) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.getDisplayName().equals(Color.color(plugin.getConfig().getString("collector.item.name")))) return;
        if (!plugin.getChunkManager().canUseChunk(chunkId))
        {
            String message = plugin.getConfig().getString("messages.chunk-locked");
            if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
            event.setCancelled(true);
            return;
        }

        // Exists
        if (plugin.getCollectorManager().exists(chunkId))
        {
            String message = plugin.getConfig().getString("messages.collector-exists");
            if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
            event.setCancelled(true);
            return;
        }

        // Lock
        plugin.getChunkManager().lock(chunkId);

        // Create & Unlock
        plugin.newChain()
                .asyncFirst(() -> plugin.getCollectorManager().create(chunkId, SerializationUtil.serialize(location)))
                .abortIf(collector -> {
                    // Check
                    if (collector == null)
                    {
                        String message = plugin.getConfig().getString("messages.creation-field");
                        if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
                        return true;
                    }
                    return false;
                })
                .sync(() -> {
                    // Unlock
                    plugin.getChunkManager().unlock(chunkId);

                    // Inform
                    String message = plugin.getConfig().getString("messages.placed-collector");
                    if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message.replace("%x%", String.format("%,d", chunk.getX())).replace("%z%", String.format("%,d", chunk.getZ()))));
                })
                .execute();
    }

    // LISTENER: COLLECTOR BREAK
    @EventHandler(ignoreCancelled = true)
    public void collectorBreak(BlockBreakEvent event)
    {
        // Args
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        long chunkId = block.getChunk().getChunkKey();

        // Check
        if (block.getType() != Material.getMaterial(plugin.getConfig().getString("collector.item.material"))) return;
        if (!plugin.getChunkManager().canUseChunk(chunkId))
        {
            String message = plugin.getConfig().getString("messages.chunk-locked");
            if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
            event.setCancelled(true);
            return;
        }

        // Check
        Collector collector = plugin.getCollectorManager().getByLocation(location);
        if (collector == null) return;

        // Lock & Cancel
        plugin.getChunkManager().lock(chunkId);
        event.setCancelled(true);

        // Destroy & Unlock
        plugin.newChain()
                .sync(() -> {
                    // Inform
                    String message = plugin.getConfig().getString("messages.removing-collector");
                    if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));

                    // Pay
                    double total = plugin.getCollectorManager().sell(collector);
                    plugin.getMoneyManager().queueMoney(player.getUniqueId(), total);
                })
                .asyncFirst(() -> plugin.getCollectorManager().delete(collector))
                .abortIf(toDelete -> {
                    // Check
                    if (toDelete == null)
                    {
                        String message = plugin.getConfig().getString("messages.deletion-fail");
                        if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
                        return true;
                    }
                    return false;
                })
                .sync(() -> {
                    // Set & Drop
                    location.getBlock().setType(Material.AIR);
                    location.getWorld().dropItemNaturally(location, plugin.getCollector(1));

                    // Unlock
                    plugin.getChunkManager().unlock(chunkId);

                    // Deposit & Inform
                    plugin.getMoneyManager().execute(player.getUniqueId());
                    String message = plugin.getConfig().getString("messages.destroyed-collector");
                    if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
                })
                .execute();
    }

}
