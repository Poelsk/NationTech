package com.nationtech.data;

import com.nationtech.NationTech;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class NationDataManager {

    private final NationTech plugin;
    private final File dataFolder;
    private final Map<UUID, NationTechnologyData> nationDataMap;

    public NationDataManager(NationTech plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "nations_data"); // Subfolder for nation data
        this.nationDataMap = new HashMap<>();

        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // Create the subfolder if it doesn't exist
        }

        loadAllNationData(); // Load all nation data when the manager is initialized
    }

    public void loadAllNationData() {
        nationDataMap.clear(); // Clear existing data before reloading

        File[] dataFiles = dataFolder.listFiles();
        if (dataFiles == null) {
            plugin.getLogger().info("No existing nation data files found.");
            return;
        }

        for (File file : dataFiles) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                try {
                    UUID nationUUID = UUID.fromString(file.getName().replace(".yml", ""));
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                    int technologyPoints = config.getInt("technologyPoints", 0);
                    List<String> unlockedTechnologies = config.getStringList("unlockedTechnologies");

                    NationTechnologyData data = new NationTechnologyData(nationUUID, technologyPoints, unlockedTechnologies);
                    nationDataMap.put(nationUUID, data);
                    plugin.getLogger().info("Loaded data for nation: " + nationUUID);

                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in file name: " + file.getName());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error loading nation data from file: " + file.getName(), e);
                }
            }
        }
        plugin.getLogger().info("Total nation data loaded: " + nationDataMap.size());
    }

    public void saveNationData(UUID nationUUID) {
        NationTechnologyData data = nationDataMap.get(nationUUID);
        if (data == null) {
            plugin.getLogger().warning("Attempted to save non-existent data for nation: " + nationUUID);
            return;
        }

        File file = new File(dataFolder, nationUUID.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("technologyPoints", data.getTechnologyPoints());
        config.set("unlockedTechnologies", data.getUnlockedTechnologies());

        try {
            config.save(file);
            // plugin.getLogger().info("Saved data for nation: " + nationUUID); // Log this only if really needed, can be spammy
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data for nation: " + nationUUID, e);
        }
    }

    public NationTechnologyData getNationData(UUID nationUUID) {
        // If data is not in map, create new default data and save it
        return nationDataMap.computeIfAbsent(nationUUID, uuid -> {
            plugin.getLogger().info("Creating new data for nation: " + uuid);
            NationTechnologyData newData = new NationTechnologyData(uuid);
            saveNationData(uuid); // Save initial empty data
            return newData;
        });
    }

    public Map<UUID, NationTechnologyData> getAllNationData() {
        return new HashMap<>(nationDataMap); // Return a copy
    }

    // Call this method to save all data when plugin disables
    public void saveAllNationData() {
        plugin.getLogger().info("Saving all nation data...");
        for (UUID uuid : nationDataMap.keySet()) {
            saveNationData(uuid);
        }
        plugin.getLogger().info("All nation data saved.");
    }
}