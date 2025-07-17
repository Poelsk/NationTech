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
            plugin.getLogger().warning("technologies.yml not found. Creating default one."); // English message
            plugin.saveResource("technologies.yml", false);
            techFile = new File(plugin.getDataFolder(), "technologies.yml");
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(techFile);
        ConfigurationSection techSection = config.getConfigurationSection("technologies");

        if (techSection == null) {
            plugin.getLogger().warning("The 'technologies' section was not found in technologies.yml. Please ensure the format is correct."); // English message
            return;
        }

        for (String techId : techSection.getKeys(false)) {
            ConfigurationSection currentTechSection = techSection.getConfigurationSection(techId);
            if (currentTechSection == null) {
                plugin.getLogger().warning("Error loading technology '" + techId + "': Invalid configuration section."); // English message
                continue;
            }

            try {
                String name = currentTechSection.getString("name");
                String description = currentTechSection.getString("description");
                int cost = currentTechSection.getInt("cost");
                List<String> prerequisites = currentTechSection.getStringList("prerequisites");
                List<String> effects = currentTechSection.getStringList("effects");

                if (name == null || description == null) {
                    plugin.getLogger().warning("Technology '" + techId + "' is missing name or description. Skipping."); // English message
                    continue;
                }

                Technology technology = new Technology(techId, name, description, cost, prerequisites, effects);
                technologies.put(techId, technology);
                plugin.getLogger().info("Loaded technology: " + name + " (ID: " + techId + ")"); // English message

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error parsing technology '" + techId + "' in technologies.yml: " + e.getMessage(), e); // English message
            }
        }
        plugin.getLogger().info("Total technologies loaded: " + technologies.size()); // English message
    }

    public Technology getTechnology(String id) {
        return technologies.get(id);
    }

    public Map<String, Technology> getAllTechnologies() {
        return new HashMap<>(technologies);
    }
}