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

import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.components.ScrollType;
import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.gui.type.RefreshScrollGui;
import kr.kieran.collectors.model.Collector;
import kr.kieran.collectors.util.Text;

public class EditorViewGui extends RefreshScrollGui
{

    private final CollectorsPlugin plugin;
    private final Collector collector;

    public EditorViewGui(CollectorsPlugin plugin, Collector collector)
    {
        super(plugin, plugin.getConfig().getInt("guis.editor-view.rows"), plugin.getConfig().getInt("guis.editor-view.page-size"), Text.color(plugin.getConfig().getString("guis.editor-view.name")), ScrollType.VERTICAL, 20L, InteractionModifier.VALUES);

        // Assign
        this.plugin = plugin;
        this.collector = collector;

        // Populate
    }

    @Override
    public void populateGui()
    {
        // TODO: Maybe add a limit to the number of items a collector can have whitelisted/blacklisted?
    }

}
