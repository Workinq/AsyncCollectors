package kr.kieran.collectors.gui;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Color;
import me.mattstudios.mfgui.gui.components.util.ItemBuilder;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class CollectorGui extends RefreshingGui
{

    private final Collector collector;

    public CollectorGui(@NotNull CollectorsPlugin plugin, int rows, @NotNull String title, long period, @NotNull Collector collector)
    {
        // Super
        super(plugin, rows, title, period);

        // Assign
        this.collector = collector;

        // Populate
        this.populateGui();
    }

    @Override
    public void populateGui()
    {
        for (Map.Entry<Material, Integer> entry : collector.getContents().entrySet())
        {
            Material material = entry.getKey();
            int amount = entry.getValue();
            if (amount == 0) continue;

            this.addItem(ItemBuilder.from(material).setLore(Color.color(List.of("&7Amount: &f" + amount))).asGuiItem(event -> {
                event.setCancelled(true);
            }));
        }
    }

}
