package com.github.nationTech;

import com.cjcrafter.foliascheduler.FoliaCompatibility;
import com.cjcrafter.foliascheduler.ServerImplementation;
import com.frengor.ultimateadvancementapi.UltimateAdvancementAPI;
import com.github.nationTech.commands.NationTechCmd;
import com.github.nationTech.gui.tree.AdvancementTreeManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NationTech extends JavaPlugin {

    private static NationTech instance;
    private static ServerImplementation scheduler;
    private AdvancementTreeManager advancementTreeManager;
    private UltimateAdvancementAPI advancementAPI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultResource("technologies.yml", false);

        // Inicializar el planificador compatible con Folia
        scheduler = new FoliaCompatibility(this).getServerImplementation();

        // Inicializar la API de logros
        this.advancementAPI = UltimateAdvancementAPI.getInstance(this);

        // Inicializar el gestor del árbol de tecnologías
        this.advancementTreeManager = new AdvancementTreeManager(this);
        this.advancementTreeManager.loadAndRegisterTechnologies();

        // Registrar comandos
        getCommand("ntca").setExecutor(new NationTechCmd(this));

        getLogger().info("NationTech ha sido habilitado con éxito.");
    }

    @Override
    public void onDisable() {
        if (advancementTreeManager!= null) {
            advancementTreeManager.unregisterAll();
        }
        getLogger().info("NationTech ha sido deshabilitado.");
    }

    public static NationTech getInstance() {
        return instance;
    }

    public static ServerImplementation getScheduler() {
        return scheduler;
    }

    public AdvancementTreeManager getAdvancementTreeManager() {
        return advancementTreeManager;
    }

    public UltimateAdvancementAPI getAdvancementAPI() {
        return advancementAPI;
    }
}