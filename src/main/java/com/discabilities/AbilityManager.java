package com.discabilities;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AbilityManager {

    public record AbilityDef(String id, @Nullable Material material, int cooldownSecs) {}

    @FunctionalInterface
    public interface Activator {
        void activate(Player player, @Nullable Player lastHit);
    }

    private final Map<String, AbilityDef> defs = new HashMap<>();
    private final Map<String, Activator> activators = new HashMap<>();
    private final Map<Material, String> materialToId = new EnumMap<>(Material.class);
    private final NamespacedKey discKey;
    private final Plugin plugin;
    private Set<UUID> bountyPlayers = new HashSet<>();

    public boolean isBountyActive(UUID uuid) { return bountyPlayers.contains(uuid); }

    public static final Set<Material> VALUABLE_BLOCKS = new HashSet<>(Arrays.asList(
        Material.ENCHANTING_TABLE,
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.ENDER_CHEST,
        Material.DIAMOND_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE,
        Material.DEEPSLATE_EMERALD_ORE,
        Material.ANCIENT_DEBRIS,
        Material.NETHER_GOLD_ORE,
        Material.NETHER_QUARTZ_ORE,
        Material.IRON_ORE,
        Material.DEEPSLATE_IRON_ORE,
        Material.GOLD_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.BARREL,
        Material.HOPPER,
        Material.DROPPER,
        Material.DISPENSER,
        Material.AIR,
        Material.CAVE_AIR,
        Material.VOID_AIR
    ));

    private final Map<String, List<String>> discLore = new HashMap<>();
    private TrustManager trustManager;

    public AbilityManager(Plugin plugin, CooldownManager cooldowns, CombatTracker combatTracker) {
        this.plugin = plugin;
        this.discKey = new NamespacedKey(plugin, "disc_id");
        registerAll();
        initLore();
    }

    public void setTrustManager(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    private boolean trusted(Player player, Player target) {
        return trustManager != null && trustManager.isTrusted(player.getUniqueId(), target.getUniqueId());
    }

    private void reg(String id, @Nullable Material mat, int cd, Activator activator) {
        defs.put(id, new AbilityDef(id, mat, cd));
        activators.put(id, activator);
        if (mat != null) materialToId.put(mat, id);
    }

    private void reg(String id, String matName, int cd, Activator activator) {
        reg(id, Material.matchMaterial(matName), cd, activator);
    }

    private void registerAll() {
        reg("13", "MUSIC_DISC_13", 90, this::activateThirteen);
        reg("blocks", "MUSIC_DISC_BLOCKS", 60, this::activateBlocks);
        reg("mall", "MUSIC_DISC_MALL", 100, this::activateMall);
        reg("stal", "MUSIC_DISC_STAL", 135, this::activateStal);
        reg("strad", "MUSIC_DISC_STRAD", 110, this::activateStrad);
        reg("11", "MUSIC_DISC_11", 115, this::activateEleven);
        reg("wait", "MUSIC_DISC_WAIT", 120, this::activateWait);
        reg("precipice", "MUSIC_DISC_PRECIPICE", 85, this::activatePrecipice);
        reg("otherside", "MUSIC_DISC_OTHERSIDE", 145, this::activateOtherside);
        reg("5", "MUSIC_DISC_5", 110, this::activateFive);
        reg("lava_chicken", "MUSIC_DISC_PIGSTEP", 240, this::activateLavaChicken);
        reg("pigstep", "MUSIC_DISC_PIGSTEP", 175, this::activatePigstep);
        reg("tears", "MUSIC_DISC_TEARS", 145, this::activateTears);
        reg("mellohi", "MUSIC_DISC_MELLOHI", 150, this::activateMellohi);
        reg("far", "MUSIC_DISC_FAR", 250, this::activateFar);
        reg("relic", "MUSIC_DISC_RELIC", 115, this::activateRelic);
        reg("ward", "MUSIC_DISC_WARD", 85, this::activateWard);
        reg("creator_music_box", "MUSIC_DISC_CREATOR_MUSIC_BOX", 30, this::activateCreatorMusicBox);
        reg("creator", "MUSIC_DISC_CREATOR", 15, this::activateCreator);
        reg("chirp", "MUSIC_DISC_CHIRP", 275, this::activateChirp);
        reg("cat", "MUSIC_DISC_CAT", 145, this::activateCat);
    }

    @Nullable
    public AbilityDef getAbilityDef(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String id = meta.getPersistentDataContainer().get(discKey, PersistentDataType.STRING);
            if (id != null) return defs.get(id);
        }
        String id = materialToId.get(item.getType());
        return id != null ? defs.get(id) : null;
    }

    @Nullable
    public AbilityDef getAbilityDefById(String id) {
        return defs.get(id);
    }

    public void activate(AbilityDef def, Player player, @Nullable Player lastHit) {
        Activator activator = activators.get(def.id());
        if (activator != null) activator.activate(player, lastHit);
    }

    public ItemStack createDiscItem(String id) {
        AbilityDef def = defs.get(id);
        if (def == null || def.material() == null) return null;
        ItemStack item = new ItemStack(def.material());
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(discKey, PersistentDataType.STRING, id);
        meta.setDisplayName("\u00a7r\u00a7d\u00a7l" + discDisplayName(id));
        List<String> lore = discLore.get(id);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String discDisplayName(String id) {
        return switch (id) {
            case "5" -> "Disc 5";
            case "11" -> "Disc 11";
            case "13" -> "Disc 13";
            case "lava_chicken" -> "Lava Chicken";
            case "creator_music_box" -> "Creator Music Box";
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

    private void initLore() {
        l("13",      "Pulls all players within 15 blocks toward you.",
                     "Strength I with 1 Wither Skull; Strength II with 4",
                     "Every 13th hit inflicts Wither on the target");
        l("blocks",  "Clears all non-valuable blocks in a 5-block radius.",
                     "50% chance Cobwebs don't consume when placed",
                     "25% chance any placed block doesn't consume",
                     "Efficiency IX while holding any mining tool");
        l("mall",    "Strikes the last hit player with 3 lightning bolts.",
                     "Speed III in rain; Speed IV in thunderstorms",
                     "Right-clicking a Trident gives Riptide V");
        l("stal",    "Creates a 10-second vortex pulling players within 20 blocks.",
                     "5% chance to launch your attacker into the air",
                     "Double Jump by sneaking while airborne (10s cooldown)",
                     "7.5% chance Slow Falling on players you hit");
        l("strad",   "Launches the last hit player up, then slams them down.",
                     "50% chance Wind Charges don't affect you",
                     "After a 2-hit combo every subsequent hit auto-crits");
        l("11",      "Fires 3 charged Wither Skulls at the 3 nearest players.",
                     "Soul Sand grants Speed III and Strength III",
                     "Every 11th hit inflicts Wither on the target",
                     "Every 11th hit taken fires a Wither Skull back");
        l("wait",    "Completely freezes the last hit player for 5 seconds.",
                     "Every 10th hit freezes the target for 1 second",
                     "Speed III in icy biomes");
        l("precipice","Fires a homing barrage of arrows at the last hit player.",
                     "Bows deal 35% more damage",
                     "Arrows shot by enemies bounce off you",
                     "10% chance per second to auto-fire at the nearest player");
        l("otherside","50/50 gamble — one player gets buffed, the other gets debuffed.",
                     "Spider Eye gives random Poison or Resistance",
                     "Strength II outside the Overworld");
        l("5",       "Fires a Warden sonic beam up to 40 blocks ahead.",
                     "Every 5 hits charges a Ring — next sneak-attack hits for 1.5x",
                     "Enchanted Golden Apples grant 10 Absorption hearts",
                     "Wardens ignore you completely");
        l("pigstep", "Grants Haste X for 15 seconds.",
                     "Strength II and Speed III in the Nether",
                     "Shield disables last 8 seconds instead of the default",
                     "5% chance Axe hits drain significantly more durability");
        l("tears",   "Summons a rain cloud on the last hit player for 7 seconds.",
                     "Permanent Dolphins Grace",
                     "Ghasts will not target or attack you",
                     "Speed IV during rain");
        l("lava_chicken","Removes Fire Resistance from all players within 15 blocks.",
                     "Permanent Fire Resistance",
                     "Swim through lava like water");
        l("mellohi", "Instantly heals you to 20 hearts for 20 seconds.",
                     "Permanent Regeneration I",
                     "Enchanted Golden Apples grant 20 Absorption hearts");
        l("far",     "Inflicts Glowing on all players within 50 blocks for 30s.",
                     "Every hit inflicts Glowing on the target for 1 second",
                     "Every 20th hit spawns a 2x2 Cobweb on the target's head");
        l("relic",   "Bounty mode for 15s — hits drop 5 gold, 5 diamonds, 5 emeralds.",
                     "Every mob kill drops 3 diamonds, 3 emeralds, and 3 gold",
                     "0.5% chance hitting a player forces them to drop Netherite Scrap");
        l("ward",    "Hero of the Village 255 for 15 seconds.",
                     "Swords apply Looting V on kills",
                     "Tools apply Fortune V on block breaks",
                     "Permanent Luck and Hero of the Village III");
        l("creator_music_box","Large velocity dash in your look direction.",
                     "Speed IV while standing on copper blocks",
                     "32 copper in inventory grants Speed III",
                     "64 copper grants Speed III + 15% chance to strike lightning");
        l("creator", "Small velocity dash in your look direction.",
                     "Speed III while standing on copper blocks",
                     "32 copper in inventory grants Speed II",
                     "64 copper grants Speed III + 10% chance to strike lightning");
        l("chirp",   "Grants flight for 5 seconds.",
                     "Every 10th hit taken fires 10 bird beak projectiles at attacker");
        l("cat",     "Speed VI and Jump Boost IV for 10 seconds.",
                     "Right-click a Cat to ride it like a horse",
                     "Every 20th hit applies Cat Poison — Poison II, Wither I, Slowness II for 20s");
    }

    private void l(String id, String abilityDesc, String... passives) {
        AbilityDef def = defs.get(id);
        int cd = def != null ? def.cooldownSecs() : 0;
        List<String> lore = new ArrayList<>();
        lore.add("\u00a7r\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        lore.add("\u00a7r\u00a75\u26a1 \u00a7f\u00a7lAbility  \u00a7r\u00a78[\u00a77" + cd + "s\u00a78]");
        lore.add("\u00a7r\u00a77" + abilityDesc);
        lore.add("\u00a7r\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
        lore.add("\u00a7r\u00a75\u25c8 \u00a7f\u00a7lPassives");
        for (String p : passives) lore.add("\u00a7r\u00a77 \u25b8 " + p);
        discLore.put(id, lore);
    }

    public void tagItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        String id = materialToId.get(item.getType());
        if (id == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta.getPersistentDataContainer().has(discKey, PersistentDataType.STRING)) return;
        meta.getPersistentDataContainer().set(discKey, PersistentDataType.STRING, id);
        meta.setDisplayName("\u00a7r\u00a7d\u00a7l" + discDisplayName(id));
        List<String> lore = discLore.get(id);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public Set<String> getDiscIds() {
        return defs.keySet();
    }

    private void msg(Player p, String text) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        p.sendMessage("\u00a7c\u25c6 \u00a7e" + text);
    }

    private void activateThirteen(Player player, @Nullable Player lastHit) {
        World world = player.getWorld();
        world.spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 50, 2.5, 1, 2.5, 0.06);
        world.spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 30, 2, 1, 2, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.6f);
        int pulled = 0;
        for (Entity e : player.getNearbyEntities(15, 15, 15)) {
            if (!(e instanceof Player target)) continue;
            if (trusted(player, target)) continue;
            Vector toward = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
            target.setVelocity(target.getVelocity().add(toward.multiply(1.5)));
            world.spawnParticle(Particle.LARGE_SMOKE, target.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.05);
            pulled++;
        }
        if (pulled == 0) player.sendMessage("\u00a77\u25c6 \u00a7eNo players in range.");
    }

    private void activateBlocks(Player player, @Nullable Player lastHit) {
        Location center = player.getLocation();
        int radius = 5;
        int cleared = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = center.clone().add(x, y, z).getBlock();
                    Material type = b.getType();
                    if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR) continue;
                    if (VALUABLE_BLOCKS.contains(type)) continue;
                    if (type.name().contains("SHULKER_BOX")) continue;
                    b.setType(Material.AIR);
                    cleared++;
                }
            }
        }
        player.getWorld().spawnParticle(Particle.CLOUD, center, 60, 3, 1.5, 3, 0.08);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 30, 2, 1, 2, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.7f);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.4f);
        player.sendMessage("\u00a77\u25c6 \u00a7aCleared \u00a7f" + cleared + "\u00a7a Blocks.");
    }

    private void activateMall(Player player, @Nullable Player lastHit) {
        if (lastHit == null) { msg(player, "No target!"); return; }
        if (trusted(player, lastHit)) { msg(player, "No target!"); return; }
        World world = lastHit.getWorld();
        final Player target = lastHit;
        world.strikeLightningEffect(target.getLocation());
        target.damage(21.2, player);
        world.spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.2);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!target.isOnline()) return;
            world.strikeLightningEffect(target.getLocation());
            target.damage(21.2, player);
        }, 5L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!target.isOnline()) return;
            world.strikeLightningEffect(target.getLocation());
            target.damage(21.2, player);
            world.spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.15);
        }, 10L);
    }

    private void activateStal(Player player, @Nullable Player lastHit) {
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 1.0f, 0.4f);
        World world = player.getWorld();
        new BukkitRunnable() {
            int elapsed = 0;
            public void run() {
                if (elapsed >= 20 || !player.isOnline()) { cancel(); return; }
                if (elapsed % 1 == 0) {
                    for (Entity e : player.getNearbyEntities(20, 20, 20)) {
                        if (!(e instanceof Player target)) continue;
                        if (trusted(player, target)) continue;
                        Vector toward = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
                        target.setVelocity(target.getVelocity().add(toward.multiply(1.2)));
                        world.spawnParticle(Particle.NAUTILUS, target.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.05);
                    }
                }
                elapsed++;
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    private void activateStrad(Player player, @Nullable Player lastHit) {
        if (lastHit == null) { msg(player, "No target!"); return; }
        if (trusted(player, lastHit)) { msg(player, "No target!"); return; }
        final Player target = lastHit;
        target.setVelocity(new Vector(0, 5, 0));
        target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation(), 20, 0.4, 0.2, 0.4, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.6f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!target.isOnline()) return;
            target.damage(33.6, player);
            target.setVelocity(new Vector(0, -4, 0));
            target.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, target.getLocation(), 3, 0.2, 0.2, 0.2, 0);
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.2);
            target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.7f);
        }, 30L);
    }

    private void activateEleven(Player player, @Nullable Player lastHit) {
        List<Player> targets = player.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(player))
            .filter(p -> !trusted(player, p))
            .sorted(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(player.getLocation())))
            .limit(3)
            .collect(Collectors.toList());
        for (Player target : targets) {
            WitherSkull skull = player.getWorld().spawn(player.getEyeLocation(), WitherSkull.class);
            Vector dir = target.getLocation().add(0, 1, 0).toVector()
                .subtract(player.getEyeLocation().toVector()).normalize();
            skull.setVelocity(dir.multiply(2.5));
            skull.setShooter(player);
            skull.setCharged(true);
            skull.setYield(10.0f);
            target.damage(40.0, player);
        }
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getEyeLocation(), 30, 0.4, 0.3, 0.4, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.2f);
    }

    private void activateWait(Player player, @Nullable Player lastHit) {
        if (lastHit == null) { msg(player, "No target!"); return; }
        if (trusted(player, lastHit)) { msg(player, "No target!"); return; }
        final Player frozen = lastHit;
        frozen.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 254));
        frozen.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
        frozen.playSound(frozen.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        frozen.getWorld().spawnParticle(Particle.END_ROD, frozen.getLocation().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0.03);
        new BukkitRunnable() {
            int t = 0;
            public void run() {
                if (t >= 100 || !frozen.isOnline()) { cancel(); return; }
                Vector v = frozen.getVelocity();
                frozen.setVelocity(new Vector(0, Math.min(v.getY(), 0), 0));
                frozen.getWorld().spawnParticle(Particle.END_ROD, frozen.getLocation().add(0, 1, 0), 3, 0.4, 0.8, 0.4, 0.02);
                t++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void activatePrecipice(Player player, @Nullable Player lastHit) {
        if (lastHit == null) { msg(player, "No target!"); return; }
        if (trusted(player, lastHit)) { msg(player, "No target!"); return; }
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.6f);
        int count = 18 + new Random().nextInt(5);
        List<Arrow> arrows = new ArrayList<>();
        Location eye = player.getEyeLocation();
        for (int i = 0; i < count; i++) {
            Arrow arrow = player.getWorld().spawn(eye, Arrow.class);
            arrow.setShooter(player);
            arrow.setDamage(14.4);
            Vector toTarget = lastHit.getLocation().add(0, 1, 0).toVector()
                .subtract(eye.toVector()).normalize();
            arrow.setVelocity(toTarget.multiply(2.0));
            arrows.add(arrow);
        }
        final Player finalTarget = lastHit;
        new BukkitRunnable() {
            int ticks = 0;
            public void run() {
                if (ticks >= 60 || !finalTarget.isOnline()) { cancel(); return; }
                arrows.removeIf(a -> !a.isValid() || a.isDead());
                for (Arrow arrow : arrows) {
                    if (!arrow.isValid()) continue;
                    Vector toTarget = finalTarget.getLocation().add(0, 1, 0).toVector()
                        .subtract(arrow.getLocation().toVector()).normalize();
                    arrow.setVelocity(arrow.getVelocity().multiply(0.8).add(toTarget.multiply(0.5)));
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 2, 1);
    }

    private void activateOtherside(Player player, @Nullable Player lastHit) {
        if (lastHit == null) { msg(player, "No target!"); return; }
        if (trusted(player, lastHit)) { msg(player, "No target!"); return; }
        int dur = 300;
        boolean playerWins = Math.random() < 0.5;
        Player winner = playerWins ? player : lastHit;
        Player loser = playerWins ? lastHit : player;
        winner.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, 3));
        winner.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, dur, 2));
        winner.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, dur, 1));
        loser.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, dur, 2));
        loser.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, dur, 2));
        loser.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
        loser.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 80, 0));
        loser.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, 3));
        winner.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, winner.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.2);
        loser.getWorld().spawnParticle(Particle.LARGE_SMOKE, loser.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.05);
        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        loser.playSound(loser.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 0.7f);
        winner.sendMessage("\u00a7a\u25c6 \u00a7fYou won the gamble \u00a78\u2014 \u00a7aSpeed IV, Strength III, Resistance II for 15s");
        loser.sendMessage("\u00a7c\u25c6 \u00a7fYou lost the gamble \u00a78\u2014 \u00a7cWeakness, Mining Fatigue, Blindness, Nausea, Slowness");
    }

    private void activateFive(Player player, @Nullable Player lastHit) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        World world = player.getWorld();
        for (double d = 0; d <= 40; d += 0.5) {
            Location point = start.clone().add(dir.clone().multiply(d));
            world.spawnParticle(Particle.END_ROD, point, 1, 0.05, 0.05, 0.05, 0);
        }
        RayTraceResult result = world.rayTraceEntities(start, dir, 40,
            e -> e != player && e instanceof LivingEntity);
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.5f);
        if (result != null && result.getHitEntity() instanceof LivingEntity hit) {
            ((LivingEntity) hit).damage(50.0, player);
            world.spawnParticle(Particle.EXPLOSION_EMITTER, hit.getLocation(), 4, 0.3, 0.3, 0.3, 0);
            world.spawnParticle(Particle.SONIC_BOOM, hit.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
            world.playSound(hit.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        }
    }

    private void activatePigstep(Player player, @Nullable Player lastHit) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 300, 9));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.8f);
        World world = player.getWorld();
        new BukkitRunnable() {
            double angle = 0;
            int ticks = 0;
            public void run() {
                if (ticks >= 40) { cancel(); return; }
                double x = Math.cos(angle) * 1.0;
                double z = Math.sin(angle) * 1.0;
                world.spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(x, 0.5, z), 2, 0.1, 0.2, 0.1, 0.05);
                angle += 0.5;
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void activateTears(Player player, @Nullable Player lastHit) {
        if (lastHit == null) { msg(player, "No target!"); return; }
        if (trusted(player, lastHit)) { msg(player, "No target!"); return; }
        player.playSound(player.getLocation(), Sound.WEATHER_RAIN, 1.0f, 0.8f);
        final Player target = lastHit;
        World world = target.getWorld();
        new BukkitRunnable() {
            int elapsed = 0;
            public void run() {
                if (elapsed >= 70 || !target.isOnline()) { cancel(); return; }
                if (elapsed % 2 == 0) {
                    target.damage(6.72, player);
                    world.spawnParticle(Particle.DRIPPING_WATER, target.getLocation().add(0, 3.5, 0), 8, 0.5, 0.3, 0.5, 0.02);
                }
                elapsed++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private void activateLavaChicken(Player player, @Nullable Player lastHit) {
        for (Entity e : player.getNearbyEntities(15, 15, 15)) {
            if (!(e instanceof Player target)) continue;
            if (trusted(player, target)) continue;
            target.removePotionEffect(org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE);
        }
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 60, 1.0, 0.5, 1.0, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
    }

    private void activateMellohi(Player player, @Nullable Player lastHit) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 400, 4));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) player.setHealth(40.0);
        }, 1L);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
    }

    private void activateFar(Player player, @Nullable Player lastHit) {
        for (Entity e : player.getNearbyEntities(50, 50, 50)) {
            if (!(e instanceof Player target)) continue;
            if (trusted(player, target)) continue;
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0));
        }
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 50, 1.0, 0.5, 1.0, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
    }

    private void activateRelic(Player player, @Nullable Player lastHit) {
        bountyPlayers.add(player.getUniqueId());
        player.sendMessage("\u00a7e\u25c6 \u00a7fBounty active for 15 seconds.");
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> bountyPlayers.remove(player.getUniqueId()), 300L);
    }

    private void activateWard(Player player, @Nullable Player lastHit) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 300, 254));
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_RESONATE, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
    }

    private void activateCreatorMusicBox(Player player, @Nullable Player lastHit) {
        Vector dir = player.getLocation().getDirection().normalize();
        player.setVelocity(dir.multiply(5.5).add(new Vector(0, 0.5, 0)));
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 30, 0.5, 0.3, 0.5, 0.1);
    }

    private void activateCreator(Player player, @Nullable Player lastHit) {
        Vector dir = player.getLocation().getDirection().normalize();
        player.setVelocity(dir.multiply(3.5).add(new Vector(0, 0.3, 0)));
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 30, 0.5, 0.3, 0.5, 0.1);
    }

    private void activateChirp(Player player, @Nullable Player lastHit) {
        player.setAllowFlight(true);
        player.setFlying(true);
        player.playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.setFlying(false);
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                player.setAllowFlight(false);
            }
        }, 100L);
    }

    private void activateCat(Player player, @Nullable Player lastHit) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 5));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 3));
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_PURREOW, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
    }

    @Nullable
    private Player getNearestPlayer(Player player) {
        return player.getWorld().getPlayers().stream()
            .filter(p -> !p.equals(player))
            .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(player.getLocation())))
            .orElse(null);
    }
}
