package com.tunacion.nationtech.reward;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.tunacion.nationtech.NationTech;
import com.tunacion.nationtech.tech.Tech;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Gestiona la entrega de recompensas a los líderes de las naciones al desbloquear tecnologías.
 */
public class Reward {

    private final NationTech plugin;

    public Reward(NationTech plugin) {
        this.plugin = plugin;
    }

    /**
     * Otorga la recompensa de una tecnología a una nación.
     * @param nationUUID El UUID de la nación que recibe la recompensa.
     * @param techId El ID de la tecnología desbloqueada.
     */
    public void giveReward(UUID nationUUID, String techId) {
        Tech tech = plugin.getTechManager().getTech(techId);
        // Verificar si la tecnología existe y si tiene comandos de recompensa configurados.
        if (tech == null || tech.getRewardCommands() == null || tech.getRewardCommands().isEmpty()) {
            return;
        }

        Nation nation = TownyAPI.getInstance().getNation(nationUUID);
        if (nation == null) {
            plugin.getLogger().warning("Se intentó dar una recompensa, pero no se encontró la nación con UUID: " + nationUUID);
            return;
        }

        // 1. Obtener al líder (rey) de la nación.
        Resident king = nation.getKing();
        if (king == null) {
            plugin.getLogger().info("La nación '" + nation.getName() + "' no tiene un líder asignado. No se pueden dar recompensas.");
            return;
        }

        // 2. Verificar si el líder está conectado.
        if (!king.isOnline()) {
            plugin.getLogger().info("El líder '" + king.getName() + "' de la nación '" + nation.getName() + "' no está conectado. La recompensa no se entregará.");
            return;
        }

        // 3. Obtener el objeto Player del líder y ejecutar los comandos.
        Player leaderPlayer = king.getPlayer();
        if (leaderPlayer == null) {
            plugin.getLogger().log(Level.SEVERE, "Error crítico: El residente '" + king.getName() + "' está online pero no se pudo obtener su objeto Player.");
            return;
        }

        List<String> commands = tech.getRewardCommands();

        // Se ejecuta en el siguiente tick para asegurar la estabilidad del servidor.
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String commandTemplate : commands) {
                // Reemplazamos el marcador de posición con el nombre del líder.
                String command = commandTemplate.replace("%leader%", leaderPlayer.getName());

                plugin.getLogger().info("Ejecutando comando de recompensa para " + leaderPlayer.getName() + ": " + command);

                // Ejecutamos el comando desde la consola del servidor.
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        });
    }
}