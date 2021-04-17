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

import kr.kieran.collectors.CollectorsPlugin;
import me.mattstudios.mfgui.gui.guis.BaseGui;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class RefreshingGui extends BaseGui
{

    /**
     * The task id assigned by Bukkit when creating the task
     * timer using {@link org.bukkit.scheduler.BukkitScheduler#runTaskTimer(Plugin, Runnable, long, long)}
     */
    private final int taskId;

    /**
     * Create a gui extending the {@link BaseGui} class which will
     * update the inventory periodically using the parameter {@code period}
     * as the interval.
     *
     * @param plugin    the collectors plugin instance
     * @param rows      the number of rows to utilise
     * @param title     the title of the gui
     * @param period    the delay between gui updates
     */
    public RefreshingGui(@NotNull CollectorsPlugin plugin, int rows, @NotNull String title, long period)
    {
        // Super
        super(rows, title);

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
        this.getGuiItems().clear();
        this.populateGui();

        // Super
        super.update();

        // Mark as no longer updating
        this.setUpdating(false);
    }

    /**
     * Method called when the contents of the inventory update.
     */
    public abstract void populateGui();

}
