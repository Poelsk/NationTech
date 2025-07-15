package com.github.nationTech.gui;

import com.github.nationTech.NationTech;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.model.Technology;
import com.github.nationTech.requirements.Requirement;
import com.github.nationTech.utils.ComponentUtils;
import com.palmergames.bukkit.towny.object.Nation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GUIManager {

    private final NationTech plugin;
    public static final NamespacedKey TECH_ID_KEY = new NamespacedKey(NationTech.getInstance(), "technology_id");
    public static final NamespacedKey TREE_ID_KEY = new NamespacedKey(NationTech.getInstance(), "tree_id");

    public GUIManager(NationTech plugin) { this.plugin = plugin; }
    private static class NationTechHolder implements NationTechGUI { @Override public Inventory getInventory() { return null; } }

    public void openDraftSelectionGUI(Player player) {
        Component title = ComponentUtils.parse("<dark_blue>Seleccionar Árbol Tecnológico");
        Set<String> treeNames = plugin.getTechnologyManager().getTreeNames();
        int size = Math.max(9, (int) (Math.ceil(treeNames.size() / 9.0) * 9));
        Inventory gui = Bukkit.createInventory(new NationTechHolder(), size, title);

        for (String treeId : treeNames) {
            boolean isOfficial = treeId.equals(TechnologyManager.OFFICIAL_TREE_ID);
            Material iconMaterial = isOfficial ? Material.ENCHANTED_BOOK : Material.WRITABLE_BOOK;
            String displayName = isOfficial ? "<gold><b>Árbol Oficial</b></gold>" : "<yellow>Borrador: " + treeId;
            ItemStack icon = new ItemStack(iconMaterial);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(ComponentUtils.parse(displayName));
            List<Component> lore = new ArrayList<>();
            lore.add(ComponentUtils.parse("<gray><!italic>Click para visualizar."));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(TREE_ID_KEY, PersistentDataType.STRING, treeId);
            icon.setItemMeta(meta);
            gui.addItem(icon);
        }
        player.openInventory(gui);
    }

    public void openNationTechGUI(@NotNull Player player, @NotNull Nation nation, @NotNull String treeId) {
        plugin.getDatabaseManager().getNationUnlockedTechs(nation.getUUID()).thenAccept(unlockedTechs -> {
            Component title = ComponentUtils.parse("<dark_gray>Tecnologías de </dark_gray><gold>" + nation.getName() + "</gold>");
            Inventory gui = Bukkit.createInventory(new NationTechHolder(), 54, title);

            TechnologyManager techManager = plugin.getTechnologyManager();
            Map<String, Technology> techTree = techManager.getTechnologyTree(treeId);

            ItemStack background = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", 0);
            for(int i = 0; i < gui.getSize(); i++) { gui.setItem(i, background); }

            for (Technology child : techTree.values()) {
                if (child.getPadreId() != null) {
                    Technology parent = techTree.get(child.getPadreId());
                    if (parent != null) {
                        drawConnection(gui, child, parent, unlockedTechs.contains(child.getId()));
                    }
                }
            }

            for (Technology tech : techTree.values()) {
                gui.setItem(tech.getSlot(), createTechIcon(tech, techTree, unlockedTechs));
            }

            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(gui));
        });
    }

    public void openAdminTechGUI(@NotNull Player player, @NotNull String treeId) {
        Component title = ComponentUtils.parse("<dark_aqua>Vista de Admin: </dark_aqua><yellow>" + treeId + "</yellow>");
        Inventory gui = Bukkit.createInventory(new NationTechHolder(), 54, title);

        TechnologyManager techManager = plugin.getTechnologyManager();
        Map<String, Technology> techTree = techManager.getTechnologyTree(treeId);

        ItemStack background = createGuiItem(Material.BLUE_STAINED_GLASS_PANE, " ", 0);
        for(int i = 0; i < gui.getSize(); i++) { gui.setItem(i, background); }

        for (Technology child : techTree.values()) {
            if (child.getPadreId() != null) {
                Technology parent = techTree.get(child.getPadreId());
                if (parent != null) {
                    drawConnection(gui, child, parent, true);
                }
            }
        }

        for (Technology tech : techTree.values()) {
            gui.setItem(tech.getSlot(), createAdminTechIcon(tech));
        }
        player.openInventory(gui);
    }

    private ItemStack createTechIcon(Technology tech, Map<String, Technology> techTree, Set<String> unlockedTechs) {
        Material iconMaterial = Material.matchMaterial(tech.getIcono());
        if (iconMaterial == null) iconMaterial = Material.BARRIER;

        ItemStack icon = new ItemStack(iconMaterial);
        ItemMeta meta = icon.getItemMeta();

        List<String> loreLines = new ArrayList<>();
        boolean isUnlocked = unlockedTechs.contains(tech.getId());
        boolean parentUnlocked = tech.getPadreId() == null || unlockedTechs.contains(tech.getPadreId());

        String rawReward = tech.getRecompensa();
        String benefitDescription = rawReward;
        if (rawReward.contains("|")) {
            benefitDescription = rawReward.split("\\|", 2)[0];
        }

        if (isUnlocked) {
            meta.displayName(ComponentUtils.parse("<green><b><!italic>" + tech.getNombre() + "</b></green>"));
            loreLines.add("<gold><!italic>¡Desbloqueado!</gold>");
            loreLines.add("");
            loreLines.add("<gray><!italic>Beneficio Obtenido:</gray>");
            loreLines.add("<green><!italic>" + benefitDescription + "</green>");

        } else if (parentUnlocked) {
            meta.displayName(ComponentUtils.parse("<yellow><!italic>" + tech.getNombre() + "</yellow>"));
            loreLines.add("");
            loreLines.add("<gray><!italic>Requisitos para desbloquear:");
            for (Requirement req : tech.getParsedRequirements()) {
                loreLines.add("<aqua><!italic>  ● " + req.getLoreText());
            }
            loreLines.add("");
            loreLines.add("<gray><!italic>Beneficio al Desbloquear:</gray>");
            loreLines.add("<green><!italic>" + benefitDescription + "</green>");
            loreLines.add("");
            loreLines.add("<yellow><b><!italic>¡Click para intentar desbloquear!");
        } else {
            meta.displayName(ComponentUtils.parse("<red><!italic>" + tech.getNombre() + "</red>"));
            Technology parentTech = techTree.get(tech.getPadreId());
            String parentName = parentTech != null ? parentTech.getNombre() : "desconocida";
            loreLines.add("");
            loreLines.add("<dark_gray><!italic>Bloqueado</dark_gray>");
            loreLines.add("<gray><!italic>Requiere: <white>" + parentName + "</white></gray>");
            loreLines.add("");
            loreLines.add("<gray><!italic>Beneficio al Desbloquear:</gray>");
            loreLines.add("<green><!italic>" + benefitDescription + "</green>");
        }

        meta.lore(loreLines.stream().map(ComponentUtils::parse).collect(Collectors.toList()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(TECH_ID_KEY, PersistentDataType.STRING, tech.getId());
        icon.setItemMeta(meta);
        return icon;
    }

    private ItemStack createAdminTechIcon(Technology tech) {
        Material iconMaterial = Material.matchMaterial(tech.getIcono());
        if (iconMaterial == null) iconMaterial = Material.BARRIER;

        ItemStack icon = new ItemStack(iconMaterial);
        ItemMeta meta = icon.getItemMeta();

        meta.displayName(ComponentUtils.parse("<aqua><b><!italic>" + tech.getNombre() + "</b></aqua>"));

        List<String> loreLines = new ArrayList<>();
        loreLines.add("");
        loreLines.add("<gray><!italic>ID: <white>" + tech.getId() + "</white></gray>");
        loreLines.add("<gray><!italic>Árbol: <white>" + tech.getTreeId() + "</white></gray>");
        loreLines.add("<gray><!italic>Padre: <white>" + (tech.getPadreId() == null ? "Ninguno" : tech.getPadreId()) + "</white></gray>");
        loreLines.add("<gray><!italic>Posición: <white>" + tech.getRow() + ", " + tech.getColumn() + "</white></gray>");
        loreLines.add("<gray><!italic>Tipo: <white>" + tech.getTipo().name() + "</white></gray>");

        String rawReward = tech.getRecompensa();
        if (rawReward.contains("|")) {
            String[] parts = rawReward.split("\\|", 2);
            loreLines.add("<gray><!italic>Beneficio (Desc): <white>" + parts[0] + "</white></gray>");
            loreLines.add("<gray><!italic>Beneficio (Cmd): <white>/" + parts[1] + "</white></gray>");
        } else {
            loreLines.add("<gray><!italic>Beneficio (Cmd): <white>/" + rawReward + "</white></gray>");
        }

        meta.lore(loreLines.stream().map(ComponentUtils::parse).collect(Collectors.toList()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        icon.setItemMeta(meta);
        return icon;
    }

    private void drawConnection(Inventory gui, Technology child, Technology parent, boolean isUnlocked) {
        Material lineMaterial = isUnlocked ? Material.LIME_STAINED_GLASS_PANE : Material.WHITE_STAINED_GLASS_PANE;
        int parentCol = parent.getColumn();
        int parentRow = parent.getRow();
        int childCol = child.getColumn();
        int childRow = child.getRow();
        for (int c = Math.min(parentCol, childCol) + 1; c < Math.max(parentCol, childCol); c++) { setLineItem(gui, parentRow, c, lineMaterial, 1001); }
        for (int r = Math.min(parentRow, childRow) + 1; r < Math.max(parentRow, childRow); r++) { setLineItem(gui, r, childCol, lineMaterial, 1002); }
        int cornerModelData = 1001;
        if (childRow != parentRow) {
            if (childCol > parentCol && childRow > parentRow) cornerModelData = 1004;
            else if (childCol < parentCol && childRow > parentRow) cornerModelData = 1003;
            else if (childCol > parentCol && childRow < parentRow) cornerModelData = 1006;
            else if (childCol < parentCol && childRow < parentRow) cornerModelData = 1005;
        }
        setLineItem(gui, parentRow, childCol, lineMaterial, cornerModelData);
    }

    private void setLineItem(Inventory gui, int row, int col, Material material, int modelData) {
        int slot = col + (row * 9);
        if (slot < 0 || slot >= gui.getSize()) return;
        ItemStack currentItem = gui.getItem(slot);
        if (currentItem != null && currentItem.getType() != Material.GRAY_STAINED_GLASS_PANE && currentItem.getType() != Material.BLUE_STAINED_GLASS_PANE) { return; }
        gui.setItem(slot, createGuiItem(material, " ", modelData));
    }

    private ItemStack createGuiItem(Material material, String name, int modelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ComponentUtils.parse(name));
        if (modelData != 0) { meta.setCustomModelData(modelData); }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}