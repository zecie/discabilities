package com.discabilities;

import java.util.*;

public class TrustManager {

    private final Map<UUID, Set<UUID>> trustMap = new HashMap<>();

    public void trust(UUID owner, UUID target) {
        trustMap.computeIfAbsent(owner, k -> new HashSet<>()).add(target);
    }

    public void untrust(UUID owner, UUID target) {
        Set<UUID> set = trustMap.get(owner);
        if (set != null) set.remove(target);
    }

    public boolean isTrusted(UUID owner, UUID target) {
        Set<UUID> set = trustMap.get(owner);
        return set != null && set.contains(target);
    }
}
