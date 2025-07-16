package com.github.nationTech.commands;

import com.github.nationTech.NationTech;
import com.github.nationTech.gui.GUIManager;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.utils.MessageManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerCommandManager implements CommandExecutor {

    private final GUIManager guiManager;

    public PlayerCommandManager(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            MessageManager.sendMessage(sender, "<red>Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        Player player = (Player) sender;
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());

        if (resident == null || !resident.hasNation()) {
            MessageManager.sendMessage(player, "<red>No perteneces a ninguna nación.");
            return true;
        }

        try {
            Nation nation = resident.getNation();
            // --- CORRECCIÓN AQUÍ ---
            // Especificamos que se debe abrir el árbol OFICIAL.
            guiManager.openNationTechGUI(player, nation, TechnologyManager.OFFICIAL_TREE_ID);
        } catch (Exception e) {
            MessageManager.sendMessage(player, "<red>Hubo un error al obtener los datos de tu nación.");
            NationTech.getInstance().getLogger().warning("Error al obtener la nación para " + player.getName() + ": " + e.getMessage());
        }

        return true;
    }
}