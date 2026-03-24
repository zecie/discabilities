package com.discabilities;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class HudManager {

    private final Plugin plugin;
    private final DiscInventory discInventory;
    private final CooldownManager cooldownManager;
    private final AbilityManager abilityManager;
    private final HitTracker hitTracker;
    private BukkitTask task;

    public HudManager(Plugin plugin, DiscInventory discInventory, CooldownManager cooldownManager,
                      AbilityManager abilityManager, HitTracker hitTracker) {
        this.plugin = plugin;
        this.discInventory = discInventory;
        this.cooldownManager = cooldownManager;
        this.abilityManager = abilityManager;
        this.hitTracker = hitTracker;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 0L, 4L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void updateAll() {
        for (Player p : Bukkit.getOnlinePlayers()) sendHud(p);
    }

    public void sendHud(Player player) {
        UUID uuid = player.getUniqueId();
        String equipped = discInventory.getEquipped(uuid);

        String text;
        if (equipped == null) {
            text = "\u00a78\u25c6  \u00a77No Disc Equipped  \u00a78\u2014  \u00a77Right-click a Disc to equip";
        } else {
            String name = displayName(equipped);
            boolean onCd = cooldownManager.isOnCooldown(uuid, equipped);
            String status;
            if (onCd) {
                long rem = cooldownManager.getRemainingSeconds(uuid, equipped);
                AbilityManager.AbilityDef def = abilityManager.getAbilityDefById(equipped);
                int total = def != null ? def.cooldownSecs() : 60;
                status = "\u00a7c\u00a7l" + rem + "s \u00a78" + cooldownBar(rem, total);
            } else {
                status = "\u00a7a\u00a7l\u25b6 Ready";
            }
            String passive = passiveStatus(player, equipped, uuid);
            String passivePart = passive.isEmpty() ? "" : "  \u00a78\u2759  " + passive;
            text = "\u00a75\u25c6 \u00a7d\u00a7l" + name + "\u00a7r \u00a75\u25c6  \u00a78\u2759  " + status + passivePart;
        }

        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    private String passiveStatus(Player player, String disc, UUID uuid) {
        int dealt = hitTracker.getHitsDealt(uuid);
        int taken = hitTracker.getHitsTaken(uuid);
        return switch (disc) {
            case "13" -> {
                int skulls = 0;
                for (ItemStack i : player.getInventory().getContents()) {
                    if (i != null && i.getType() == Material.WITHER_SKELETON_SKULL) skulls += i.getAmount();
                }
                if (skulls >= 4) yield "\u00a7c\u2694 Strength II \u00a78\u00b7 \u00a7f" + (dealt % 13) + "/13";
                if (skulls >= 1) yield "\u00a76\u2694 Strength I \u00a78\u00b7 \u00a7f" + (dealt % 13) + "/13";
                yield "\u00a78No Skulls \u00a78\u00b7 \u00a7f" + (dealt % 13) + "/13";
            }
            case "5" -> hitTracker.isRingActive(uuid)
                    ? "\u00a7b\u2726 Ring Charged"
                    : "\u00a77Ring \u00a7f" + (dealt % 5) + "\u00a78/\u00a7f5";
            case "strad" -> {
                int combo = hitTracker.getCombo(uuid);
                yield combo > 1 ? "\u00a76\u00d7" + combo + " \u00a77Combo" : "\u00a78No Combo";
            }
            case "11" -> "\u00a7f" + (dealt % 11) + "\u00a78/\u00a7f11 \u00a78\u00b7 \u00a7f" + (taken % 11) + "\u00a78t";
            case "wait" -> "\u00a7f" + (dealt % 10) + "\u00a78/\u00a7f10 \u00a78\u00b7 \u00a77Freeze";
            case "tears" -> player.getWorld().hasStorm() ? "\u00a7b\u26c6 Rain" : "\u00a78Dry";
            case "mall" -> player.getWorld().isThundering() ? "\u00a7e\u26a1 Thunder"
                    : player.getWorld().hasStorm() ? "\u00a77\u2601 Rain" : "\u00a78Clear";
            case "pigstep" -> "NETHER".equals(player.getWorld().getEnvironment().name())
                    ? "\u00a7c\u2605 Nether" : "\u00a78Not Nether";
            case "otherside" -> !"NORMAL".equals(player.getWorld().getEnvironment().name())
                    ? "\u00a7d\u2605 Strength" : "\u00a78Overworld";
            case "lava_chicken" -> "\u00a77\u2726 Fireproof";
            case "mellohi" -> "\u00a7a\u2665 Regenerating";
            case "far" -> "\u00a77" + hitTracker.getHitsDealt(uuid) + " \u00a78Hits";
            case "relic" -> abilityManager.isBountyActive(uuid) ? "\u00a7e\u2726 Bounty Active" : "\u00a78No Bounty";
            case "ward" -> "\u00a7a\u2694 Looting V";
            case "creator" -> {
                int copper = 0;
                for (ItemStack i : player.getInventory().getContents()) {
                    if (i != null && i.getType() == Material.COPPER_INGOT) copper += i.getAmount();
                }
                yield "\u00a77" + copper + " \u00a78Cu";
            }
            case "creator_music_box" -> {
                int copper = 0;
                for (ItemStack i : player.getInventory().getContents()) {
                    if (i != null && i.getType() == Material.COPPER_INGOT) copper += i.getAmount();
                }
                yield "\u00a77" + copper + " \u00a78Cu";
            }
            case "chirp" -> "\u00a7f" + (taken % 10) + "\u00a78/\u00a7f10 \u00a78Beaks";
            case "cat" -> "\u00a7f" + (dealt % 20) + "\u00a78/\u00a7f20 \u00a78Poison";
            default -> "";
        };
    }

    private String cooldownBar(long remaining, int total) {
        int filled = total > 0 ? (int) Math.round(6.0 * remaining / total) : 0;
        filled = Math.max(0, Math.min(6, filled));
        StringBuilder bar = new StringBuilder("\u00a78[\u00a7c");
        for (int i = 0; i < filled; i++) bar.append('\u25a0');
        bar.append("\u00a78");
        for (int i = filled; i < 6; i++) bar.append('\u25a1');
        bar.append(']');
        return bar.toString();
    }

    public static String displayName(String id) {
        return switch (id) {
            case "5" -> "Disc 5";
            case "11" -> "Disc 11";
            case "13" -> "Disc 13";
            default -> {
                String[] words = id.replace("_", " ").split(" ");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(' ');
                }
                yield sb.toString().trim();
            }
        };
    }
}
