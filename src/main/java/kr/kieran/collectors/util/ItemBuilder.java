package kr.kieran.collectors.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"deprecation", "unused"})
public class ItemBuilder extends ItemStack
{

    public ItemBuilder(Material material)
    {
        super(material);
    }

    public ItemBuilder(ItemStack item)
    {
        super(item);
    }

    public ItemBuilder amount(int amount)
    {
        this.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name)
    {
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(name);
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLores(List<String> text)
    {
        ItemMeta meta = this.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null)
        {
            lore = new ArrayList<>();
        }
        lore.addAll(text);
        meta.setLore(lore);
        this.setItemMeta(meta);
        return this;
    }

    public ItemBuilder durability(int durability)
    {
        this.setDurability((short) durability);
        return this;
    }

}
