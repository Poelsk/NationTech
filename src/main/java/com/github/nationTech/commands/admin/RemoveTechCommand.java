package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveTechCommand implements SubCommand {

    private final TechnologyManager technologyManager = NationTech.getInstance().getTechnologyManager();

    @Override
    public String getName() {
        return "removetech";
    }

    @Override
    public String getSyntax() {
        return "/ntca removetech <id_tecnologia> [nombre_arbol]";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.removetech";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        String techId = args[0];
        String treeId = (args.length > 1) ? args[1].toLowerCase() : TechnologyManager.OFFICIAL_TREE_ID;

        // --- CORRECCIÓN AQUÍ ---
        // Se comprueba si la tecnología existe antes de llamar al método de borrado.
        if (technologyManager.getTechnologyTree(treeId).containsKey(techId)) {
            technologyManager.deleteTechnology(treeId, techId);
            MessageManager.sendMessage(sender, "<green>Tecnología '<white>" + techId + "</white>' eliminada del árbol '<white>" + treeId + "</white>' con éxito.");
        } else {
            MessageManager.sendMessage(sender, "<red>No se encontró ninguna tecnología con el ID '<white>" + techId + "</white>' en el árbol '<white>" + treeId + "</white>'.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        TechnologyManager tm = NationTech.getInstance().getTechnologyManager();
        List<String> completions = new ArrayList<>();
        String input = args[args.length - 1].toLowerCase();

        if (args.length == 1) {
            tm.getTreeNames().forEach(treeId -> completions.addAll(tm.getTechnologyTree(treeId).keySet()));
        } else if (args.length == 2) {
            completions.addAll(tm.getTreeNames());
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}