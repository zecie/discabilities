package com.discabilities;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetExperienceCommand implements CommandExecutor, TabCompleter {

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
            player.sendMessage(ChatColor.YELLOW + "Usage: /getexperience <levels>");
            return true;
        }

        int levels;
        try {
            levels = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number: " + args[0]);
            return true;
        }

        player.giveExpLevels(levels);
        player.sendMessage(ChatColor.GREEN + "\u25C6 Given " + levels + " experience level" + (levels == 1 ? "" : "s") + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("1", "5", "10", "20", "50", "100");
        }
        return List.of();
    }
}
