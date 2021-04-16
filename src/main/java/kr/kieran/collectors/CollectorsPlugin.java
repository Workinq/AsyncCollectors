package kr.kieran.collectors;

import kr.kieran.collectors.command.CollectorCommand;
import kr.kieran.collectors.database.Database;
import kr.kieran.collectors.listener.ContentsListeners;
import kr.kieran.collectors.listener.InteractListeners;
import kr.kieran.collectors.listener.LoadingListeners;
import kr.kieran.collectors.listener.PlacementListeners;
import kr.kieran.collectors.manager.ChunkManager;
import kr.kieran.collectors.manager.CollectorManager;
import kr.kieran.collectors.manager.MoneyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CollectorsPlugin extends JavaPlugin
{

    // INSTANCE & CONSTRUCT
    private static CollectorsPlugin instance;
    public static CollectorsPlugin get() { return instance; }
    public CollectorsPlugin() { instance = this; }

    // DATABASE MANAGER
    private Database database;
    public Database getDatabase() { return database; }

    // COLLECTOR MANAGER
    private CollectorManager collectorManager;
    public CollectorManager getCollectorManager() { return collectorManager; }

    // CHUNK MANAGER
    private ChunkManager chunkManager;
    public ChunkManager getChunkManager() { return chunkManager; }

    // MONEY MANAGER
    private MoneyManager moneyManager;
    public MoneyManager getMoneyManager() { return moneyManager; }

    // OVERRIDE
    @Override public void onLoad() { this.saveDefaultConfig(); }

    @Override
    public void onEnable()
    {
        // Economy
        this.setupEconomy();

        // Managers
        this.registerManagers();
        this.registerCommands();
        this.registerListeners();
    }

    @Override
    public void onDisable()
    {
        this.moneyManager.disable();
        this.collectorManager.disable();
        this.chunkManager.disable();
        this.database.disable();
    }

    // REGISTER: MANAGERS
    private void registerManagers()
    {
        this.database = new Database(this);
        this.collectorManager = new CollectorManager(this);
        this.chunkManager = new ChunkManager();
        this.moneyManager = new MoneyManager(this);
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

    // ECONOMY
    private Economy economy;
    public Economy getEconomy() { return economy; }
    private void setupEconomy()
    {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> provider = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) return;
        this.economy = provider.getProvider();
    }

}
