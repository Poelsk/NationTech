package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements SubCommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getSyntax() {
        return "/ntca reload";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        NationTech plugin = NationTech.getInstance();

        // Recargamos el archivo config.yml desde el disco.
        plugin.reloadConfig();

        // Recargamos todos los árboles tecnológicos desde la base de datos a la caché en memoria.
        plugin.getTechnologyManager().loadAllTrees();

        MessageManager.sendMessage(sender, "<green>¡NationTech recargado con éxito!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // Este comando no tiene argumentos, por lo tanto no hay sugerencias.
        return new ArrayList<>();
    }
}