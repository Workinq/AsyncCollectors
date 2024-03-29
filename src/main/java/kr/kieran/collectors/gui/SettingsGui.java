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
import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.action.CollectorModeSwitch;
import kr.kieran.collectors.gui.type.RefreshBaseGui;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Text;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class SettingsGui extends RefreshBaseGui
{

    private final CollectorsPlugin plugin;
    private final Collector collector;

    public SettingsGui(@NotNull CollectorsPlugin plugin, Collector collector)
    {
        super(plugin, plugin.getConfig().getInt("guis.settings.rows"), Text.color(plugin.getConfig().getString("guis.settings.name")), 20L, InteractionModifier.VALUES);

        // Assign
        this.plugin = plugin;
        this.collector = collector;

        // Populate
        this.populateGui();
    }

    @Override
    public void populateGui()
    {
        // Mode
        String path = "guis.settings.items.mode." + collector.getMode().getPathName();
        this.setItem(
                plugin.getConfig().getInt("guis.settings.items.mode.slot"),
                ItemBuilder
                        .from(Material.getMaterial(plugin.getConfig().getString(path + ".material")))
                        .setName(Text.color(plugin.getConfig().getString(path + ".name")))
                        .setLore(Text.color(plugin.getConfig().getStringList(path + ".lore")))
                        .asGuiItem(new CollectorModeSwitch(plugin, collector, this)));

        // Item collection editor
        path = "guis.settings.items.editor";
        this.setItem(
                plugin.getConfig().getInt(path + ".slot"),
                ItemBuilder
                        .from(Material.getMaterial(plugin.getConfig().getString(path + ".material")))
                        .setName(Text.color(plugin.getConfig().getString(path + ".name")))
                        .setLore(Text.color(plugin.getConfig().getStringList(path + ".lore")))
                        .asGuiItem(event -> new EditorMenuGui(plugin, collector).open(event.getWhoClicked())));

        // Filler
        this.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName(" ").asGuiItem());
    }

}
