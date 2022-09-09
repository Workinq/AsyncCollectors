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
import kr.kieran.collectors.gui.SettingsGui;
import kr.kieran.collectors.model.Collector;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CollectorModeSwitch implements GuiAction<InventoryClickEvent>
{

    private final CollectorsPlugin plugin;
    private final Collector collector;
    private final SettingsGui gui;

    public CollectorModeSwitch(CollectorsPlugin plugin, Collector collector, SettingsGui gui)
    {
        this.plugin = plugin;
        this.collector = collector;
        this.gui = gui;
    }

    @Override
    public void execute(InventoryClickEvent event)
    {
        collector.setMode(collector.getMode().nextMode());
        plugin.getCollectorManager().save(collector.getChunkId(), false);
        // TODO: Add a cooldown period to switching modes (avoid saving to database too often)
        gui.update();
    }

}
