package com.discabilities;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class WithdrawCommand implements CommandExecutor, TabCompleter {

    private final DiscInventory discInventory;
    private final AbilityManager abilityManager;
    private final HudManager hudManager;

    public WithdrawCommand(DiscInventory discInventory, AbilityManager abilityManager, HudManager hudManager) {
        this.discInventory = discInventory;
        this.abilityManager = abilityManager;
        this.hudManager = hudManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("only players can use this command.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        String equipped = discInventory.getEquipped(uuid);

        if (equipped == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.sendMessage("\u00a7c\u25c6 \u00a7eYou don't have a Disc equipped.");
            return true;
        }

        ItemStack item = abilityManager.createDiscItem(equipped);
        if (item != null) player.getInventory().addItem(item);
        discInventory.clear(uuid);

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.8f);
        player.sendMessage("\u00a77\u25c6 \u00a7e" + HudManager.displayName(equipped) + "\u00a77 withdrawn to inventory.");
        hudManager.sendHud(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
