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
