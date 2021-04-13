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
            player.sendMessage(Color.color(plugin.getConfig().getString("messages.chunk-locked")));
            event.setCancelled(true);
            return;
        }

        // Exists
        if (plugin.getCollectorManager().exists(chunkId))
        {
            player.sendMessage(Color.color(plugin.getConfig().getString("messages.collector-exists")));
            event.setCancelled(true);
            return;
        }

        // Lock
        plugin.getChunkManager().lock(chunkId);

        // Create & Unlock
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getCollectorManager().create(chunkId, SerializationUtil.serialize(location),after -> {
                plugin.getChunkManager().unlock(chunkId);
                player.sendMessage(Color.color(plugin.getConfig().getString("messages.placed-collector").replace("%x%", String.format("%,d", chunk.getX())).replace("%z%", String.format("%,d", chunk.getZ()))));
            });
        });
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
            player.sendMessage(Color.color(plugin.getConfig().getString("messages.chunk-locked")));
            event.setCancelled(true);
            return;
        }

        // Check
        Collector collector = plugin.getCollectorManager().getByLocation(location);
        if (collector == null) return;

        // Lock
        plugin.getChunkManager().lock(chunkId);

        // Destroy & Unlock
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getCollectorManager().delete(collector, after -> {
                plugin.getChunkManager().unlock(chunkId);
                player.sendMessage(Color.color(plugin.getConfig().getString("messages.destroyed-collector")));
            });
        });
    }

}
