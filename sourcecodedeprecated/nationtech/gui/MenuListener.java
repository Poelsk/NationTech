package com.tunacion.nationtech.gui;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.tunacion.nationtech.NationTech;
import com.tunacion.nationtech.tech.Tech;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

/**
 * Escucha los eventos de clic en los inventarios del plugin.
 */
public class MenuListener implements Listener {

    private final NationTech plugin;

    public MenuListener(NationTech plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getInventory().getHolder() instanceof TechTreeGUI) {
            event.setCancelled(true); // Previene que el jugador tome los ítems.

            Player player = (Player) event.getWhoClicked();
            Nation nation = Objects.requireNonNull(TownyAPI.getInstance().getResident(player)).getNationOrNull();
            if (nation == null) {
                player.closeInventory();
                return;
            }

            Tech tech = plugin.getTechManager().getTechBySlot(event.getSlot());
            if (tech != null) {
                // Aquí se puede agregar la lógica para desbloquear tecnologías.
                // Por ahora, solo muestra información.
                player.sendMessage("Has hecho clic en la tecnología: " + tech.getName());
            }
        }
    }
}