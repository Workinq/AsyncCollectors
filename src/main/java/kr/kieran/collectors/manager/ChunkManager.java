package kr.kieran.collectors.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkManager
{

    // CACHE
    private final Set<Long> trackedChunks = ConcurrentHashMap.newKeySet();  // TRACKED CHUNKS
    private final Set<Long> lockedChunks = new HashSet<>();                 // LOCKED CHUNKS

    // --------------------- //
    // LOCKED CHUNKS         //
    // --------------------- //

    /**
     * Lock a chunk so no collectors can be placed or destroyed
     * to prevent any duplicate collectors from being placed or
     * from non-existent collectors being destroyed.
     *
     * @param chunkId the id of the chunk to lock
     */
    public void lock(long chunkId)
    {
        synchronized (this.lockedChunks)
        {
            this.lockedChunks.add(chunkId);
        }
    }

    /**
     * Unlock a chunk to allow any collectors to be placed
     * or destroyed.
     *
     * @param chunkId the id of the chunk to unlock
     */
    public void unlock(long chunkId)
    {
        synchronized (this.lockedChunks)
        {
            this.lockedChunks.remove(chunkId);
        }
    }

    /**
     * Check if there if the chunk is locked by looking up the
     * chunk id in the set containing the locked chunks.
     *
     * @param chunkId the id of the chunk to lookup
     * @return {@code true} if the chunk is locked or {@code false} otherwise
     */
    public boolean isLocked(long chunkId)
    {
        return this.lockedChunks.contains(chunkId);
    }

    // --------------------- //
    // TRACKED CHUNKS        //
    // --------------------- //

    /**
     * Add a chunk to the list of tracked chunks, this
     * is normally called when a chunk is loaded by listening
     * to the {@link org.bukkit.event.world.ChunkLoadEvent} event.
     *
     * @param chunkId the id of the chunk to untrack
     */
    public void track(long chunkId)
    {
        this.trackedChunks.add(chunkId);
    }

    /**
     * Remove a chunk from the list of tracked chunks, this
     * is normally called when a chunk is unloaded by listening
     * to the {@link org.bukkit.event.world.ChunkUnloadEvent} event.
     *
     * @param chunkId the id of the chunk to untrack
     */
    public void untrack(long chunkId)
    {
        this.trackedChunks.remove(chunkId);
        this.unlock(chunkId);
    }

    /**
     * Check if the chunk being queried is being tracked by the
     * plugin. This is to avoid placing more than one collector
     * in a single chunk.
     *
     * @param chunkId the id of the chunk
     * @return {@code true} if the chunk is being tracked by the plugin or {@code false} otherwise
     */
    public boolean isTracked(long chunkId)
    {
        return trackedChunks.contains(chunkId);
    }

    /**
     * Clear the cache of all objects to prevent memory leaks.
     */
    public void disable()
    {
        this.trackedChunks.clear();
        this.lockedChunks.clear();
    }

}
