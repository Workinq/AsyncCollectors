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

package kr.kieran.collectors.command;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CollectorCommand implements CommandExecutor
{

    private final CollectorsPlugin plugin;

    public CollectorCommand(CollectorsPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        // Check
        if (args.length < 1)
        {
            Text.message(sender, plugin.getConfig().getString("messages.invalid-usage"));
            return true;
        }

        // Target
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null)
        {
            Text.message(sender, plugin.getConfig().getString("messages.invalid-player"), args[0]);
            return true;
        }

        // Amount
        int amount;
        try
        {
            amount = args.length == 2 ? Integer.parseInt(args[1]) : 1;
        }
        catch (NumberFormatException e)
        {
            Text.message(sender, plugin.getConfig().getString("messages.invalid-number"), args[1]);
            return true;
        }

        // Give
        ItemStack collector = plugin.getCollector(amount);
        if (target.getInventory().firstEmpty() == -1)
        {
            target.getWorld().dropItemNaturally(target.getLocation(), collector);
        }
        else
        {
            target.getInventory().addItem(collector);
        }

        // Inform
        Text.message(sender, plugin.getConfig().getString("messages.gave-collector"), amount, target.getName());
        Text.message(target, plugin.getConfig().getString("messages.received-collector"), amount);
        return true;
    }

}
