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

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.components.ScrollType;
import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.action.CollectorSellAction;
import kr.kieran.collectors.gui.type.RefreshScrollGui;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Text;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentsGui extends RefreshScrollGui
{

    private final CollectorsPlugin plugin;
    private final Collector collector;

    public ContentsGui(@NotNull CollectorsPlugin plugin, @NotNull Collector collector)
    {
        // Super
        super(plugin, 3, 7, Text.color(plugin.getConfig().getString("guis.contents.name")), ScrollType.HORIZONTAL, 40L, InteractionModifier.VALUES);

        // Assign
        this.plugin = plugin;
        this.collector = collector;

        // Populate
        this.populateGui();
    }

    @Override
    public void populateGui()
    {
        // Filler
        this.getFiller().fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem());

        // Items
        for (Map.Entry<Material, Integer> entry : collector.getContents().entrySet())
        {
            // Args
            Material material = entry.getKey();
            List<String> lore = Text.color(plugin.getConfig().getStringList("guis.contents.item.lore").stream().map(text -> text.replace("%amount%", String.format("%,d", entry.getValue())).replace("%value%", String.format("%,.1f", plugin.getCollectorManager().value(collector, material)))).collect(Collectors.toList()));

            // Add
            this.addItem(ItemBuilder
                    .from(material)
                    .setName(Text.getNicedEnumString(material.name()))
                    .setLore(lore)
                    .asGuiItem(new CollectorSellAction(plugin, material, collector, this)));
        }

        // Navigation
        this.setItem(3, 3, ItemBuilder.from(Material.ARROW).setName(Text.color("Previous")).asGuiItem(event -> this.previous()));
        this.setItem(3, 7, ItemBuilder.from(Material.ARROW).setName(Text.color("Next")).asGuiItem(event -> this.next()));
    }

}
