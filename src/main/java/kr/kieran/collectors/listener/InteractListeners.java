package kr.kieran.collectors.listener;

import kr.kieran.collectors.CollectorsPlugin;
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        // TODO: Fix check to prevent cancelling placing regular blocks
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

        // TODO: Open the collector menu
    }

    // LISTENER: COLLECTOR SELL
    @EventHandler(priority = EventPriority.NORMAL)
    public void sell(PlayerInteractEvent event)
    {
        // Args
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;
        long chunkId = block.getChunk().getChunkKey();

        // Check
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking()) return;
        // TODO: Fix check to prevent cancelling placing regular blocks
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

        // Sell collector contents
        double total = 0.0d, finalTotal;
        for (Map.Entry<Material, Integer> entry : collector.getContents().entrySet())
        {
            Material material = entry.getKey();
            int amount = entry.getValue();
            if (amount <= 0) continue;
            if (!plugin.getConfig().isSet("prices." + material.name())) continue;

            double price = plugin.getConfig().getDouble("prices." + material.name());
            total += price * amount;
        }
        finalTotal = total;

        // Clear
        collector.clearContents();

        // Save & Inform
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getCollectorManager().save(collector, after -> {
                // TODO: Give the player money using Vault
                player.sendMessage(Color.color(plugin.getConfig().getString("messages.contents-sold").replace("%total%", String.format("%,.2f", finalTotal))));
            });
        });
    }

}
