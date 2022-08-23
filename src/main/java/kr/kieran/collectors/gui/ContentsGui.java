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
import dev.triumphteam.gui.components.util.GuiFiller;
import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.gui.type.RefreshScrollGui;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Color;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContentsGui extends RefreshScrollGui
{

    private final CollectorsPlugin plugin;
    private final Collector collector;

    public ContentsGui(@NotNull CollectorsPlugin plugin, int rows, long period, @NotNull Collector collector)
    {
        // Super
        super(plugin, rows, 7, Color.color(plugin.getConfig().getString("guis.contents.name")), ScrollType.VERTICAL, period, InteractionModifier.VALUES);

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
        this.getFiller();

        // Items
        for (Map.Entry<Material, Integer> entry : collector.getContents().entrySet())
        {
            // Args
            Material material = entry.getKey();
            int startAmount = entry.getValue();
            double startValue = plugin.getCollectorManager().value(collector, material);
            List<String> lore = Color.color(plugin.getConfig().getStringList("guis.contents.item.lore").stream().map(text -> text.replace("%amount%", String.format("%,d", startAmount)).replace("%value%", String.format("%,.1f", startValue))).collect(Collectors.toList()));
            List<Component> components = lore.stream().map(Component::text).collect(Collectors.toList());

            // Add
            this.addItem(ItemBuilder.from(material).name(Component.text(this.getNicedEnumString(material.name()))).lore(components).asGuiItem(event ->
            {
                // Cancel
                event.setCancelled(true);

                // Args
                Player player = (Player) event.getWhoClicked();
                int amount = collector.getMaterialAmount(material);
                double total = plugin.getCollectorManager().sell(collector, material);

                // Check
                if (total == 0.0d)
                {
                    // TODO: Make it so collectors don't pick up items which can't be sold
                    return;
                }

                // Deposit & Inform
                plugin.getMoneyManager().pay(player.getUniqueId(), total);
                String message = plugin.getConfig().getString("messages.material-sold");
                if (message != null && !message.isEmpty()) player.sendMessage(Color.color(message.replace("%amount%", String.format("%,d", amount)).replace("%material%", this.getNicedEnumString(material.name())).replace("%total%", String.format("%,.1f", total))));

                // Update
                this.update();
            }));
        }

        // Navigation
        this.setItem(3, 3, ItemBuilder.from(Material.ARROW).name(Component.text(Color.color("Previous"))).asGuiItem(event -> this.previous()));
        this.setItem(3, 7, ItemBuilder.from(Material.ARROW).name(Component.text(Color.color("Next"))).asGuiItem(event -> this.next()));
    }

    private String implode(Object[] list)
    {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < list.length; i++)
        {
            Object item = list[i];
            String str = (item == null ? "NULL" : item.toString());

            if (i != 0) ret.append(" ");
            ret.append(str);
        }
        return ret.toString();
    }

    private static final Pattern PATTERN_ENUM_SPLIT = Pattern.compile("[\\s_]+");
    private String getNicedEnumString(String name)
    {
        List<String> parts = new ArrayList<>();
        for (String part : PATTERN_ENUM_SPLIT.split(name.toLowerCase()))
        {
            parts.add(part.substring(0, 1).toUpperCase() + part.substring(1));
        }
        return ChatColor.WHITE + this.implode(parts.toArray(new Object[0]));
    }

    @Override
    public @NotNull GuiFiller getFiller()
    {
        GuiFiller filler = super.getFiller();
        filler.fillBorder(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.space()).asGuiItem());
        return filler;
    }

}
