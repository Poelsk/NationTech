package com.nationtech;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.nationtech.commands.AdminCommands;
import com.nationtech.commands.TechnologyCommands;
import com.nationtech.data.NationDataManager;
import com.nationtech.gui.TechnologyGUI;
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
    private TechnologyGUI technologyGUI;
    private UltimateAdvancementAPI advancementAPI;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("NationTech is enabling...");

        // Save default configuration files
        saveDefaultConfig();
        saveResource("technologies.yml", false);

        // Initialize core managers
        this.technologyManager = new TechnologyManager(this);
        this.nationDataManager = new NationDataManager(this);

        // Register main plugin listener for other plugin events
        getServer().getPluginManager().registerEvents(this, this);

        // Initialize Towny integration
        initializeTownyHandler();

        // Initialize UltimateAdvancementAPI integration
        initializeAdvancementAPI();

        // Register event listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TechnologyPointsListener(this), this);

        // Register commands
        registerCommands();

        getLogger().info("NationTech has been enabled successfully! Total technologies loaded: " +
                technologyManager.getAllTechnologies().size());
    }

    @Override
    public void onDisable() {
        getLogger().info("NationTech is disabling...");

        // Save all nation data before shutdown
        if (nationDataManager != null) {
            nationDataManager.saveAllNationData();
        }

        // Clean up advancement API
        if (technologyGUI != null && advancementAPI != null) {
            try {
                // Cleanup advancement tab if needed
                getLogger().info("Cleaning up advancement GUI...");
            } catch (Exception e) {
                getLogger().warning("Error during advancement cleanup: " + e.getMessage());
            }
        }

        getLogger().info("NationTech has been disabled.");
    }

    private void initializeTownyHandler() {
        Plugin townyPlugin = getServer().getPluginManager().getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            try {
                this.townyHandler = new TownyHandler();
                getLogger().info("Towny detected and integrated successfully.");
            } catch (Exception e) {
                getLogger().warning("Failed to initialize Towny integration: " + e.getMessage());
                this.townyHandler = null;
            }
        } else {
            this.townyHandler = null;
            getLogger().warning("Towny not detected. Personal technology points will be used instead.");
        }
    }

    private void initializeAdvancementAPI() {
        Plugin advancementPlugin = getServer().getPluginManager().getPlugin("UltimateAdvancementAPI");
        if (advancementPlugin != null && advancementPlugin.isEnabled()) {
            try {
                this.advancementAPI = UltimateAdvancementAPI.getInstance(this);

                // Wait a bit for the API to fully initialize
                getServer().getScheduler().runTaskLater(this, () -> {
                    try {
                        this.technologyGUI = new TechnologyGUI(this, advancementAPI);
                        if (technologyGUI.isInitialized()) {
                            getLogger().info("UltimateAdvancementAPI detected and technology GUI initialized successfully.");
                        } else {
                            getLogger().warning("Technology GUI initialization failed - GUI may not work properly.");
                            this.technologyGUI = null;
                        }
                    } catch (Exception e) {
                        getLogger().severe("Failed to create technology GUI: " + e.getMessage());
                        this.technologyGUI = null;
                    }
                }, 20L); // Wait 1 second

            } catch (Exception e) {
                getLogger().severe("Failed to initialize UltimateAdvancementAPI: " + e.getMessage());
                this.advancementAPI = null;
                this.technologyGUI = null;
            }
        } else {
            getLogger().warning("UltimateAdvancementAPI not found. Technology GUI will be disabled.");
            this.advancementAPI = null;
            this.technologyGUI = null;
        }
    }

    private void registerCommands() {
        TechnologyCommands commandHandler = new TechnologyCommands(this);
        AdminCommands adminHandler = new AdminCommands(this);

        // Register the main tech command
        var techCommand = getCommand("tech");
        if (techCommand != null) {
            techCommand.setExecutor(commandHandler);
            techCommand.setTabCompleter(commandHandler);
        } else {
            getLogger().warning("Failed to register /tech command. Check plugin.yml configuration.");
        }

        // Register admin command
        var adminCommand = getCommand("techadmin");
        if (adminCommand != null) {
            adminCommand.setExecutor(adminHandler);
            adminCommand.setTabCompleter(adminHandler);
        } else {
            getLogger().warning("Failed to register /techadmin command. Check plugin.yml configuration.");
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        String pluginName = event.getPlugin().getName();

        // Handle Towny late loading
        if (pluginName.equalsIgnoreCase("Towny")) {
            getLogger().info("Towny has been enabled, initializing integration...");
            initializeTownyHandler();
        }

        // Handle UltimateAdvancementAPI late loading
        if (pluginName.equalsIgnoreCase("UltimateAdvancementAPI")) {
            getLogger().info("UltimateAdvancementAPI has been enabled, initializing GUI...");
            initializeAdvancementAPI();
        }
    }

    // Static getter for plugin instance
    public static NationTech getInstance() {
        return instance;
    }

    // Getters for managers and handlers
    public TechnologyManager getTechnologyManager() {
        return technologyManager;
    }

    public NationDataManager getNationDataManager() {
        return nationDataManager;
    }

    public TownyHandler getTownyHandler() {
        return townyHandler;
    }

    public TechnologyGUI getTechnologyGUI() {
        return technologyGUI;
    }

    public UltimateAdvancementAPI getAdvancementAPI() {
        return advancementAPI;
    }

    // Utility methods
    public boolean isTownyIntegrationEnabled() {
        return this.townyHandler != null;
    }

    public boolean isAdvancementGUIEnabled() {
        return this.technologyGUI != null && this.advancementAPI != null;
    }

    // Configuration reload method
    @Override
    public void reloadConfiguration() {
        try {
            reloadConfig();
            technologyManager.loadTechnologies();
            nationDataManager.loadAllNationData();

            // Reinitialize GUI if available
            if (isAdvancementGUIEnabled() && technologyGUI != null) {
                getLogger().info("Reinitializing technology GUI...");
                technologyGUI.reinitialize();
            }

            getLogger().info("Configuration reloaded successfully.");
        } catch (Exception e) {
            getLogger().severe("Error reloading configuration: " + e.getMessage());
            throw e;
        }
    }
}