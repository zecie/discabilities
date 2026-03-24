package com.discabilities;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscInventory {

    private final Map<UUID, String> slots = new HashMap<>();
    private final File dataFile;
    private final Plugin plugin;

    public DiscInventory(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        plugin.getDataFolder().mkdirs();
        loadAll();
    }

    public String getEquipped(UUID uuid) {
        return slots.get(uuid);
    }

    public void equip(UUID uuid, String discId) {
        slots.put(uuid, discId);
    }

    public void clear(UUID uuid) {
        slots.remove(uuid);
    }

    public boolean hasDisc(UUID uuid) {
        return slots.containsKey(uuid) && slots.get(uuid) != null;
    }

    public void saveAll() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : slots.entrySet()) {
            if (entry.getValue() != null) {
                config.set(entry.getKey().toString(), entry.getValue());
            }
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save disc playerdata: " + e.getMessage());
        }
    }

    private void loadAll() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String discId = config.getString(key);
                if (discId != null) {
                    slots.put(uuid, discId);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
