package com.tunacion.nationtech;

import com.tunacion.nationtech.command.AdminCommand;
import com.tunacion.nationtech.command.UserCommand;
import com.tunacion.nationtech.config.ConfigManager;
import com.tunacion.nationtech.data.DataManager;
import com.tunacion.nationtech.gui.MenuListener;
import com.tunacion.nationtech.listener.TownyListener;
import com.tunacion.nationtech.reward.Reward;
import com.tunacion.nationtech.tech.TechManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Clase principal del plugin NationTech.
 * Se encarga de inicializar y deshabilitar todos los componentes del plugin.
 */
public final class NationTech extends JavaPlugin {

    private static NationTech instance;
    private DataManager dataManager;
    private TechManager techManager;
    private ConfigManager configManager;
    private Reward rewardManager;

    @Override
    public void onEnable() {
        instance = this;

        // Inicializa el manejador de configuración.
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Inicializa el manejador de tecnologías.
        techManager = new TechManager(this);
        techManager.loadTechs(); // Carga las tecnologías desde los archivos de configuración.

        // Inicializa el manejador de datos.
        dataManager = new DataManager(this);

        // Inicializa el manejador de recompensas.
        rewardManager = new Reward(this);

        // Registro de eventos y comandos.
        registerListeners();
        registerCommands();

        getLogger().info("NationTech ha sido habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        // Guarda todos los datos antes de deshabilitar el plugin.
        if (dataManager != null) {
            dataManager.saveAllNationProgress();
        }
        getLogger().info("NationTech ha sido deshabilitado correctamente.");
    }

    /**
     * Registra los listeners del plugin.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new TownyListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
    }

    /**
     * Registra los comandos del plugin.
     */
    private void registerCommands() {
        Objects.requireNonNull(getCommand("nationtech")).setExecutor(new UserCommand(this));
        Objects.requireNonNull(getCommand("ntecha")).setExecutor(new AdminCommand(this));
    }

    /**
     * Obtiene la instancia única del plugin.
     *
     * @return La instancia de NationTech.
     */
    public static NationTech getInstance() {
        return instance;
    }

    /**
     * Obtiene el manejador de datos.
     *
     * @return El DataManager.
     */
    public DataManager getDataManager() {
        return dataManager;
    }

    /**
     * Obtiene el manejador de tecnologías.
     *
     * @return El TechManager.
     */
    public TechManager getTechManager() {
        return techManager;
    }

    /**
     * Obtiene el manejador de configuración.
     *
     * @return El ConfigManager.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Obtiene el manejador de recompensas.
     *
     * @return El Reward manager.
     */
    public Reward getRewardManager() {
        return rewardManager;
    }
}