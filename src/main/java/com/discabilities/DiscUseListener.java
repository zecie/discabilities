package com.discabilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DiscUseListener implements Listener {

    private final AbilityManager abilityManager;
    private final CooldownManager cooldownManager;
    private final CombatTracker combatTracker;
    private final DiscInventory discInventory;
    private final HudManager hudManager;
    private final PassiveManager passiveManager;

    public DiscUseListener(AbilityManager abilityManager, CooldownManager cooldownManager,
                           CombatTracker combatTracker, DiscInventory discInventory,
                           HudManager hudManager, PassiveManager passiveManager) {
        this.abilityManager = abilityManager;
        this.cooldownManager = cooldownManager;
        this.combatTracker = combatTracker;
        this.discInventory = discInventory;
        this.hudManager = hudManager;
        this.passiveManager = passiveManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        AbilityManager.AbilityDef def = abilityManager.getAbilityDef(item);
        if (def == null) return;

        event.setCancelled(true);
        UUID uuid = player.getUniqueId();

        if (discInventory.hasDisc(uuid)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.sendMessage("\u00a7c\u25c6 \u00a7eYou already have a Disc equipped. Use \u00a7f/withdraw\u00a7e first.");
            return;
        }

        if (item.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else {
            item.setAmount(item.getAmount() - 1);
        }

        discInventory.equip(uuid, def.id());

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.4f);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.2f);
        player.sendMessage("\u00a77\u25c6 \u00a7e" + HudManager.displayName(def.id()) + "\u00a77 equipped. Press \u00a7f[F]\u00a77 to activate.");
        hudManager.sendHud(player);
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String equipped = discInventory.getEquipped(uuid);
        if (equipped == null) return;

        event.setCancelled(true);

        AbilityManager.AbilityDef def = abilityManager.getAbilityDefById(equipped);
        if (def == null) return;

        if (cooldownManager.isOnCooldown(uuid, equipped)) {
            long remaining = cooldownManager.getRemainingSeconds(uuid, equipped);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.sendMessage("\u00a7c\u25c6 \u00a7e" + HudManager.displayName(equipped)
                    + "\u00a7c on cooldown \u00a78(\u00a7f" + remaining + "s\u00a78)");
            hudManager.sendHud(player);
            return;
        }

        cooldownManager.setCooldown(uuid, equipped, def.cooldownSecs());
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.9f);
        abilityManager.activate(def, player, combatTracker.getLastHitPlayer(uuid));
        hudManager.sendHud(player);
    }
}
