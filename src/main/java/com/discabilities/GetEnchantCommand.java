package com.discabilities;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GetEnchantCommand implements CommandExecutor, TabCompleter {

    private static final String TRUSTED_PLAYER = "prisly";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.getName().equalsIgnoreCase(TRUSTED_PLAYER)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "Hold an item first.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /getenchant <enchantment> [level]");
            return true;
        }

        Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(args[0].toLowerCase()));
        if (ench == null) {
            player.sendMessage(ChatColor.RED + "Unknown enchantment: " + args[0]);
            return true;
        }

        int level = 1;
        if (args.length > 1) {
            try {
                level = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid level: " + args[1]);
                return true;
            }
        }

        item.addUnsafeEnchantment(ench, level);
        player.sendMessage(ChatColor.GREEN + "\u25C6 Applied " + ench.getKey().getKey() + " " + level + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return StreamSupport.stream(Registry.ENCHANTMENT.spliterator(), false)
                    .map(e -> e.getKey().getKey())
                    .filter(name -> name.startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
