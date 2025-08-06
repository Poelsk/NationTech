package com.nationtech.tech;

import com.nationtech.NationTech;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;

public class TechnologyManager {

    private final NationTech plugin;
    private final Map<String, Technology> technologies;

    public TechnologyManager(NationTech plugin) {
        this.plugin = plugin;
        this.technologies = new HashMap<>();
        loadTechnologies();
    }

    public void loadTechnologies() {
        technologies.clear();
        File techFile = new File(plugin.getDataFolder(), "technologies.yml");
        if (!techFile.exists()) {
            plugin.getLogger().warning("technologies.yml not found. Creating default one.");
            plugin.saveResource("technologies.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(techFile);
        ConfigurationSection techSection = config.getConfigurationSection("technologies");

        if (techSection == null) {
            plugin.getLogger().warning("The 'technologies' section was not found in technologies.yml. Please ensure the format is correct.");
            return;
        }

        for (String techId : techSection.getKeys(false)) {
            ConfigurationSection currentTechSection = techSection.getConfigurationSection(techId);
            if (currentTechSection == null) {
                plugin.getLogger().warning("Error loading technology '" + techId + "': Invalid configuration section.");
                continue;
            }

            try {
                String name = currentTechSection.getString("name");
                String description = currentTechSection.getString("description");
                int cost = currentTechSection.getInt("cost");
                String icon = currentTechSection.getString("icon");
                List<String> prerequisites = currentTechSection.getStringList("prerequisites");
                List<String> effects = currentTechSection.getStringList("effects");

                if (name == null || description == null) {
                    plugin.getLogger().warning("Technology '" + techId + "' is missing name or description. Skipping.");
                    continue;
                }

                Technology technology = new Technology(techId, name, description, cost, icon, prerequisites, effects);
                technologies.put(techId, technology);
                plugin.getLogger().info("Loaded technology: " + name + " (ID: " + techId + ")");

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error parsing technology '" + techId + "' in technologies.yml: " + e.getMessage(), e);
            }
        }
        plugin.getLogger().info("Total technologies loaded: " + technologies.size());
    }

    public Technology getTechnology(String id) {
        return technologies.get(id);
    }

    public Map<String, Technology> getAllTechnologies() {
        return new HashMap<>(technologies);
    }
}