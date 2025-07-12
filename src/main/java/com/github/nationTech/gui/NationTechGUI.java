package com.github.nationTech.gui;

import org.bukkit.inventory.InventoryHolder;

/**
 * Una interfaz marcadora para identificar todos los inventarios
 * que pertenecen al plugin NationTech. Cualquier GUI de este plugin
 * debe tener un InventoryHolder que implemente esta interfaz.
 */
public interface NationTechGUI extends InventoryHolder {
    // No necesita contenido, solo actuar como un tipo/marcador.
}