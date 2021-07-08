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
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.BaseGui;
import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.action.MenuAction;
import kr.kieran.collectors.util.Color;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class MenuGui extends BaseGui
{

    private final CollectorsPlugin plugin;
    private final Collector collector;

    public MenuGui(@NotNull CollectorsPlugin plugin, @NotNull Collector collector)
    {
        super(plugin.getConfig().getInt("guis.menu.rows"), Color.color(plugin.getConfig().getString("guis.menu.name")), InteractionModifier.VALUES);

        // Assign
        this.plugin = plugin;
        this.collector = collector;

        // Populate
        this.populateGui();
    }

    private void populateGui()
    {
        for (String key : plugin.getConfig().getConfigurationSection("guis.menu.items").getKeys(false))
        {
            String path = "guis.menu.items." + key;
            this.setItem(
                    plugin.getConfig().getInt(path + ".slot"),
                    ItemBuilder
                            .from(Material.getMaterial(plugin.getConfig().getString(path + ".material", "STONE")))
                            .name(Component.text(Color.color(plugin.getConfig().getString(path + ".name"))))
                            .asGuiItem(event -> {
                                // Cancel
                                event.setCancelled(true);

                                // Action
                                MenuAction action;
                                try
                                {
                                    action = MenuAction.valueOf(plugin.getConfig().getString(path + ".action"));
                                }
                                catch (IllegalArgumentException e)
                                {
                                    return;
                                }

                                if (action == MenuAction.CONTENTS)
                                {
                                    // Open contents gui
                                    int rows = collector.isEmpty() ? 3 : Math.min(plugin.getConfig().getBoolean("guis.contents.variable-rows") ? (int) Math.ceil((double) collector.getContents().size() / 9.0d) + 2 : plugin.getConfig().getInt("guis.contents.rows"), 6);
                                    ContentsGui gui = new ContentsGui(plugin, rows, 20L, collector);
                                    gui.open(event.getWhoClicked());
                                }
                                else
                                {
                                    // Open settings gui
                                    SettingsGui gui = new SettingsGui(plugin, collector);
                                    gui.open(event.getWhoClicked());
                                }
                            })
            );
        }

        // Filler
        this.getFiller();
    }

    @Override
    public @NotNull GuiFiller getFiller()
    {
        GuiFiller filler = super.getFiller();
        filler.fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).asGuiItem());
        return filler;
    }

}
