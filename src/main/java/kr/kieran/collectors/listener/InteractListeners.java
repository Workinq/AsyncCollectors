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
import kr.kieran.collectors.util.Text;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
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
        try
        {
            Map<String, Object> attributes = this.isActionValid(event);
            if (attributes.getOrDefault("success", Boolean.FALSE) == Boolean.TRUE)
            {
                Player player = (Player) attributes.get("player");
                if (player.isSneaking()) return;

                MenuGui menu = new MenuGui(plugin, (Collector) attributes.get("collector"));
                menu.open(player);
            }
        }
        catch (Exception ignored)
        {
        }
    }

    // LISTENER: COLLECTOR SELL
    @EventHandler(priority = EventPriority.NORMAL)
    public void sell(PlayerInteractEvent event)
    {
        try
        {
            Map<String, Object> attributes = this.isActionValid(event);
            if (attributes.getOrDefault("success", Boolean.FALSE) == Boolean.TRUE)
            {
                Player player = (Player) attributes.get("player");
                if (!player.isSneaking()) return;
                UUID uniqueId = player.getUniqueId();
                long chunkId = (long) attributes.get("chunkId");

                // Collector
                Collector collector = (Collector) attributes.get("collector");
                if (collector.isEmpty())
                {
                    Text.message(player, plugin.getConfig().getString("messages.collector-empty"));
                    return;
                }

                // Money
                double total = plugin.getCollectorManager().sell(collector);
                plugin.getMoneyManager().queueMoney(uniqueId, total);

                // Save & Inform
                plugin.newChain()
                        .async(() -> plugin.getCollectorManager().save(chunkId))
                        .sync(() -> {
                            // Deposit
                            plugin.getMoneyManager().execute(uniqueId);

                            // Inform
                            Text.message(player, plugin.getConfig().getString("messages.contents-sold"), total);
                        })
                        .execute();
            }
        }
        catch (Exception ignored)
        {
        }
    }

    private Map<String, Object> isActionValid(PlayerInteractEvent event) throws Exception
    {
        // TODO: What the fuck am I doing here? Change this back to something that makes sense...

        // Event attributes
        Map<String, Object> attributes = new HashMap<>();

        // Args
        Player player = event.getPlayer();
        attributes.put("player", player);
        Block block = event.getClickedBlock();
        if (block == null) throw new Exception("Block is null");
        attributes.put("block", block);
        long chunkId = block.getChunk().getChunkKey();
        attributes.put("chunkId", chunkId);

        // Check
        if (block.getType() != Material.getMaterial(plugin.getConfig().getString("collector.item.material"))) throw new Exception("Incorrect collector material");
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) throw new Exception("Incorrect event action");
        if (plugin.getChunkManager().isChunkBusy(chunkId))
        {
            Text.message(player, plugin.getConfig().getString("messages.chunk-locked"));
            event.setCancelled(true);
            throw new Exception("Chunk is busy");
        }

        // Collector
        Collector collector = plugin.getCollectorManager().getByLocation(block.getLocation());
        if (collector == null) throw new Exception("Collector is null");
        attributes.put("collector", collector);

        // All checks succeeded, cancel event and return
        event.setCancelled(true);
        attributes.put("success", Boolean.TRUE);
        return attributes;
    }

}
