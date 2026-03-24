package com.discabilities;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class TrustCommand implements CommandExecutor, TabCompleter {

    private final TrustManager trustManager;

    public TrustCommand(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("\u00a7cOnly players can use this command.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("\u00a7c\u25c6 \u00a7eUsage: /" + label + " <player>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("\u00a7c\u25c6 \u00a7ePlayer not found: \u00a7f" + args[0]);
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage("\u00a7c\u25c6 \u00a7eYou cannot trust yourself.");
            return true;
        }
        if (command.getName().equalsIgnoreCase("trust")) {
            trustManager.trust(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("\u00a77\u25c6 \u00a7f" + target.getName() + "\u00a77 trusted \u00a78\u2014 your abilities will no longer affect them.");
        } else {
            trustManager.untrust(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("\u00a77\u25c6 \u00a7f" + target.getName() + "\u00a77 untrusted.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(prefix))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
