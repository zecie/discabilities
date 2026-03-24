package com.discabilities;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {

    private final CombatTracker combatTracker;
    private final PassiveManager passiveManager;
    private final TrustManager trustManager;

    public CombatListener(CombatTracker combatTracker, PassiveManager passiveManager, TrustManager trustManager) {
        this.combatTracker = combatTracker;
        this.passiveManager = passiveManager;
        this.trustManager = trustManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        Player attacker = null;
        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker != null && victim instanceof Player victimPlayer) {
            if (trustManager.isTrusted(attacker.getUniqueId(), victimPlayer.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
            combatTracker.setLastHit(attacker.getUniqueId(), victimPlayer.getUniqueId());
            passiveManager.onPlayerHit(attacker, victimPlayer, event);
            passiveManager.onPlayerTakeHit(victimPlayer, attacker, event);
            passiveManager.onStalAttack(attacker, victimPlayer, event);
        }

        if (victim instanceof Player victimPlayer) {
            if (damager instanceof WindCharge) {
                passiveManager.onPlayerTakeHitFromStrad(victimPlayer, event);
            }
        }
    }
}
