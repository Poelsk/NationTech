package com.tunacion.nationtech.tech;

import com.tunacion.nationtech.NationTech;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * Gestiona la carga y el acceso a todas las tecnologías disponibles.
 */
public class TechManager {

    private final NationTech plugin;
    private final Map<String, Tech> techs = new HashMap<>();
    private final Map<Integer, String> techSlots = new HashMap<>();

    public TechManager(NationTech plugin) {
        this.plugin = plugin;
    }

    /**
     * Carga todas las tecnologías desde la carpeta 'techs'.
     */
    public void loadTechs() {
        techs.clear();
        techSlots.clear();

        File techsFolder = new File(plugin.getDataFolder(), "techs");
        if (!techsFolder.exists()) {
            techsFolder.mkdirs();
            // Guardamos un archivo de ejemplo si la carpeta no existe.
            plugin.saveResource("techs/01_agricultura_basica.yml", false);
        }

        File[] techFiles = techsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (techFiles == null) return;

        for (File techFile : techFiles) {
            FileConfiguration techConfig = YamlConfiguration.loadConfiguration(techFile);
            ConfigurationSection techSection = techConfig.getConfigurationSection("tech");
            if (techSection != null) {
                String id = techSection.getString("id");

                // --- SOLUCIÓN A TU PETICIÓN ---
                // Aquí usamos ChatColor.translateAlternateColorCodes para procesar el nombre.
                // Esto reemplaza la necesidad de usar 'NationTech.MM.deserialize'.
                String name = ChatColor.translateAlternateColorCodes('&', techSection.getString("name", ""));

                List<String> description = new ArrayList<>();
                for (String line : techSection.getStringList("description")) {
                    String s = ChatColor.translateAlternateColorCodes('&', line);
                    description.add(s);
                }
                Material material = Material.matchMaterial(techSection.getString("material", "STONE"));
                int slot = techSection.getInt("slot");

                // Cargar los comandos de recompensa
                List<String> rewardCommands = techSection.getStringList("reward-commands");

                if (id != null && material != null) {
                    Tech tech = new Tech(id, name, description, material, slot, rewardCommands);
                    techs.put(id.toLowerCase(), tech);
                    techSlots.put(slot, id.toLowerCase());
                }
            }
        }
        plugin.getLogger().info("Cargando");
    }

    public Tech getTech(String id) {
        return techs.get(id.toLowerCase());
    }

    public Tech getTechBySlot(int slot) {
        String id = techSlots.get(slot);
        return id != null ? getTech(id) : null;
    }

    public Collection<Tech> getAllTechs() {
        return techs.values();
    }
}
