package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ListTestCommand implements SubCommand {
    @Override
    public String getName() {
        return "listtest";
    }

    @Override
    public String getSyntax() {
        return "/ntca listtest";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.listtest";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
            return;
        }
        Player player = (Player) sender;
        // Llamamos al GUIManager para que abra el menú de selección de árboles.
        NationTech.getInstance().getGuiManager().openDraftSelectionGUI(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // Este comando no tiene argumentos, por lo tanto no hay sugerencias.
        return new ArrayList<>();
    }
}