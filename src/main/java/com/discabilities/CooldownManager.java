package com.discabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public boolean isOnCooldown(UUID playerUUID, String discId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        if (playerCooldowns == null) return false;
        Long expiresAt = playerCooldowns.get(discId);
        return expiresAt != null && System.currentTimeMillis() < expiresAt;
    }

    public long getRemainingSeconds(UUID playerUUID, String discId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        if (playerCooldowns == null) return 0;
        Long expiresAt = playerCooldowns.get(discId);
        if (expiresAt == null) return 0;
        return Math.max(0, (expiresAt - System.currentTimeMillis()) / 1000);
    }

    public void setCooldown(UUID playerUUID, String discId, int cooldownSeconds) {
        cooldowns.computeIfAbsent(playerUUID, k -> new HashMap<>())
                .put(discId, System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }

    public void resetCooldowns(UUID playerUUID) {
        Map<String, Long> cd = cooldowns.get(playerUUID);
        if (cd != null) cd.clear();
    }
}
