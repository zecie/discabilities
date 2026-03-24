package com.discabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HitTracker {

    private final Map<UUID, Integer> hitsDealt = new HashMap<>();
    private final Map<UUID, Integer> hitsTaken = new HashMap<>();
    private final Map<UUID, Integer> comboCount = new HashMap<>();
    private final Map<UUID, UUID> comboTarget = new HashMap<>();
    private final Map<UUID, Long> comboLastHit = new HashMap<>();
    private final Map<UUID, Boolean> ringActive = new HashMap<>();

    public int incrementHitsDealt(UUID uuid) {
        int val = hitsDealt.getOrDefault(uuid, 0) + 1;
        hitsDealt.put(uuid, val);
        return val;
    }

    public int incrementHitsTaken(UUID uuid) {
        int val = hitsTaken.getOrDefault(uuid, 0) + 1;
        hitsTaken.put(uuid, val);
        return val;
    }

    public int getHitsDealt(UUID uuid) {
        return hitsDealt.getOrDefault(uuid, 0);
    }

    public int getHitsTaken(UUID uuid) {
        return hitsTaken.getOrDefault(uuid, 0);
    }

    public void resetHitsDealt(UUID uuid) {
        hitsDealt.remove(uuid);
    }

    public void resetHitsTaken(UUID uuid) {
        hitsTaken.remove(uuid);
    }

    public int incrementCombo(UUID attacker, UUID target) {
        long now = System.currentTimeMillis();
        UUID lastTarget = comboTarget.get(attacker);
        Long lastTime = comboLastHit.get(attacker);
        if (lastTarget == null || !lastTarget.equals(target) || lastTime == null || (now - lastTime) > 3000) {
            comboCount.put(attacker, 1);
        } else {
            comboCount.put(attacker, comboCount.getOrDefault(attacker, 0) + 1);
        }
        comboTarget.put(attacker, target);
        comboLastHit.put(attacker, now);
        return comboCount.get(attacker);
    }

    public int getCombo(UUID uuid) {
        return comboCount.getOrDefault(uuid, 0);
    }

    public void resetCombo(UUID uuid) {
        comboCount.remove(uuid);
        comboTarget.remove(uuid);
        comboLastHit.remove(uuid);
    }

    public boolean isRingActive(UUID uuid) {
        return ringActive.getOrDefault(uuid, false);
    }

    public void setRingActive(UUID uuid, boolean active) {
        ringActive.put(uuid, active);
    }

    public void reset(UUID uuid) {
        hitsDealt.remove(uuid);
        hitsTaken.remove(uuid);
        comboCount.remove(uuid);
        comboTarget.remove(uuid);
        comboLastHit.remove(uuid);
        ringActive.remove(uuid);
    }
}
