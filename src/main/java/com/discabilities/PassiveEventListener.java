package com.discabilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PassiveEventListener implements Listener {

    private final DiscInventory discInventory;
    private final PassiveManager passiveManager;
    private final AbilityManager abilityManager;

    public PassiveEventListener(DiscInventory discInventory, PassiveManager passiveManager, AbilityManager abilityManager) {
        this.discInventory = discInventory;
        this.passiveManager = passiveManager;
        this.abilityManager = abilityManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        passiveManager.onBlockPlace(event.getPlayer(), event.getBlock().getType(), event);
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        passiveManager.onItemConsume(event.getPlayer(), event.getItem().getType(), event);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        passiveManager.onEntityTarget(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != null) {
            passiveManager.onPlayerInteract(event.getPlayer(), event);
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() != null) {
            passiveManager.onPlayerSneak(event.getPlayer(), event.isSneaking());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        passiveManager.onEntityDeath(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        passiveManager.onBlockBreak(event);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        passiveManager.onPlayerInteractEntity(event);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        passiveManager.onEntityDamage(event);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        abilityManager.tagItem(event.getItem().getItemStack());
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        abilityManager.tagItem(event.getCurrentItem());
        abilityManager.tagItem(event.getCursor());
    }

    @EventHandler
    public void onInvDrag(InventoryDragEvent event) {
        abilityManager.tagItem(event.getOldCursor());
    }
}
