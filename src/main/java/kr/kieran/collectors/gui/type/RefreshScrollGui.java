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

package kr.kieran.collectors.gui.type;

import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.components.ScrollType;
import dev.triumphteam.gui.guis.ScrollingGui;
import kr.kieran.collectors.CollectorsPlugin;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class RefreshScrollGui extends ScrollingGui
{

    /**
     * The task id assigned by Bukkit when creating the task
     * timer using {@link org.bukkit.scheduler.BukkitScheduler#runTaskTimer(Plugin, Runnable, long, long)}
     */
    private final int taskId;

    public RefreshScrollGui(@NotNull CollectorsPlugin plugin, int rows, int pageSize, @NotNull String title, @NotNull ScrollType scrollType, long period, @NotNull Set<InteractionModifier> interactionModifiers)
    {
        super(rows, pageSize, title, scrollType, interactionModifiers);

        // Start the update task
        this.taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, this::update, period, period).getTaskId();

        // Cancel task on gui close
        this.setCloseGuiAction(event -> plugin.getServer().getScheduler().cancelTask(this.taskId));
    }

    @Override
    public void update()
    {
        // Mark as updating
        this.setUpdating(true);

        // Clear the gui items & repopulate using populateGui()
        this.getPageItems().clear();
        this.getCurrentPageItems().clear();
        this.populateGui();

        // Super
        super.update();


        // Mark as no longer updating
        this.setUpdating(false);
    }

    @Override
    public boolean next()
    {
        return super.next();
    }

    public abstract void populateGui();

}
