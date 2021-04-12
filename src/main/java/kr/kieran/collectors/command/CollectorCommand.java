package kr.kieran.collectors.command;

import kr.kieran.collectors.CollectorsPlugin;
import kr.kieran.collectors.util.Color;
import kr.kieran.collectors.util.ItemBuilder;
import org.bukkit.Material;
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
            sender.sendMessage(Color.color(plugin.getConfig().getString("messages.invalid-usage")));
            return true;
        }

        // Target
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null)
        {
            sender.sendMessage(Color.color(plugin.getConfig().getString("messages.invalid-player").replace("%player%", args[0])));
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
            sender.sendMessage(Color.color(plugin.getConfig().getString("messages.invalid-number").replace("%number%", args[1])));
            return true;
        }

        // Give
        ItemStack collector = new ItemBuilder(Material.getMaterial(plugin.getConfig().getString("collector.item.material"))).durability(plugin.getConfig().getInt("collector.item.data")).name(Color.color(plugin.getConfig().getString("collector.item.name"))).setLores(Color.color(plugin.getConfig().getStringList("collector.item.lore"))).amount(amount);
        if (target.getInventory().firstEmpty() == -1)
        {
            target.getWorld().dropItemNaturally(target.getLocation(), collector);
        }
        else
        {
            target.getInventory().addItem(collector);
        }

        // Inform
        sender.sendMessage(Color.color(plugin.getConfig().getString("messages.gave-collector").replace("%amount%", String.format("%,d", amount)).replace("%player%", target.getName())));
        target.sendMessage(Color.color(plugin.getConfig().getString("messages.received-collector").replace("%amount%", String.format("%,d", amount))));
        return true;
    }

}
