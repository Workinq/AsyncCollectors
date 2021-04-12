package kr.kieran.collectors.model;

import kr.kieran.collectors.util.SerializationUtil;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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
    public Map<Material, Integer> getContents() { return Collections.unmodifiableMap(contents); }
    public int getMaterialAmount(Material material) { return contents.getOrDefault(material, 0); }
    public void setMaterialAmount(Material material, int amount) { contents.put(material, amount); }
    public void clearContents() { contents.clear(); }

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
