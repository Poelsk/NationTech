package com.nationtech;

import com.nationtech.tech.Technology;
import com.nationtech.tech.TechnologyManager;
import com.nationtech.data.NationDataManager;
import com.nationtech.listeners.TechnologyPointsListener; // Import the new listener class
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager; // Import PluginManager

public class NationTech extends JavaPlugin {

    private static NationTech instance;
    private TechnologyManager technologyManager;
    private NationDataManager nationDataManager;
    private boolean townyEnabled;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("NationTech is enabling...");

        saveDefaultConfig();
        saveResource("technologies.yml", false);

        this.technologyManager = new TechnologyManager(this);

        // Towny detection
        if (getServer().getPluginManager().getPlugin("Towny") != null && getServer().getPluginManager().isPluginEnabled("Towny")) {
            this.townyEnabled = true;
            getLogger().info("Towny detected. Towny functionalities enabled.");
            this.nationDataManager = new NationDataManager(this);
        } else {
            this.townyEnabled = false;
            getLogger().warning("Towny not detected, disabling Towny functionalities.");
        }

        // Register event listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TechnologyPointsListener(this), this); // <<-- NEW: Register your listener here

        getLogger().info("NationTech has been enabled successfully! Total technologies loaded: " + technologyManager.getAllTechnologies().size());
        for (Technology tech : technologyManager.getAllTechnologies().values()) {
            getLogger().info(" - Loaded technology: " + tech.getName() + " (ID: " + tech.getId() + ")");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("NationTech is disabling...");

        if (townyEnabled && nationDataManager != null) {
            nationDataManager.saveAllNationData();
        }
        getLogger().info("NationTech has been disabled.");
    }

    public static NationTech getInstance() {
        return instance;
    }

    public TechnologyManager getTechnologyManager() {
        return technologyManager;
    }

    public NationDataManager getNationDataManager() {
        return nationDataManager;
    }

    public boolean isTownyEnabled() {
        return townyEnabled;
    }
}