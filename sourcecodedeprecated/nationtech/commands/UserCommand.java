package com.tunacion.nationtech.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.tunacion.nationtech.NationTech;
import com.tunacion.nationtech.config.Lang;
import com.tunacion.nationtech.gui.TechTreeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


/**
 * Maneja los comandos de usuario para NationTech.
 */
public class UserCommand implements CommandExecutor {

    private final NationTech plugin;

    public UserCommand(NationTech plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.ONLY_PLAYERS.getMessage());
            return true;
        }

        Player player = (Player) sender;
        Resident resident = TownyAPI.getInstance().getResident(player);

        if (resident == null || !resident.hasNation()) {
            player.sendMessage(Lang.NO_NATION.getMessage());
            return true;
        }

        // Abre el árbol de tecnologías para la nación del jugador.
        try {
            new TechTreeGUI(plugin, resident.getNation()).open(player);
        } catch (TownyException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}