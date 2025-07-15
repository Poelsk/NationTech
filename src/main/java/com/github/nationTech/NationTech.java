package com.github.nationTech;

import com.cjcrafter.foliascheduler.FoliaCompatibility;
import com.cjcrafter.foliascheduler.ServerImplementation;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.github.nationTech.commands.AdminCommandManager;
import com.github.nationTech.commands.PlayerCommandManager;
import com.github.nationTech.database.DatabaseManager;
import com.github.nationTech.gui.GUIManager;
import com.github.nationTech.gui.tree.AdvancementTreeManager;
import com.github.nationTech.listeners.GUIListener;
import com.github.nationTech.listeners.PlayerListener;
import com.github.nationTech.managers.TechnologyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class NationTech extends JavaPlugin {

    private static NationTech instance;
    private static ServerImplementation scheduler;
    private DatabaseManager databaseManager;
    private TechnologyManager technologyManager;
    private GUIManager guiManager;
    private AdvancementTreeManager advancementTreeManager;
    private UltimateAdvancementAPI advancementAPI;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("technologies.yml", false);

        scheduler = new FoliaCompatibility(this).getServerImplementation();

        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect().join(); // Wait for DB connection

        this.technologyManager = new TechnologyManager(this);
        this.technologyManager.loadAllTrees();

        this.guiManager = new GUIManager(this);

        // Advancement tree using UltimateAdvancementAPI
        this.advancementAPI = UltimateAdvancementAPI.getInstance(this);
        this.advancementTreeManager = new AdvancementTreeManager(this);
        this.advancementTreeManager.loadAndRegisterTechnologies();


        // Register Commands
        AdminCommandManager adminCommands = new AdminCommandManager();
        Objects.requireNonNull(getCommand("ntca")).setExecutor(adminCommands);
        Objects.requireNonNull(getCommand("ntca")).setTabCompleter(adminCommands);
        Objects.requireNonNull(getCommand("ntc")).setExecutor(new PlayerCommandManager(guiManager));


        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);


        getLogger().info("NationTech has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        if (advancementTreeManager!= null) {
            advancementTreeManager.unregisterAll();
        }
        getLogger().info("NationTech has been disabled.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static NationTech getInstance() {
        return instance;
    }

    public static ServerImplementation getScheduler() {
        return scheduler;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TechnologyManager getTechnologyManager() {
        return technologyManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public AdvancementTreeManager getAdvancementTreeManager() {
        return advancementTreeManager;
    }

    public UltimateAdvancementAPI getAdvancementAPI() {
        return advancementAPI;
    }

    public Economy getEconomy() {
        return economy;
    }
}