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

package kr.kieran.collectors.manager;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.model.Collector;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class CollectorManager
{

    private final CollectorsPlugin plugin;

    // CACHE
    private final ConcurrentMap<Long, Collector> cache = new ConcurrentHashMap<>();  // COLLECTORS

    /**
     * Get the collector from the cache using the chunk id.
     *
     * @param chunkId the chunk key to get a collector from
     * @return the collector from the cache or {@code null} if not present
     */
    public Collector getById(long chunkId)
    {
        return cache.getOrDefault(chunkId, null);
    }

    /**
     * Get the collector from the cache using its location.
     *
     * @param location the location of the collector
     * @return the collector from the cache or {@code null} if not present
     */
    public Collector getByLocation(Location location)
    {
        for (Collector collector : cache.values())
        {
            if (collector.getLocation().equals(location)) return collector;
        }
        return null;
    }

    /**
     * A shortcut to {@link CollectorManager#exists(long, boolean)} by
     * setting lookup to false without the need for an extra parameter.
     *
     * @param chunkId the id of the player to check for
     * @return true if a model exists with the {@code id} or false otherwise
     */
    public boolean exists(long chunkId)
    {
        return this.exists(chunkId, false);
    }

    /**
     * Check if a model exists in the cache with the identifier provided.
     *
     * @param chunkId the id of the player to check for
     * @return true if a collector exists with the {@code id} or otherwise
     */
    public boolean exists(long chunkId, boolean lookup)
    {
        boolean exists = cache.containsKey(chunkId);
        if (exists) return true;
        if (lookup)
        {
            try (
                    Connection connection = plugin.getDatabase().getConnection();
                    PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM `collectors_collectors` WHERE `collector_id` = ? LIMIT 1;")
            )
            {
                // Set
                statement.setLong(1, chunkId);

                // Results
                ResultSet result = statement.executeQuery();
                return result.next();
            }
            catch (SQLException e)
            {
                plugin.getLogger().log(Level.SEVERE, "A sql exception occurred (exists): " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Remove a model from the cache with the given identifier.
     *
     * @param chunkId the id to invalidate from the cache
     */
    public void invalidate(long chunkId)
    {
        cache.remove(chunkId);
    }

    // CONSTRUCT
    public CollectorManager(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Load a collector from the database using the chunk key as the
     * primary key. If a record doesn't exist {@link CollectorManager#create(long, Location)}
     * will be called to insert a new one into the database.
     *
     * @param chunkId the key for the chunk the collector is placed in
     * @return the collector after being loaded
     */
    public Collector load(long chunkId)
    {
        try (
                Connection connection = plugin.getDatabase().getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM `collectors_collectors` WHERE `collector_id` = ? LIMIT 1;")
        )
        {
            // Set
            statement.setLong(1, chunkId);

            // Results
            ResultSet result = statement.executeQuery();
            if (!result.next()) return null;

            // Variables
            Collector.Mode mode = Collector.Mode.valueOf(result.getString("mode"));
            Map<Material, Integer> contents = new HashMap<>();
            try (PreparedStatement contentsStatement = connection.prepareStatement("SELECT * FROM `collectors_contents` WHERE `collector_id` = ?;"))
            {
                // Set
                contentsStatement.setLong(1, chunkId);

                // Results
                ResultSet contentsResult = contentsStatement.executeQuery();
                while (contentsResult.next())
                {
                    // Args
                    Material material = Material.valueOf(contentsResult.getString("material"));
                    int amount = contentsResult.getInt("amount");

                    // Add
                    contents.put(material, amount);
                }
            }
            Location location = new Location(
                    plugin.getServer().getWorld(result.getString("world")),
                    result.getInt("block_x"),
                    result.getInt("block_y"),
                    result.getInt("block_z")
            );

            // Collector
            Collector collector = new Collector(chunkId, contents, mode, location);

            // Cache
            cache.put(chunkId, collector);
            return collector;
        }
        catch (SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, "A sql exception occurred (load): " + e.getMessage());
            return null;
        }
    }

    /**
     * Create a new collector at the given location
     *
     * @param chunkId  the generated chunk id
     * @param location the location of the collector to be created
     * @return the collector after being created
     */
    public Collector create(long chunkId, Location location)
    {
        try (
                Connection connection = plugin.getDatabase().getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO `collectors_collectors` (`collector_id`, `world`, `block_x`, `block_y`, `block_z`) VALUES (?, ?, ?, ?, ?);")
        )
        {
            // Set
            statement.setLong(1, chunkId);
            statement.setString(2, location.getWorld().getName());
            statement.setInt(3, location.getBlockX());
            statement.setInt(4, location.getBlockY());
            statement.setInt(5, location.getBlockZ());

            // Update
            statement.executeUpdate();

            // Collector
            Collector collector = new Collector(chunkId, new HashMap<>(), Collector.Mode.ALL, location);

            // Save
            cache.put(chunkId, collector);
            return collector;
        }
        catch (SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, "A sql exception occurred (create): " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete the collector from the database using the chunk id as the
     * identifier for it. You must remove the collector from the cache
     * using the same id using {@link CollectorManager#invalidate(long)}
     * to prevent any memory leaks as this method will not remove it in
     * case the object needs to be used elsewhere.
     *
     * @param chunkId the chunkId of the collector to be removed from the database
     */
    public Collector delete(long chunkId)
    {
        try (
                Connection connection = plugin.getDatabase().getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM `collectors_collectors` WHERE `collector_id` = ?;")
        )
        {
            // Set
            statement.setLong(1, chunkId);

            // Delete
            statement.executeUpdate();

            // Cache
            return cache.remove(chunkId);
        }
        catch (SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, "A sql exception occurred (delete): " + e.getMessage());
            return null;
        }
    }

    /**
     * Save a collector to the database.
     *
     * @param chunkId the chunkId of the collector to save to the database
     * @return the collector after being saved
     */
    public Collector save(long chunkId)
    {
        return this.save(chunkId, true);
    }

    /**
     * Save a collector to the database and, if {@code saveContents} is set to {@code true}
     * save its contents too.
     *
     * @param chunkId the chunkId of the collector to save to the database
     * @param saveContents whether to save the collector's contents to the database
     * @return the collector after being saved
     */
    public Collector save(long chunkId, boolean saveContents)
    {
        // Fetch & lock
        Collector collector = cache.get(chunkId);
        if (collector == null) throw new RuntimeException("Collector at '" + chunkId + "' is null");
        collector.lock();

        try (
                Connection connection = plugin.getDatabase().getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE `collectors_collectors` SET `mode` = ? WHERE `collector_id` = ?;")
        )
        {
            // Set
            statement.setString(1, collector.getMode().name());
            statement.setLong(2, chunkId);

            // Update
            statement.executeUpdate();

            // Contents
            if (saveContents)
            {
                // TODO: Find a way to execute contents update as batch statement
                collector.getAbsoluteContents().forEach((material, integer) -> this.saveContents(connection, chunkId, material, integer));
            }

            return collector;
        }
        catch (SQLException e)
        {
            plugin.getLogger().log(Level.SEVERE, "A sql exception occurred (save): " + e.getMessage());
            return null;
        }
        finally
        {
            collector.unlock();
        }
    }

    /**
     * Save the contents of the provided collector to the database. This method will
     * determine whether or not to delete an entry from the database if the amount is
     * less than or equal to 0. If the item isn't being deleted, first the method will
     * try to update an entry in the database however, if the entry doesn't exist it
     * will then insert a new one.
     *
     * @param connection a connection to not open an unnecessary amount
     * @param chunkId  the chunkId of the collector to save the contents of
     * @param material   the material to delete or save
     * @param amount     the amount of the material
     */
    public void saveContents(Connection connection, long chunkId, Material material, int amount)
    {
        boolean delete = amount <= 0;
        if (delete)
        {
            try (
                    PreparedStatement statement = connection.prepareStatement("DELETE FROM `collectors_contents` WHERE `collector_id` = ? AND `material` = ?;")
            )
            {
                // Set
                statement.setLong(1, chunkId);
                statement.setString(2, material.name());

                // Update
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                plugin.getLogger().log(Level.SEVERE, "A sql exception occurred: " + e.getMessage());
            }
        }
        else
        {
            try (
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO `collectors_contents` (`collector_id`, `amount`, `material`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `amount` = VALUES(`amount`);")
            )
            {
                // Set
                statement.setLong(1, chunkId);
                statement.setInt(2, amount);
                statement.setString(3, material.name());

                // Update
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                plugin.getLogger().log(Level.SEVERE, "A sql exception occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Sell all the contents of a collector
     *
     * @param collector the collector to sell the contents of
     * @return the total price after selling all the contents
     */
    public double sell(Collector collector)
    {
        // Calculate
        double total = this.value(collector);

        // Clear
        collector.clearContents();
        return total;
    }

    /**
     * Sell only a specific material from a collector.
     *
     * @param collector the collector to sell the contents of
     * @param material  the material to sell
     * @return the total price after selling the material or -1 if it doesn't exist in the config
     */
    public double sell(Collector collector, Material material)
    {
        // Args
        double total = this.value(collector, material);

        // Clear
        collector.setMaterialAmount(material, 0);
        return total;
    }

    /**
     * Calculate the total value for an entire collector.
     *
     * @param collector the collector to calculate the value of
     * @return the total value of the collector
     */
    public double value(Collector collector)
    {
        // Calculate
        double total = 0.0d;
        for (Map.Entry<Material, Integer> entry : collector.getContents().entrySet())
        {
            Material material = entry.getKey();
            total += this.value(collector, material);
        }
        return total;
    }

    /**
     * Calculate the total value for a specific material inside a collector.
     *
     * @param collector the collector to calculate the value of
     * @param material  the material to calculate the value of
     * @return the total value of the material in the collector
     */
    public double value(Collector collector, Material material)
    {
        // Args
        int amount = collector.getMaterialAmount(material);
        if (amount == 0 || !plugin.getConfig().isSet("prices." + material.name())) return 0.0d;
        double price = plugin.getConfig().getDouble("prices." + material.name());
        return amount * price;
    }

    /**
     * Clear the cache of all objects to prevent memory leaks.
     */
    public void disable()
    {
        cache.clear();
    }

}
