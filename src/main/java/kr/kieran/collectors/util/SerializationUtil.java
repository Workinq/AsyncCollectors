package kr.kieran.collectors.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerializationUtil
{

    public static String serialize(Location location)
    {
        return "" + location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + "";
    }

    public static Location deserialize(String serialized)
    {
        String[] split = serialized.split(":");
        return new Location(Bukkit.getWorld(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
    }

}
