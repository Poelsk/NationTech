package com.github.nationTech.gui.tree;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.github.nationTech.NationTech;
import com.github.nationTech.utils.ColorUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdvancementTreeManager {

    private final NationTech plugin;
    private AdvancementTab techTab;
    private final AdvancementKey rootKey;


    public AdvancementTreeManager(NationTech plugin) {
        this.plugin = plugin;
        this.rootKey = new AdvancementKey(plugin.getDescription().getName().toLowerCase(), "root");
    }

    public void loadAndRegisterTechnologies() {
        // Crear la pestaña de logros para NationTech
        this.techTab = plugin.getAdvancementAPI().createAdvancementTab("nationtech");

        File techFile = new File(plugin.getDataFolder(), "technologies.yml");
        FileConfiguration techConfig = YamlConfiguration.loadConfiguration(techFile);
        ConfigurationSection techSection = techConfig.getConfigurationSection("technologies");
        if (techSection == null) {
            plugin.getLogger().warning("No se encontró la sección 'technologies' en technologies.yml");
            return;
        }

        Map<String, BaseAdvancement> advancementsToRegister = new HashMap<>();
        Map<String, String> parentLinks = new HashMap<>();

        // Primera pasada: Crear todos los objetos de Advancement
        for (String key : techSection.getKeys(false)) {
            ConfigurationSection current = techSection.getConfigurationSection(key);
            if (current == null) continue;

            String name = current.getString("name", "Sin Nombre");
            List<String> description = current.getStringList("description");
            Material icon = Material.matchMaterial(current.getString("icon", "STONE"));
            String parentId = current.getString("parent");

            AdvancementDisplay display = new AdvancementDisplay(
                    new ItemStack(icon != null ? icon : Material.STONE),
                    LegacyComponentSerializer.legacySection().serialize(ColorUtils.format(name)),
                    AdvancementFrameType.TASK,
                    true,
                    true,
                    0,
                    0,
                    description.stream().map(d -> LegacyComponentSerializer.legacySection().serialize(ColorUtils.format(d))).collect(Collectors.toList())
            );

            BaseAdvancement advancement;
            if (parentId == null || parentId.equalsIgnoreCase("null")) {
                advancement = new RootAdvancement(techTab, new AdvancementKey(plugin.getDescription().getName().toLowerCase(), key), display, "textures/gui/advancements/backgrounds/stone.png");
            } else {
                advancement = new BaseAdvancement(new AdvancementKey(plugin.getDescription().getName().toLowerCase(), key), display);
                parentLinks.put(key, parentId);
            }
            advancementsToRegister.put(key, advancement);
        }

        // Segunda pasada: Establecer las relaciones de parentesco
        for (Map.Entry<String, BaseAdvancement> entry : advancementsToRegister.entrySet()) {
            String childId = entry.getKey();
            BaseAdvancement childAdv = entry.getValue();

            if (parentLinks.containsKey(childId)) {
                String parentId = parentLinks.get(childId);
                BaseAdvancement parentAdv = advancementsToRegister.get(parentId);
                if (parentAdv != null) {
                    childAdv.setParent(parentAdv.getKey().getKey());
                }
            }
        }

        // Registrar todos los avances en la pestaña
        techTab.registerAdvancements(advancementsToRegister.values().toArray(new BaseAdvancement[0]));
        plugin.getLogger().info("Se han cargado y registrado " + advancementsToRegister.size() + " tecnologías en el árbol de logros.");
    }

    public void showToPlayer(Player player) {
        if (techTab != null) {
            techTab.showTab(player);
        }
    }

    public void grantAdvancement(Player player, String techId) {
        BaseAdvancement advancement = techTab.getAdvancement(new AdvancementKey(plugin.getDescription().getName().toLowerCase(), techId));
        if (advancement instanceof Advancement) {
            ((Advancement) advancement).grant(player);
        }
    }

    public void unregisterAll() {
        if (techTab != null) {
            plugin.getAdvancementAPI().deleteAdvancementTab(new AdvancementKey(plugin.getDescription().getName().toLowerCase(), "nationtech"));
        }
    }

    public void reloadTechnologies() {
        // Usar el planificador para evitar problemas en servidores Folia
        NationTech.getScheduler().global().run(() -> {
            unregisterAll();
            loadAndRegisterTechnologies();
        });
    }
}