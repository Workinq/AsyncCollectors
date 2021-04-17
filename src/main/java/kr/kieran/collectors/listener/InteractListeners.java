package kr.kieran.collectors.listener;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.gui.CollectorGui;
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
            player.sendMessage(Color.color(plugin.getConfig().getString("messages.chunk-locked")));
            event.setCancelled(true);
            return;
        }

        // Collector
        Collector collector = plugin.getCollectorManager().getByLocation(block.getLocation());
        if (collector == null) return;

        // Cancel
        event.setCancelled(true);

        // Open
        CollectorGui gui = new CollectorGui(plugin, plugin.getConfig().getInt("collector.gui.rows"), Color.color(plugin.getConfig().getString("collector.gui.name")), 20L, collector);
        gui.open(player);
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
        if (block.getType() != Material.getMaterial(plugin.getConfig().getString("collector.item.material"))) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!plugin.getChunkManager().canUseChunk(chunkId))
        {
            player.sendMessage(Color.color(plugin.getConfig().getString("messages.chunk-locked")));
            event.setCancelled(true);
            return;
        }

        // Collector
        Collector collector = plugin.getCollectorManager().getByLocation(block.getLocation());
        if (collector == null) return;
        if (collector.isEmpty())
        {
            player.sendMessage(Color.color(plugin.getConfig().getString("messages.collector-empty")));
            event.setCancelled(true);
            return;
        }

        // Cancel
        event.setCancelled(true);

        // Sell collector contents
        double total = 0.0d, finalTotal;
        for (Map.Entry<Material, Integer> entry : collector.getContents().entrySet())
        {
            Material material = entry.getKey();
            int amount = entry.getValue();
            if (!plugin.getConfig().isSet("prices." + material.name())) continue;

            double price = plugin.getConfig().getDouble("prices." + material.name());
            total += price * amount;
        }
        finalTotal = total;

        // Clear
        collector.clearContents();

        // Money
        plugin.getMoneyManager().queueMoney(uniqueId, finalTotal);

        // Save & Inform
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getCollectorManager().save(collector, after -> {
                // Deposit
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getMoneyManager().execute(uniqueId));

                // Inform
                player.sendMessage(Color.color(plugin.getConfig().getString("messages.contents-sold").replace("%total%", String.format("%,.1f", finalTotal))));
            });
        });
    }

}
