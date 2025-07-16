package com.github.nationTech.commands;

import co.aikar.commands.PaperCommandManager;
import com.github.nationTech.NationTech;
import com.github.nationTech.managers.TechnologyManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager {

    public CommandManager(NationTech plugin, TechnologyManager techManager) {
        PaperCommandManager manager = new PaperCommandManager(plugin);

        // --- Registro de Autocompletados ---
        manager.getCommandCompletions().registerAsyncCompletion("@treenames", c -> techManager.getTreeNames());

        manager.getCommandCompletions().registerAsyncCompletion("@tech_ids_and_null", c -> {
            // --- CORRECCIÓN DEFINITIVA ---
            // Se obtiene el primer argumento que el usuario ha escrito (el nombre del árbol).
            // getContextValue(Clase, Indice) donde el índice 1 es el primer argumento del usuario.
            String treeId = c.getContextValue(String.class, 1);
            if (treeId == null || !techManager.getTreeNames().contains(treeId)) {
                treeId = TechnologyManager.OFFICIAL_TREE_ID;
            }

            List<String> completions = new ArrayList<>();
            completions.add("null");
            completions.addAll(techManager.getTechnologyTree(treeId).keySet());
            return completions;
        });

        manager.getCommandCompletions().registerAsyncCompletion("@technologies", c -> {
            return techManager.getTechnologyTree(TechnologyManager.OFFICIAL_TREE_ID).keySet();
        });

        manager.getCommandCompletions().registerAsyncCompletion("@nations", c -> TownyAPI.getInstance().getNations().stream().map(Nation::getName).collect(Collectors.toList()));

        // --- Registro de Contextos ---
        manager.getCommandContexts().registerContext(Nation.class, c -> {
            String nationName = c.popFirstArg();
            Nation nation = TownyAPI.getInstance().getNation(nationName);
            if (nation == null) {
                throw new co.aikar.commands.InvalidCommandArgument("¡La nación '" + nationName + "' no existe!");
            }
            return nation;
        });

        // --- Registro de los Comandos ---
        manager.registerCommand(new NationTechAdminCommand());
        manager.registerCommand(new NationTechPlayerCommand());
    }
}