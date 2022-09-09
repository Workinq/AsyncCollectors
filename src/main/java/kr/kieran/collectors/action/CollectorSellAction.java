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

package kr.kieran.collectors.action;

import dev.triumphteam.gui.components.GuiAction;
import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.gui.ContentsGui;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CollectorSellAction implements GuiAction<InventoryClickEvent>
{

    private final CollectorsPlugin plugin;
    private final Material material;
    private final Collector collector;
    private final ContentsGui gui;

    public CollectorSellAction(CollectorsPlugin plugin, Material material, Collector collector, ContentsGui gui)
    {
        this.plugin = plugin;
        this.material = material;
        this.collector = collector;
        this.gui = gui;
    }

    @Override
    public void execute(InventoryClickEvent event)
    {
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
        Text.message(player, plugin.getConfig().getString("messages.material-sold"), amount, Text.getNicedEnumString(material.name()), total);

        // Update
        gui.update();
    }

}
