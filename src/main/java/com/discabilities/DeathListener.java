package com.discabilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DeathListener implements Listener {

    private final DiscInventory discInventory;
    private final AbilityManager abilityManager;
    private final HudManager hudManager;
    private final HitTracker hitTracker;

    public DeathListener(DiscInventory discInventory, AbilityManager abilityManager, HudManager hudManager, HitTracker hitTracker) {
        this.discInventory = discInventory;
        this.abilityManager = abilityManager;
        this.hudManager = hudManager;
        this.hitTracker = hitTracker;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        String equipped = discInventory.getEquipped(uuid);
        if (equipped != null) {
            ItemStack disc = abilityManager.createDiscItem(equipped);
            if (disc != null) {
                event.getDrops().add(disc);
            }
            discInventory.clear(uuid);
        }
        hitTracker.reset(uuid);
        hudManager.sendHud(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        discInventory.saveAll();
    }
}
