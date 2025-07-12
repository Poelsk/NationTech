package com.tunacion.nationtech.config;

import com.tunacion.nationtech.NationTech;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Maneja la carga y el acceso a los archivos de configuración.
 */
public class ConfigManager {

    private final NationTech plugin;
    private FileConfiguration langConfig;
    private File langFile;

    public ConfigManager(NationTech plugin) {
        this.plugin = plugin;
        saveDefaultLangConfig();
    }

    /**
     * Carga todas las configuraciones del plugin.
     */
    public void loadConfigs() {
        plugin.saveDefaultConfig();
        reloadLangConfig();
        Lang.load(langConfig);
    }

    /**
     * Recarga el archivo de idioma.
     */
    public void reloadLangConfig() {
        if (langFile == null) {
            langFile = new File(plugin.getDataFolder(), "lang.yml");
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        final InputStream defLangStream = plugin.getResource("lang.yml");
        if (defLangStream != null) {
            langConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defLangStream)));
        }
    }

    /**
     * Guarda la configuración de idioma por defecto si no existe.
     */
    private void saveDefaultLangConfig() {
        if (langFile == null) {
            langFile = new File(plugin.getDataFolder(), "lang.yml");
        }
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
    }
}