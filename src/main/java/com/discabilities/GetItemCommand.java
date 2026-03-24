package com.discabilities;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetItemCommand implements CommandExecutor, TabCompleter {

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

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /getitem <material> [amount]");
            return true;
        }

        Material mat = Material.matchMaterial(args[0]);
        if (mat == null || mat == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Unknown material: " + args[0]);
            return true;
        }

        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[1])));
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid amount: " + args[1]);
                return true;
            }
        }

        player.getInventory().addItem(new ItemStack(mat, amount));
        player.sendMessage(ChatColor.GREEN + "\u25C6 Given " + amount + "x " + mat.name().toLowerCase());
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toUpperCase();
            return Arrays.stream(Material.values())
                    .filter(m -> m != Material.AIR && m.name().startsWith(prefix))
                    .map(m -> m.name().toLowerCase())
                    .limit(50)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
