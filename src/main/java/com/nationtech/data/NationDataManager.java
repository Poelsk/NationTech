package com.nationtech.data;

import com.nationtech.NationTech;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class NationDataManager {

    private final NationTech plugin;
    private final Map<UUID, NationTechnologyData> nationDataMap;
    private final File dataFolder;

    public NationDataManager(NationTech plugin) {
        this.plugin = plugin;
        this.nationDataMap = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        loadAllNationData();
    }

    public NationTechnologyData getNationData(UUID uuid) {
        return nationDataMap.computeIfAbsent(uuid, k -> new NationTechnologyData(uuid));
    }

    public void loadAllNationData() {
        nationDataMap.clear();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                NationTechnologyData data = NationTechnologyData.fromConfiguration(uuid, config);
                nationDataMap.put(uuid, data);
                plugin.getLogger().info("Loaded data for: " + uuid.toString());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not parse UUID from file name: " + file.getName());
            }
        }
        plugin.getLogger().info("Total nation/player data loaded: " + nationDataMap.size());
    }

    public void saveNationData(UUID uuid) {
        NationTechnologyData data = nationDataMap.get(uuid);
        if (data == null) return;

        File file = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        data.save(config);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data for " + uuid, e);
        }
    }

    public void saveAllNationData() {
        plugin.getLogger().info("Saving all nation data...");
        for (UUID uuid : nationDataMap.keySet()) {
            saveNationData(uuid);
        }
        plugin.getLogger().info("All nation data saved.");
    }
}