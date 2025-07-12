package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.database.DatabaseManager;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.utils.MessageManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GiveTechCommand implements SubCommand {

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getSyntax() {
        return "/ntca give <id_tecnologia> <nombre_nacion>";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.give";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        String techId = args[0];
        String nationName = args[1];

        Nation nation = TownyAPI.getInstance().getNation(nationName);
        if (nation == null) {
            MessageManager.sendMessage(sender, "<red>La nación '<white>" + nationName + "</white>' no existe.");
            return;
        }

        // Verifica que la tecnología exista en el árbol oficial
        TechnologyManager tm = NationTech.getInstance().getTechnologyManager();
        if (!tm.getTechnologyTree(TechnologyManager.OFFICIAL_TREE_ID).containsKey(techId)) {
            MessageManager.sendMessage(sender, "<red>La tecnología '<white>" + techId + "</white>' no existe en el árbol oficial.");
            return;
        }

        DatabaseManager dbManager = NationTech.getInstance().getDatabaseManager();
        // Añadimos el progreso directamente. No consume recursos ni da recompensa.
        dbManager.addUnlockedTechnology(nation.getUUID(), techId);
        MessageManager.sendMessage(sender, "<green>Tecnología '<white>" + techId + "</white>' otorgada a la nación '<white>" + nation.getName() + "</white>'.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        TechnologyManager tm = NationTech.getInstance().getTechnologyManager();
        List<String> completions = new ArrayList<>();
        String input = args[args.length - 1].toLowerCase();

        if (args.length == 1) { // Sugerir <id_tecnologia> del árbol oficial
            completions.addAll(tm.getTechnologyTree(TechnologyManager.OFFICIAL_TREE_ID).keySet());
        } else if (args.length == 2) { // Sugerir <nombre_nacion>
            TownyAPI.getInstance().getNations().forEach(n -> completions.add(n.getName().replace(" ", "_")));
        }

        // Filtra las sugerencias para que coincidan con lo que el usuario ya ha escrito
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}