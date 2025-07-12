package com.tunacion.nationtech.gui;

import com.palmergames.bukkit.towny.object.Nation;
import com.tunacion.nationtech.NationTech;
import com.tunacion.nationtech.config.Lang;
import com.tunacion.nationtech.data.NationProgress;
import com.tunacion.nationtech.tech.Tech;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa la GUI del árbol de tecnologías.
 */
public class TechTreeGUI implements InventoryHolder {

    private final Inventory inventory;
    private final NationTech plugin;
    private final Nation nation;

    public TechTreeGUI(NationTech plugin, Nation nation) {
        this.plugin = plugin;
        this.nation = nation;
        this.inventory = Bukkit.createInventory(this, 54, Lang.TECH_TREE_TITLE.getMessage());
        initializeItems();
    }

    /**
     * Inicializa los ítems en el inventario de la GUI.
     */
    private void initializeItems() {
        NationProgress progress = plugin.getDataManager().getNationProgress(nation.getUUID());

        for (Tech tech : plugin.getTechManager().getAllTechs()) {
            ItemStack item = new ItemStack(tech.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(tech.getName());
                List<String> lore = new ArrayList<>(tech.getDescription());

                if (progress.hasTech(tech.getId())) {
                    lore.add("");
                    lore.add(Lang.TECH_UNLOCKED.getMessage());
                } else {
                    lore.add("");
                    lore.add(Lang.TECH_LOCKED.getMessage());
                    // Podrías añadir lógica para mostrar requisitos aquí.
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(tech.getSlot(), item);
        }
    }

    /**
     * Abre el inventario para un jugador.
     * @param player El jugador que verá la GUI.
     */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}