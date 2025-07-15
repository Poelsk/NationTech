package com.github.nationTech.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.github.nationTech.NationTech;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.utils.MessageManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;

@CommandAlias("ntc")
@CommandPermission("nationtech.user")
@Description("Comando principal de NationTech para jugadores.")
public class NationTechPlayerCommand extends BaseCommand {

    @Default
    public void onDefault(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
        if (resident == null || !resident.hasNation()) {
            MessageManager.sendMessage(player, "<red>No perteneces a ninguna nación para ver sus tecnologías.");
            return;
        }

        // --- CORRECCIÓN AQUÍ: Se añade el bloque try-catch ---
        try {
            Nation nation = resident.getNation();
            NationTech.getInstance().getGuiManager().openNationTechGUI(player, nation, TechnologyManager.OFFICIAL_TREE_ID);
        } catch (NotRegisteredException e) {
            // Este error puede ocurrir en casos raros, lo manejamos de forma segura.
            MessageManager.sendMessage(player, "<red>Hubo un error al obtener los datos de tu nación.");
            NationTech.getInstance().getLogger().warning("Error al obtener la nación para " + player.getName() + " (NotRegisteredException)");
        } catch (TownyException e) {
            throw new RuntimeException(e);
        }
    }
}