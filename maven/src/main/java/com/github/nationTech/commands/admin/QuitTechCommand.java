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

public class QuitTechCommand implements SubCommand {

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public String getSyntax() {
        return "/ntca quit <id_tecnologia> <nombre_nacion>";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.quit";
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

        DatabaseManager dbManager = NationTech.getInstance().getDatabaseManager();
        dbManager.removeUnlockedTechnology(nation.getUUID(), techId);
        MessageManager.sendMessage(sender, "<green>Tecnología '<white>" + techId + "</white>' quitada a la nación '<white>" + nation.getName() + "</white>'.");
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

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}