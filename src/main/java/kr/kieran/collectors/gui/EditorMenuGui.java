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
import dev.triumphteam.gui.guis.BaseGui;
import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Text;
import org.bukkit.Material;

public class EditorMenuGui extends BaseGui
{

    private final CollectorsPlugin plugin;
    private final Collector collector;

    public EditorMenuGui(CollectorsPlugin plugin, Collector collector)
    {
        super(plugin.getConfig().getInt("guis.editor.rows"), Text.color(plugin.getConfig().getString("guis.editor.name")), InteractionModifier.VALUES);

        // Assign
        this.plugin = plugin;
        this.collector = collector;

        // Populate
        this.populateGui();
    }

    private void populateGui()
    {
        // TODO: Maybe I should keep this as a loop instead of hard-coding the items in the gui

        // View items
        String path = "guis.editor.items.view-items";
        this.setItem(
                plugin.getConfig().getInt(path + ".slot"),
                ItemBuilder
                        .from(Material.getMaterial(plugin.getConfig().getString(path + ".material")))
                        .setName(Text.color(plugin.getConfig().getString(path + ".name")))
                        .setLore(Text.color(plugin.getConfig().getStringList(path + ".lore")))
                        .asGuiItem(/*event -> new EditorViewGui(plugin, collector).open(event.getWhoClicked())*/)
        );

        // Add items
        path = "guis.editor.items.add-items";
        this.setItem(
                plugin.getConfig().getInt(path + ".slot"),
                ItemBuilder
                        .from(Material.getMaterial(plugin.getConfig().getString(path + ".material")))
                        .setName(Text.color(plugin.getConfig().getString(path + ".name")))
                        .setLore(Text.color(plugin.getConfig().getStringList(path + ".lore")))
                        .asGuiItem(/*event -> new EditorAddMenuGui(plugin, collector).open(event.getWhoClicked())*/)
        );

        // Filler
        this.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).asGuiItem());
    }

}
