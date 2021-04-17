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

package kr.kieran.collectors.model;

import kr.kieran.collectors.util.SerializationUtil;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Collector
{

    // LOCK
    private final ReentrantLock lock = new ReentrantLock(true);
    public void lock() { lock.lock(); }
    public void unlock() { lock.unlock(); }
    public boolean isLocked() { return lock.isLocked(); }

    // ID: CHUNK KEY
    private final long chunkId;
    public long getChunkId() { return chunkId; }

    // CONTENTS
    private final Map<Material, Integer> contents;
    public Map<Material, Integer> getAbsoluteContents() { return Collections.unmodifiableMap(contents); }
    public Map<Material, Integer> getContents() { return this.contents.entrySet().stream().filter(entry -> entry.getValue() != 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); }
    public int getMaterialAmount(Material material) { return contents.getOrDefault(material, 0); }
    public void setMaterialAmount(Material material, int amount) { contents.put(material, amount); }
    public void clearContents() { contents.replaceAll((material, integer) -> integer = 0); }
    public boolean isEmpty()
    {
        if (contents.isEmpty()) return true;
        return contents.values().stream().noneMatch(amount -> amount > 0);
    }

    // MODE
    private Mode mode;
    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

    // LOCATION
    private final String location;
    public Location getLocation() { return location != null ? SerializationUtil.deserialize(location) : null; }

    public Collector(long chunkId, Map<Material, Integer> contents, Mode mode, String location)
    {
        this.chunkId = chunkId;
        this.mode = mode;
        this.contents = contents;
        this.location = location;
    }

    public enum Mode
    {
        WHITELIST, BLACKLIST, ALL
    }

}
