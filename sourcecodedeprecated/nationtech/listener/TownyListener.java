package com.tunacion.nationtech.listener;

import com.palmergames.bukkit.towny.event.NewNationEvent;
import com.tunacion.nationtech.NationTech;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Escucha los eventos de Towny relevantes para NationTech.
 */
public class TownyListener implements Listener {

    private final NationTech plugin;

    public TownyListener(NationTech plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNewNation(NewNationEvent event) {
        // Asegura que se cree un archivo de progreso para la nueva nación.
        plugin.getDataManager().getNationProgress(event.getNation().getUUID());
    }
}