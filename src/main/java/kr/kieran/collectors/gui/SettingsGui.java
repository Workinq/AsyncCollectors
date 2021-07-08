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
import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.action.SettingsAction;
import kr.kieran.collectors.gui.type.RefreshBaseGui;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Color;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SettingsGui extends RefreshBaseGui
{

    private final CollectorsPlugin plugin;
    private final Collector collector;

    public SettingsGui(@NotNull CollectorsPlugin plugin, Collector collector)
    {
        super(plugin, plugin.getConfig().getInt("guis.settings.rows"), Color.color(plugin.getConfig().getString("guis.settings.name")), 20L, InteractionModifier.VALUES);

        // Assign
        this.plugin = plugin;
        this.collector = collector;

        // Populate
        this.populateGui();
    }

    @Override
    public void populateGui()
    {
        // Items
        for (String key : plugin.getConfig().getConfigurationSection("guis.settings.items").getKeys(false))
        {
            String path = "guis.settings.items." + key;
            Material material = Material.getMaterial(plugin.getConfig().getString(path + (key.equals("mode") ? "." + collector.getMode().name().toLowerCase() : "") + ".material"));
            List<Component> lore = Color.color(plugin.getConfig().getStringList(path + (key.equals("mode") ? "." + collector.getMode().name().toLowerCase() : "") + ".lore")).stream().map(Component::text).collect(Collectors.toList());
            this.setItem(
                    plugin.getConfig().getInt(path + ".slot"),
                    ItemBuilder
                            .from(material)
                            .name(Component.text(Color.color(plugin.getConfig().getString(path + ".name"))))
                            .lore(lore)
                            .asGuiItem(event -> {
                                // Cancel
                                event.setCancelled(true);

                                // Action
                                SettingsAction action;
                                try
                                {
                                    action = SettingsAction.valueOf(plugin.getConfig().getString(path + ".action"));
                                }
                                catch (IllegalArgumentException e)
                                {
                                    return;
                                }

                                if (action == SettingsAction.MODE)
                                {
                                    System.out.println("MODE");
                                }
                                else if (action == SettingsAction.BLOCK_EDIT)
                                {
                                    System.out.println("BLOCK_EDIT");
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
        filler.fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.space()).asGuiItem());
        return filler;
    }

}
