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
        plugin.getChunkManager().lock(chunkId);

        // TODO: Maybe use try/finally to always unlock the chunk in case of an error from loading the collector

        plugin.newChain()
                .async(() -> plugin.getCollectorManager().load(chunkId))
                .sync(() -> this.loadChunk(chunkId))
                .execute();
    }

    // LISTENER: COLLECTOR UNLOAD
    @EventHandler
    public void chunkUnload(ChunkUnloadEvent event)
    {
        // Args
        long chunkId = event.getChunk().getChunkKey();
        plugin.getChunkManager().lock(chunkId);

        Collector collector = plugin.getCollectorManager().getById(chunkId);
        if (collector == null)
        {
            this.unloadChunk(chunkId);
            return;
        }

        // Save & unload
        plugin.getSaveTask().queueSave(chunkId);
        this.unloadChunk(chunkId);
    }

    private void loadChunk(long chunkId)
    {
        plugin.getChunkManager().track(chunkId);
        plugin.getChunkManager().unlock(chunkId);
    }

    private void unloadChunk(long chunkId)
    {
        plugin.getCollectorManager().invalidate(chunkId);
        plugin.getChunkManager().untrack(chunkId);
        plugin.getChunkManager().unlock(chunkId);
    }

}
