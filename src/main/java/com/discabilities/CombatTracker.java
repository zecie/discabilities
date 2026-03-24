package com.discabilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTracker {

    private final Map<UUID, UUID> lastHit = new HashMap<>();

    public void setLastHit(UUID attacker, UUID target) {
        lastHit.put(attacker, target);
    }

    @Nullable
    public Player getLastHitPlayer(UUID attacker) {
        UUID targetUUID = lastHit.get(attacker);
        if (targetUUID == null) return null;
        return Bukkit.getPlayer(targetUUID);
    }
}
