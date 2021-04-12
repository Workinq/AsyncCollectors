package kr.kieran.collectors.listener;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.model.Collector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class LoadingListeners implements Listener
{

    private final CollectorsPlugin plugin;

    public LoadingListeners(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    // LISTENER: COLLECTOR LOAD
    @EventHandler
    public void chunkLoad(ChunkLoadEvent event)
    {
        long chunkId = event.getChunk().getChunkKey();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> plugin.getCollectorManager().load(chunkId, collector -> plugin.getChunkManager().track(chunkId)));
    }

    // LISTENER: COLLECTOR UNLOAD
    @EventHandler
    public void chunkUnload(ChunkUnloadEvent event)
    {
        // Args
        long chunkId = event.getChunk().getChunkKey();
        Collector collector = plugin.getCollectorManager().getById(chunkId);
        if (collector == null)
        {
            plugin.getCollectorManager().invalidate(chunkId);
            return;
        }

        // Save & unload
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> plugin.getCollectorManager().save(collector, after -> plugin.getCollectorManager().invalidate(chunkId)));
    }

}
