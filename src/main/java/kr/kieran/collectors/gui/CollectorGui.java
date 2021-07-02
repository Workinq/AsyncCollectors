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

package kr.kieran.collectors.gui;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Color;
import me.mattstudios.mfgui.gui.components.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class CollectorGui extends RefreshingGui
{

    private final CollectorsPlugin plugin;
    private final Collector collector;

    public CollectorGui(@NotNull CollectorsPlugin plugin, int rows, @NotNull String title, long period, @NotNull Collector collector)
    {
        // Super
        super(plugin, rows, title, period);

        // Assign
        this.plugin = plugin;
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

            this.addItem(ItemBuilder.from(material).setLore(Color.color(List.of("&7Amount: &f" + amount))).asGuiItem(event -> {
                // Args
                Player player = (Player) event.getWhoClicked();
                double total = plugin.getCollectorManager().sell(collector, material);

                // Check
                if (total == -1.0d)
                {
                    event.setCancelled(true);
                    return;
                }

                // Cancel click
                event.setCancelled(true);

                // Deposit
                plugin.getMoneyManager().pay(player.getUniqueId(), total);
                player.sendMessage(Color.color(plugin.getConfig().getString("messages.material-sold").replace("%amount%", String.format("%,d", amount)).replace("%material%", material.name()).replace("%total%", String.format("%.1f", total))));
            }));
        }
    }

}
