package com.github.nationTech.listeners;

import com.github.nationTech.NationTech;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final NationTech plugin;

    public PlayerListener(NationTech plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String resourcePackURL = plugin.getConfig().getString("resource-pack-url");

        if (resourcePackURL != null && !resourcePackURL.isEmpty()) {
            // Enviamos una solicitud al jugador para que descargue el resource pack.
            // Hay un pequeño delay para asegurar que el cliente esté listo para recibirlo.
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.setResourcePack(resourcePackURL);
            }, 20L); // 20 ticks = 1 segundo
        }
    }
}