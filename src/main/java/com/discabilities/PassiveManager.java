package com.discabilities;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.UUID;

public class PassiveManager {

    private final Plugin plugin;
    private final DiscInventory discInventory;
    private final HitTracker hitTracker;
    private final CooldownManager cooldownManager;
    private final CombatTracker combatTracker;
    private final AbilityManager abilityManager;
    private final TrustManager trustManager;

    public PassiveManager(Plugin plugin, DiscInventory discInventory, HitTracker hitTracker,
                          CooldownManager cooldownManager, CombatTracker combatTracker, AbilityManager abilityManager,
                          TrustManager trustManager) {
        this.plugin = plugin;
        this.discInventory = discInventory;
        this.hitTracker = hitTracker;
        this.cooldownManager = cooldownManager;
        this.combatTracker = combatTracker;
        this.abilityManager = abilityManager;
        this.trustManager = trustManager;
    }

    public void start() {
        new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    applyTickPassives(player);
                }
            }
        }.runTaskTimer(plugin, 0, 5);

        new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    onPrecipiceAutoFire(player);
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void applyTickPassives(Player player) {
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (disc == null) return;

        switch (disc) {
            case "13" -> applyThirteenTick(player);
            case "blocks" -> applyBlocksTick(player);
            case "mall" -> applyMallTick(player);
            case "11" -> applyElevenTick(player);
            case "wait" -> applyWaitTick(player);
            case "otherside" -> applyOthersideTick(player);
            case "5" -> applyFiveTick(player);
            case "pigstep" -> applyPigstepTick(player);
            case "tears" -> applyTearsTick(player);
            case "lava_chicken" -> applyLavaChickenTick(player);
            case "mellohi" -> applyMellohiTick(player);
            case "ward" -> applyWardTick(player);
            case "creator" -> applyCreatorTick(player);
            case "creator_music_box" -> applyCreatorMusicBoxTick(player);
        }
    }

    private void applyThirteenTick(Player player) {
        int skullCount = 0;
        for (ItemStack i : player.getInventory().getContents()) {
            if (i != null && i.getType() == Material.WITHER_SKELETON_SKULL) skullCount += i.getAmount();
        }
        if (skullCount >= 4) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 12, 1, true, false));
        } else if (skullCount >= 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 12, 0, true, false));
        }
    }

    private void applyBlocksTick(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        String typeName = main.getType().name();
        if (typeName.contains("PICKAXE") || typeName.contains("AXE") || typeName.contains("SHOVEL") || typeName.contains("HOE")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 12, 8, true, false));
        }
    }

    private void applyMallTick(Player player) {
        World world = player.getWorld();
        if (world.isThundering()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 3, true, false));
        } else if (world.hasStorm()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 2, true, false));
        }
    }

    private void applyElevenTick(Player player) {
        Location loc = player.getLocation();
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        if (below == Material.SOUL_SAND) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 2, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 12, 2, true, false));
            player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc.add(0, 0.1, 0), 3, 0.3, 0.1, 0.3, 0.02);
        }
    }

    private void applyWaitTick(Player player) {
        String biomeName = player.getLocation().getBlock().getBiome().name();
        if (biomeName.contains("FROZEN") || biomeName.contains("SNOWY") || biomeName.contains("ICE")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 2, true, false));
        }
    }

    private void applyOthersideTick(Player player) {
        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 12, 1, true, false));
        }
    }

    private void applyFiveTick(Player player) {
    }

    private void applyPigstepTick(Player player) {
        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 12, 1, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 2, true, false));
        }
    }

    private void applyTearsTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 12, 0, true, false));
        World world = player.getWorld();
        if (world.hasStorm()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 3, true, false));
        }
    }

    private void applyLavaChickenTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 12, 0, true, false));
        if (player.isInLava() && !player.isOnGround()) {
            Vector v = player.getVelocity();
            if (v.getY() < 0.15) player.setVelocity(new Vector(v.getX(), 0.15, v.getZ()));
        }
    }

    private void applyMellohiTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 12, 0, true, false));
    }

    private void applyWardTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 12, 0, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 12, 2, true, false));
    }

    private void applyCreatorTick(Player player) {
        int copper = countCopperIngots(player);
        String blockBelow = player.getLocation().clone().subtract(0, 1, 0).getBlock().getType().name();
        if (copper >= 64) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 2, true, false));
        } else if (copper >= 32) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 1, true, false));
        } else if (blockBelow.contains("COPPER")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 2, true, false));
        }
    }

    private void applyCreatorMusicBoxTick(Player player) {
        int copper = countCopperIngots(player);
        String blockBelow = player.getLocation().clone().subtract(0, 1, 0).getBlock().getType().name();
        if (copper >= 64) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 2, true, false));
        } else if (copper >= 32) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 2, true, false));
        } else if (blockBelow.contains("COPPER")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12, 3, true, false));
        }
    }

    private int countCopperIngots(Player player) {
        int count = 0;
        for (ItemStack i : player.getInventory().getContents()) {
            if (i != null && i.getType() == Material.COPPER_INGOT) count += i.getAmount();
        }
        return count;
    }

    public void onPlayerHit(Player attacker, Player victim, EntityDamageByEntityEvent event) {
        String disc = discInventory.getEquipped(attacker.getUniqueId());
        if (disc == null) return;

        switch (disc) {
            case "13" -> {
                int hits = hitTracker.incrementHitsDealt(attacker.getUniqueId());
                if (hits % 13 == 0) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                    victim.getWorld().spawnParticle(Particle.LARGE_SMOKE, victim.getLocation().add(0, 1, 0), 15, 0.3, 0.6, 0.3, 0.05);
                }
            }
            case "strad" -> {
                int combo = hitTracker.incrementCombo(attacker.getUniqueId(), victim.getUniqueId());
                if (combo >= 3) {
                    boolean naturalCrit = !attacker.isOnGround() && attacker.getFallDistance() > 0.0f && !attacker.isInWater();
                    event.setDamage(event.getDamage() * (naturalCrit ? 1.2 : 1.5));
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 30, 0.5, 0.8, 0.5, 0.2);
                    victim.getWorld().spawnParticle(Particle.ENCHANTED_HIT, victim.getLocation().add(0, 1, 0), 15, 0.4, 0.6, 0.4, 0.1);
                    attacker.playSound(attacker.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                    attacker.playSound(attacker.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.6f, 1.2f);
                }
            }
            case "11" -> {
                int hits = hitTracker.incrementHitsDealt(attacker.getUniqueId());
                if (hits % 11 == 0) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                    victim.getWorld().spawnParticle(Particle.LARGE_SMOKE, victim.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.04);
                    attacker.playSound(attacker.getLocation(), org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 0.8f, 1.6f);
                }
            }
            case "wait" -> {
                int hits = hitTracker.incrementHitsDealt(attacker.getUniqueId());
                if (hits % 10 == 0) {
                    final Player frozenTarget = victim;
                    frozenTarget.playSound(frozenTarget.getLocation(), org.bukkit.Sound.BLOCK_GLASS_BREAK, 1.0f, 0.6f);
                    new BukkitRunnable() {
                        int t = 0;
                        public void run() {
                            if (t >= 20 || !frozenTarget.isOnline()) { cancel(); return; }
                            frozenTarget.setVelocity(new Vector(0, Math.min(frozenTarget.getVelocity().getY(), 0), 0));
                            t++;
                        }
                    }.runTaskTimer(plugin, 0, 1);
                }
            }
            case "5" -> {
                int hits = hitTracker.incrementHitsDealt(attacker.getUniqueId());
                if (hits % 5 == 0) {
                    hitTracker.setRingActive(attacker.getUniqueId(), true);
                    spawnRingParticles(attacker);
                    attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.8f);
                    attacker.sendMessage("\u00a7b\u25c8 \u00a7fring charged \u00a78- \u00a77next sneak-attack hits for 1.5x");
                }
                if (attacker.isSneaking() && hitTracker.isRingActive(attacker.getUniqueId())) {
                    event.setDamage(event.getDamage() * 1.5);
                    hitTracker.setRingActive(attacker.getUniqueId(), false);
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.2);
                    attacker.playSound(attacker.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
                }
            }
            case "pigstep" -> {
                victim.setCooldown(Material.SHIELD, 160);
                ItemStack attackerMain = attacker.getInventory().getItemInMainHand();
                if (attackerMain.getType().name().contains("AXE") && Math.random() < 0.05) {
                    ItemStack victimMain = victim.getInventory().getItemInMainHand();
                    if (victimMain != null && victimMain.getItemMeta() instanceof Damageable dmg) {
                        int current = dmg.getDamage();
                        dmg.setDamage(current + 50);
                        victimMain.setItemMeta((ItemMeta) dmg);
                    }
                }
            }
            case "far" -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0));
                int hits = hitTracker.incrementHitsDealt(attacker.getUniqueId());
                if (hits % 20 == 0) {
                    spawnHeadCobweb(victim);
                }
            }
            case "relic" -> {
                if (Math.random() < 0.005) {
                    victim.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(Material.NETHERITE_SCRAP, 1));
                }
                if (abilityManager.isBountyActive(attacker.getUniqueId())) {
                    victim.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(Material.GOLD_INGOT, 5));
                    victim.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(Material.DIAMOND, 5));
                    victim.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(Material.EMERALD, 5));
                }
            }
            case "creator" -> {
                if (countCopperIngots(attacker) >= 64 && Math.random() < 0.10) {
                    victim.getWorld().strikeLightningEffect(victim.getLocation());
                }
            }
            case "creator_music_box" -> {
                if (countCopperIngots(attacker) >= 64 && Math.random() < 0.15) {
                    victim.getWorld().strikeLightningEffect(victim.getLocation());
                }
            }
            case "cat" -> {
                int hits = hitTracker.incrementHitsDealt(attacker.getUniqueId());
                if (hits % 20 == 0) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 400, 1));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 0));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 400, 1));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 400, 0));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 300, 0));
                    victim.playSound(victim.getLocation(), org.bukkit.Sound.ENTITY_CAT_PURREOW, 1.0f, 1.0f);
                    victim.getWorld().spawnParticle(Particle.LARGE_SMOKE, victim.getLocation().add(0, 1, 0), 20, 0.4, 0.8, 0.4, 0.05);
                    victim.getWorld().spawnParticle(Particle.WITCH, victim.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.08);
                }
            }
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE && event.getDamager() instanceof Arrow) {
            String attackerDisc = discInventory.getEquipped(attacker.getUniqueId());
            if ("precipice".equals(attackerDisc)) {
                event.setDamage(event.getDamage() * 1.35);
            }
        }
    }

    public void onPlayerTakeHit(Player victim, Player attacker, EntityDamageByEntityEvent event) {
        String disc = discInventory.getEquipped(victim.getUniqueId());
        if (disc == null) return;

        switch (disc) {
            case "stal" -> {
                if (Math.random() < 0.05) {
                    attacker.setVelocity(new Vector(0, 2.0, 0));
                }
            }
            case "strad" -> {
                Entity damager = event.getDamager();
                if (damager instanceof Projectile proj && proj.getType().name().equals("WIND_CHARGE")) {
                    if (Math.random() < 0.5) event.setCancelled(true);
                }
            }
            case "11" -> {
                int taken = hitTracker.incrementHitsTaken(victim.getUniqueId());
                if (taken % 11 == 0) {
                    WitherSkull skull = victim.getWorld().spawn(victim.getEyeLocation(), WitherSkull.class);
                    Vector dir = attacker.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
                    skull.setVelocity(dir.multiply(2.0));
                    skull.setShooter(victim);
                    skull.setCharged(true);
                    skull.setYield(5.0f);
                    victim.playSound(victim.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.4f);
                }
            }
            case "precipice" -> {
                if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE && event.getDamager() instanceof Arrow arrow) {
                    if (!(arrow.getShooter() instanceof Player shooter) || !shooter.equals(victim)) {
                        event.setCancelled(true);
                    }
                }
            }
            case "chirp" -> {
                int taken = hitTracker.incrementHitsTaken(victim.getUniqueId());
                if (taken % 10 == 0 && !trustManager.isTrusted(victim.getUniqueId(), attacker.getUniqueId())) {
                    final Player finalAttacker = attacker;
                    final Player finalVictim = victim;
                    for (int i = 0; i < 10; i++) {
                        final int delay = i * 2;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (!finalVictim.isOnline() || !finalAttacker.isOnline()) return;
                            Arrow arrow = finalVictim.getWorld().spawn(finalVictim.getEyeLocation(), Arrow.class);
                            arrow.setShooter(finalVictim);
                            arrow.setDamage(7.2);
                            Vector dir = finalAttacker.getLocation().add(0, 1, 0).toVector()
                                .subtract(finalVictim.getEyeLocation().toVector()).normalize();
                            arrow.setVelocity(dir.multiply(1.8));
                        }, delay);
                    }
                    victim.playSound(victim.getLocation(), org.bukkit.Sound.ENTITY_PARROT_HURT, 1.0f, 1.0f);
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }

    public void onPlayerTakeHitFromStrad(Player victim, EntityDamageByEntityEvent event) {
        String disc = discInventory.getEquipped(victim.getUniqueId());
        if (!"strad".equals(disc)) return;
        Entity damager = event.getDamager();
        if (damager instanceof Projectile proj && proj.getType().name().equals("WIND_CHARGE")) {
            if (Math.random() < 0.5) event.setCancelled(true);
        }
    }

    public void onBlockPlace(Player player, Material blockType, BlockPlaceEvent event) {
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (!"blocks".equals(disc)) return;
        if (blockType == Material.COBWEB) {
            if (Math.random() < 0.5) {
                event.setCancelled(true);
                player.getInventory().addItem(new ItemStack(Material.COBWEB, 1));
            }
        } else {
            if (Math.random() < 0.25) {
                event.setCancelled(true);
                player.getInventory().addItem(new ItemStack(blockType, 1));
            }
        }
    }

    public void onItemConsume(Player player, Material item, PlayerItemConsumeEvent event) {
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (disc == null) return;

        switch (disc) {
            case "otherside" -> {
                if (item == Material.SPIDER_EYE) {
                    if (Math.random() < 0.5) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
                    }
                }
            }
            case "5" -> {
                if (item == Material.ENCHANTED_GOLDEN_APPLE) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 9));
                }
            }
            case "mellohi" -> {
                if (item == Material.ENCHANTED_GOLDEN_APPLE) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 9));
                }
            }
        }
    }

    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player target)) return;
        String disc = discInventory.getEquipped(target.getUniqueId());
        if (disc == null) return;

        switch (disc) {
            case "5" -> {
                if (event.getEntity().getType() == EntityType.WARDEN) {
                    event.setCancelled(true);
                }
            }
            case "tears" -> {
                if (event.getEntity().getType() == EntityType.GHAST) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (!"mall".equals(disc)) return;

        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
            && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() == Material.TRIDENT) {
            main.addUnsafeEnchantment(Enchantment.RIPTIDE, 5);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            player.sendMessage("\u00a7d\u2726 \u00a7friptide V applied to trident.");
        }
    }

    public void onPlayerSneak(Player player, boolean sneaking) {
        if (!sneaking) return;
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (!"stal".equals(disc)) return;
        if (player.isOnGround()) return;
        if (cooldownManager.isOnCooldown(player.getUniqueId(), "stal_dj")) return;
        player.setVelocity(player.getVelocity().add(new Vector(0, 1.2, 0)));
        cooldownManager.setCooldown(player.getUniqueId(), "stal_dj", 10);
    }

    public void onStalAttack(Player attacker, Player victim, EntityDamageByEntityEvent event) {
        String disc = discInventory.getEquipped(attacker.getUniqueId());
        if (!"stal".equals(disc)) return;
        if (Math.random() < 0.075) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 0));
        }
    }

    public void onPrecipiceAutoFire(Player player) {
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (!"precipice".equals(disc)) return;
        if (Math.random() >= 0.10) return;
        Player nearest = player.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(player))
            .filter(p -> !trustManager.isTrusted(player.getUniqueId(), p.getUniqueId()))
            .min(java.util.Comparator.comparingDouble(p -> p.getLocation().distanceSquared(player.getLocation())))
            .orElse(null);
        if (nearest == null) return;
        Arrow arrow = player.getWorld().spawn(player.getEyeLocation(), Arrow.class);
        arrow.setShooter(player);
        arrow.setDamage(7.6);
        Vector dir = nearest.getLocation().add(0, 1, 0).toVector()
            .subtract(player.getEyeLocation().toVector()).normalize();
        arrow.setVelocity(dir.multiply(2.0));
    }

    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        String disc = discInventory.getEquipped(killer.getUniqueId());
        if (disc == null) return;

        if ("relic".equals(disc) && !(entity instanceof Player)) {
            event.getDrops().add(new ItemStack(Material.DIAMOND, 3));
            event.getDrops().add(new ItemStack(Material.EMERALD, 3));
            event.getDrops().add(new ItemStack(Material.GOLD_INGOT, 3));
        }

        if ("ward".equals(disc)) {
            ItemStack mainHand = killer.getInventory().getItemInMainHand();
            if (mainHand.getType().name().contains("SWORD")) {
                java.util.List<ItemStack> extras = new java.util.ArrayList<>();
                for (ItemStack drop : event.getDrops()) {
                    if (drop != null && drop.getType() != Material.AIR) {
                        extras.add(new ItemStack(drop.getType(), drop.getAmount() * 4));
                    }
                }
                event.getDrops().addAll(extras);
            }
        }
    }

    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (!"ward".equals(disc)) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() == Material.AIR) return;
        Collection<ItemStack> drops = event.getBlock().getDrops(tool);
        for (ItemStack drop : drops) {
            if (drop != null && drop.getType() != Material.AIR) {
                event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(),
                    new ItemStack(drop.getType(), drop.getAmount() * 4)
                );
            }
        }
    }

    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (!"cat".equals(disc)) return;
        if (event.getRightClicked() instanceof org.bukkit.entity.Cat cat) {
            cat.addPassenger(player);
            event.setCancelled(true);
        }
    }

    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        String disc = discInventory.getEquipped(player.getUniqueId());
        if (!"lava_chicken".equals(disc)) return;
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.LAVA
            || cause == EntityDamageEvent.DamageCause.FIRE
            || cause == EntityDamageEvent.DamageCause.FIRE_TICK
            || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
            event.setCancelled(true);
        }
    }

    private void spawnRingParticles(Player player) {
        double radius = 1.0;
        World world = player.getWorld();
        Location loc = player.getLocation().add(0, 0.5, 0);
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            world.spawnParticle(Particle.ELECTRIC_SPARK, loc.clone().add(x, 0, z), 1, 0, 0, 0, 0);
        }
    }

    private void spawnHeadCobweb(Player target) {
        Location base = target.getLocation();
        java.util.List<org.bukkit.block.Block> placed = new java.util.ArrayList<>();
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                org.bukkit.block.Block b = base.clone().add(dx, 1, dz).getBlock();
                if (b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) {
                    b.setType(Material.COBWEB);
                    placed.add(b);
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (org.bukkit.block.Block b : placed) {
                if (b.getType() == Material.COBWEB) b.setType(Material.AIR);
            }
        }, 80L);
    }
}
