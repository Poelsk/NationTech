package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.model.TechnologyType;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level; // --- CORRECCIÓN AQUÍ: Import faltante añadido ---
import java.util.stream.Collectors;

public class AddTechCommand implements SubCommand {

    private final TechnologyManager technologyManager = NationTech.getInstance().getTechnologyManager();

    @Override
    public String getName() {
        return "addtech";
    }

    @Override
    public String getSyntax() {
        return "/ntca addtech <id> <fila> <col> <nombre> <padreId|null> <F|O> <requisitos> <icono> <beneficio> [nombre_arbol]";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.addtech";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 9) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        try {
            String id = args[0];
            int row = Integer.parseInt(args[1]);
            int column = Integer.parseInt(args[2]);
            String nombre = args[3];
            String padreId = args[4].equalsIgnoreCase("null") ? null : args[4];
            TechnologyType tipo = args[5].equalsIgnoreCase("F") ? TechnologyType.FINAL : TechnologyType.OPEN;
            String requisitos = args[6];
            String icono = args[7].toUpperCase();

            String treeId = TechnologyManager.OFFICIAL_TREE_ID;
            int benefitEndIndex = args.length;

            if (args.length > 9) {
                String potentialTreeId = args[args.length - 1].toLowerCase();
                if (technologyManager.getTreeNames().contains(potentialTreeId)) {
                    treeId = potentialTreeId;
                    benefitEndIndex = args.length - 1;
                }
            }

            StringBuilder beneficioBuilder = new StringBuilder();
            for (int i = 8; i < benefitEndIndex; i++) {
                beneficioBuilder.append(args[i]).append(" ");
            }
            String beneficio = beneficioBuilder.toString().trim();

            if (beneficio.isEmpty()) {
                MessageManager.sendMessage(sender, "<red>El comando de beneficio no puede estar vacío.");
                return;
            }

            if (technologyManager.getTechnologyTree(treeId).containsKey(id)) {
                MessageManager.sendMessage(sender, "<red>Ya existe una tecnología con el ID '<white>" + id + "</white>' en el árbol '<white>" + treeId + "</white>'.");
                return;
            }

            technologyManager.createTechnology(treeId, id, row, column, nombre, padreId, tipo, requisitos, icono, beneficio);
            MessageManager.sendMessage(sender, "<green>Tecnología '<white>" + nombre + "</white>' creada en el árbol '<white>" + treeId + "</white>'.");

        } catch (NumberFormatException e) {
            MessageManager.sendMessage(sender, "<red>La fila y la columna deben ser números enteros.");
        } catch (Exception e) {
            MessageManager.sendMessage(sender, "<red>Ocurrió un error inesperado al procesar el comando.");
            NationTech.getInstance().getLogger().log(Level.SEVERE, "Error ejecutando AddTechCommand", e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        TechnologyManager tm = NationTech.getInstance().getTechnologyManager();
        List<String> completions = new ArrayList<>();
        String input = args[args.length - 1].toLowerCase();
        String treeId = TechnologyManager.OFFICIAL_TREE_ID;
        if (args.length == 10) {
            String potentialTreeId = args[8].toLowerCase();
            if (tm.getTreeNames().contains(potentialTreeId)) { treeId = potentialTreeId; }
        } else if (args.length > 10) {
            String potentialTreeId = args[args.length - 2].toLowerCase();
            if (tm.getTreeNames().contains(potentialTreeId)) { treeId = potentialTreeId; }
        }
        switch (args.length) {
            case 5:
                completions.add("null");
                completions.addAll(tm.getTechnologyTree(treeId).keySet());
                break;
            case 6:
                completions.add("F");
                completions.add("O");
                break;
            case 8:
                for (Material mat : Material.values()) {
                    if (mat.isItem()) { completions.add(mat.name().toLowerCase()); }
                }
                break;
            case 10:
                completions.addAll(tm.getTreeNames());
                break;
            default:
                return new ArrayList<>();
        }
        return completions.stream().filter(s -> s.toLowerCase().startsWith(input)).collect(Collectors.toList());
    }
}