package kr.kieran.collectors.database;

import com.zaxxer.hikari.HikariDataSource;
import kr.kieran.collectors.CollectorsPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class Database
{

    private final CollectorsPlugin plugin;
    private final HikariDataSource dataSource = new HikariDataSource();

    public Database(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
        this.registerProperties();
        this.setupTables();
    }

    /**
     * Register all necessary data source properties for Hikari
     * to connect to the database successfully and optimize settings
     * to allow for faster queries and updates.
     */
    private void registerProperties()
    {
        // Log
        plugin.getLogger().log(Level.INFO, "Setting up database environment...");

        // Driver & pool size
        this.dataSource.setMaximumPoolSize(10);
        this.dataSource.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");

        // Credentials
        this.dataSource.addDataSourceProperty("serverName", plugin.getConfig().getString("mysql.host"));
        this.dataSource.addDataSourceProperty("port", plugin.getConfig().getString("mysql.port"));
        this.dataSource.addDataSourceProperty("databaseName", plugin.getConfig().getString("mysql.database"));
        this.dataSource.addDataSourceProperty("user", plugin.getConfig().getString("mysql.user"));
        this.dataSource.addDataSourceProperty("password", plugin.getConfig().getString("mysql.password"));

        // Properties
        this.dataSource.addDataSourceProperty("cachePrepStmts", true);
        this.dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        this.dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        this.dataSource.addDataSourceProperty("useServerPrepStmts", true);
        this.dataSource.addDataSourceProperty("rewriteBatchedStatements", true);
    }

    /**
     * Setup the required tables synchronously to ensure they're
     * available before the plugin attempts to use them.
     */
    private void setupTables()
    {
        // Log
        plugin.getLogger().log(Level.INFO, "Executing preliminary database queries...");

        // Setup
        try (
                Connection connection = this.getConnection();
                PreparedStatement collectors = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `collectors_collectors` (`collector_id` BIGINT NOT NULL, `mode` VARCHAR(9) NOT NULL DEFAULT 'ALL', `location` VARCHAR(255) NOT NULL, PRIMARY KEY (`collector_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
                PreparedStatement contents = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `collectors_contents` (`collector_id` BIGINT NOT NULL, `material` VARCHAR(255) NOT NULL, `amount` INT NOT NULL, PRIMARY KEY (`collector_id`, `material`), FOREIGN KEY (`collector_id`) REFERENCES `collectors_collectors` (`collector_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;")
        )
        {
            // Execute
            collectors.executeUpdate();
            contents.executeUpdate();
        }
        catch (SQLException e)
        {
            // Log
            plugin.getLogger().log(Level.SEVERE, "A sql exception occurred: " + e.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Log
        plugin.getLogger().log(Level.INFO, "Finished executing preliminary database queries.");
    }

    /**
     * Retrieve a connection from the Hikari connection pool
     *
     * @return a connection to execute queries
     * @throws SQLException if something went wrong throw an exception
     */
    public Connection getConnection() throws SQLException { return this.dataSource.getConnection(); }
    public void disable() { this.dataSource.close(); }

}
