package kr.kieran.collectors.listener;

import kr.kieran.collectors.CollectorsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListeners implements Listener
{

    private final CollectorsPlugin plugin;

    public InteractListeners(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    // LISTENER: COLLECTOR INTERACT
    @EventHandler(ignoreCancelled = true)
    public void collectorInteract(PlayerInteractEvent event)
    {
    }

}
