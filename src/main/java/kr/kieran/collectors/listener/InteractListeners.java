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
import kr.kieran.collectors.gui.MenuGui;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

public class InteractListeners implements Listener
{

    private final CollectorsPlugin plugin;

    public InteractListeners(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    // LISTENER: COLLECTOR OPEN
    @EventHandler(priority = EventPriority.NORMAL)
    public void open(PlayerInteractEvent event)
    {
        // Args
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;
        long chunkId = block.getChunk().getChunkKey();

        // Check
        if (player.isSneaking()) return;
        if (block.getType() != Material.getMaterial(plugin.getConfig().getString("collector.item.material"))) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!plugin.getChunkManager().canUseChunk(chunkId))
        {
            String message = plugin.getConfig().getString("messages.chunk-locked");
            if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
            event.setCancelled(true);
            return;
        }

        // Collector
        Collector collector = plugin.getCollectorManager().getByLocation(block.getLocation());
        if (collector == null) return;

        // Cancel
        event.setCancelled(true);

        // Open
        MenuGui menu = new MenuGui(plugin, collector);
        menu.open(player);
    }

    // LISTENER: COLLECTOR SELL
    @EventHandler(priority = EventPriority.NORMAL)
    public void sell(PlayerInteractEvent event)
    {
        // Args
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        Block block = event.getClickedBlock();
        if (block == null) return;
        long chunkId = block.getChunk().getChunkKey();

        // Check
        if (!player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (block.getType() != Material.getMaterial(plugin.getConfig().getString("collector.item.material"))) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!plugin.getChunkManager().canUseChunk(chunkId))
        {
            String message = plugin.getConfig().getString("messages.chunk-locked");
            if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
            event.setCancelled(true);
            return;
        }

        // Collector
        Collector collector = plugin.getCollectorManager().getByLocation(block.getLocation());
        if (collector == null) return;
        if (collector.isEmpty())
        {
            String message = plugin.getConfig().getString("messages.collector-empty");
            if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message));
            event.setCancelled(true);
            return;
        }

        // Cancel
        event.setCancelled(true);

        // Money
        double total = plugin.getCollectorManager().sell(collector);
        plugin.getMoneyManager().queueMoney(uniqueId, total);

        // Save & Inform
        plugin.newChain()
                .async(() -> plugin.getCollectorManager().save(collector))
                .sync(() -> {
                    // Deposit
                    plugin.getMoneyManager().execute(uniqueId);

                    // Inform
                    String message = plugin.getConfig().getString("messages.contents-sold");
                    if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message.replace("%total%", String.format("%,.1f", total))));
                })
                .execute();
    }

}
