package com.github.nationTech.commands;

import org.bukkit.command.CommandSender;
import java.util.List;

public interface SubCommand {

    void execute(CommandSender sender, String[] args);

    String getName();

    String getSyntax();

    String getPermission();

    /**
     * --- MÉTODO NUEVO ---
     * Proporciona una lista de sugerencias para el autocompletado.
     * @param sender Quien ejecuta el comando.
     * @param args Los argumentos actuales.
     * @return Una lista de posibles completados.
     */
    List<String> onTabComplete(CommandSender sender, String[] args);
}