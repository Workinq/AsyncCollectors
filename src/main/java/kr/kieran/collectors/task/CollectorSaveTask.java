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

package kr.kieran.collectors.task;

import kr.kieran.collectors.CollectorsPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class CollectorSaveTask extends BukkitRunnable
{

    // Collectors will be saved every 5 seconds
    // TODO: Make this configurable
    private static final long SAVE_INTERVAL = TimeUnit.SECONDS.toMillis(5L);

    private final CollectorsPlugin plugin;

    // Key: Chunk ID
    // Value: Submission Epoch
    private final ConcurrentMap<Long, Long> cache = new ConcurrentHashMap<>();

    public CollectorSaveTask(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        // TODO: See if we can make this a batch statement
        Iterator<Map.Entry<Long, Long>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<Long, Long> entry = iterator.next();
            long chunkId = entry.getKey(), time = entry.getValue();

            // If 5 seconds have passed since the collector was queued for a save
            if (System.currentTimeMillis() > time + SAVE_INTERVAL)
            {
                if (plugin.getCollectorManager().exists(chunkId))
                {
                    plugin.getCollectorManager().save(chunkId);
                }
                iterator.remove();
            }
        }
    }

    public void queueSave(long chunkId)
    {
        if (cache.containsKey(chunkId)) return;
        cache.put(chunkId, System.currentTimeMillis());
    }

    public void disable()
    {
        this.cache.clear();
    }

}
