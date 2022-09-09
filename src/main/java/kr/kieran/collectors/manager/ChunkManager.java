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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    public synchronized void lock(long chunkId)
    {
        lockedChunks.add(chunkId);
    }

    /**
     * Unlock a chunk to allow any collectors to be placed
     * or destroyed.
     *
     * @param chunkId the id of the chunk to unlock
     */
    public synchronized void unlock(long chunkId)
    {
        lockedChunks.remove(chunkId);
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
        return lockedChunks.contains(chunkId);
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
        trackedChunks.add(chunkId);
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
        trackedChunks.remove(chunkId);
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
     * Check if the chunk the player is interacting in is either
     * tracked or locked. If it is not being tracked then it means
     * the plugin has not yet loaded the data for teh chunk. If the
     * chunk is locked then it means an operation is currently being
     * performed on the chunk.
     *
     * @param chunkId the id of the chunk to check
     * @return true if the chunk can be modified and false otherwise
     */
    public boolean canUseChunk(long chunkId)
    {
        return this.isTracked(chunkId) && !this.isLocked(chunkId);
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
