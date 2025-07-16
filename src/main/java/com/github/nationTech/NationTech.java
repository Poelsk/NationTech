package com.github.nationTech;

import com.github.nationTech.commands.CommandManager;
import com.github.nationTech.database.DatabaseManager;
import com.github.nationTech.gui.GUIManager;
import com.github.nationTech.listeners.GUIListener;
import com.github.nationTech.listeners.PlayerListener;
import com.github.nationTech.managers.TechnologyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class NationTech extends JavaPlugin {

    private static NationTech instance;
    private DatabaseManager databaseManager;
    private TechnologyManager technologyManager;
    private GUIManager guiManager;
    private static Economy economy = null;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("Inicializando componentes de NationTech...");

        // 1. Inicializamos los gestores principales.
        this.guiManager = new GUIManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.technologyManager = new TechnologyManager(this);

        // 2. Conectamos a la base de datos y cargamos las tecnologías.
        // La carga de tecnologías se ejecuta después de que la conexión sea exitosa.
        this.databaseManager.connect().thenRun(() -> {
            technologyManager.loadAllTrees();
        }).exceptionally(ex -> {
            getLogger().log(Level.SEVERE, "No se pudo conectar a la base de datos durante el inicio.", ex);
            return null;
        });

        // 3. Registramos los comandos usando el nuevo sistema ACF.
        getLogger().info("Registrando comandos con ACF...");
        new CommandManager(this, this.technologyManager);

        // 4. Registramos los eventos.
        getLogger().info("Registrando eventos...");
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // 5. Verificamos las dependencias.
        getLogger().info("Verificando dependencias...");
        if (getServer().getPluginManager().getPlugin("Towny") == null) {
            getLogger().severe("¡Towny no encontrado! NationTech no puede funcionar sin Towny. Deshabilitando...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            getLogger().info("No se encontró un plugin de economía compatible con Vault. Las funciones de dinero estarán desactivadas.");
        } else {
            getLogger().info("Conectado con Vault! Las funciones de economía están activas.");
        }

        getLogger().info("NationTech v" + getDescription().getVersion() + " ha sido habilitado exitosamente.");
    }

    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.disconnect();
        }
        getLogger().info("NationTech ha sido deshabilitado.");
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

    // --- Getters ---

    public static NationTech getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
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
}