package com.nationtech;

import com.nationtech.data.NationDataManager;
import com.nationtech.listeners.TechnologyPointsListener;
import com.nationtech.tech.TechnologyManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class NationTech extends JavaPlugin implements Listener {

    private static NationTech instance;
    private TechnologyManager technologyManager;
    private NationDataManager nationDataManager;
    private TownyHandler townyHandler;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("NationTech is enabling...");

        saveDefaultConfig();
        saveResource("technologies.yml", false);

        this.technologyManager = new TechnologyManager(this);
        this.nationDataManager = new NationDataManager(this);

        getServer().getPluginManager().registerEvents(this, this);

        initializeTownyHandler();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TechnologyPointsListener(this), this);

        getLogger().info("NationTech has been enabled successfully! Total technologies loaded: " + technologyManager.getAllTechnologies().size());
    }

    @Override
    public void onDisable() {
        getLogger().info("NationTech is disabling...");
        if (nationDataManager != null) {
            nationDataManager.saveAllNationData();
        }
        getLogger().info("NationTech has been disabled.");
    }

    private void initializeTownyHandler() {
        Plugin townyPlugin = getServer().getPluginManager().getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            this.townyHandler = new TownyHandler();
            getLogger().info("Towny detected. Towny functionalities enabled.");
        } else {
            this.townyHandler = null;
            getLogger().warning("Towny not detected. Towny functionalities will be disabled.");
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("Towny")) {
            getLogger().info("Towny has been enabled, initializing integration...");
            initializeTownyHandler();
        }
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

    public TownyHandler getTownyHandler() {
        return townyHandler;
    }

    public boolean isTownyIntegrationEnabled() {
        return this.townyHandler != null;
    }
}