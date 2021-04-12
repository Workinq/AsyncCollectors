package kr.kieran.collectors;

import kr.kieran.collectors.command.CollectorCommand;
import kr.kieran.collectors.database.Database;
import kr.kieran.collectors.listener.ContentsListeners;
import kr.kieran.collectors.listener.InteractListeners;
import kr.kieran.collectors.listener.LoadingListeners;
import kr.kieran.collectors.listener.PlacementListeners;
import kr.kieran.collectors.manager.ChunkManager;
import kr.kieran.collectors.manager.CollectorManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CollectorsPlugin extends JavaPlugin
{

    // DATABASE MANAGER
    private Database database;
    public Database getDatabase() { return database; }

    // COLLECTOR MANAGER
    private CollectorManager collectorManager;
    public CollectorManager getCollectorManager() { return collectorManager; }

    // CHUNK MANAGER
    private ChunkManager chunkManager;
    public ChunkManager getChunkManager() { return chunkManager; }

    // OVERRIDE
    @Override public void onLoad() { this.saveDefaultConfig(); }

    @Override
    public void onEnable()
    {
        this.registerManagers();
        this.registerCommands();
        this.registerListeners();
    }

    @Override
    public void onDisable()
    {
        this.database.disable();
        this.chunkManager.disable();
        this.collectorManager.disable();
    }

    // REGISTER: MANAGERS
    private void registerManagers()
    {
        this.database = new Database(this);
        this.collectorManager = new CollectorManager(this);
        this.chunkManager = new ChunkManager();
    }

    // REGISTER: COMMANDS
    private void registerCommands()
    {
        this.getCommand("collector").setExecutor(new CollectorCommand(this));
    }

    // REGISTER: LISTENERS
    private void registerListeners()
    {
        this.getServer().getPluginManager().registerEvents(new LoadingListeners(this), this);
        this.getServer().getPluginManager().registerEvents(new PlacementListeners(this), this);
        this.getServer().getPluginManager().registerEvents(new InteractListeners(this), this);
        this.getServer().getPluginManager().registerEvents(new ContentsListeners(this), this);
    }

}
