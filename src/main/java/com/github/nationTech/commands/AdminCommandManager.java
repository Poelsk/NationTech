package com.github.nationTech.commands;

import com.github.nationTech.commands.admin.*;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter; // IMPORTAMOS TABCOMPLETER
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// IMPLEMENTAMOS LA INTERFAZ TabCompleter
public class AdminCommandManager implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public AdminCommandManager() {
        registerSubCommand(new AddTechCommand());
        registerSubCommand(new CreateDraftCommand());
        registerSubCommand(new ListTestCommand());
        registerSubCommand(new SetPackCommand());
        registerSubCommand(new RemoveTechCommand());
        registerSubCommand(new GiveTechCommand());
        registerSubCommand(new QuitTechCommand());
        registerSubCommand(new ReloadCommand());
    }

    private void registerSubCommand(SubCommand subCommand) {
        subcommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // ... (La lógica de ejecución no cambia) ...
        if (args.length == 0) {
            String availableCmds = subcommands.keySet().stream().sorted().collect(Collectors.joining(", "));
            MessageManager.sendMessage(sender, "Usa /ntca <subcomando> [argumentos...]");
            MessageManager.sendMessage(sender, "Subcomandos: <white>" + availableCmds);
            return true;
        }
        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subcommands.get(subCommandName);
        if (subCommand == null) {
            MessageManager.sendMessage(sender, "<red>Comando desconocido.");
            return true;
        }
        if (!sender.hasPermission(subCommand.getPermission())) {
            MessageManager.sendMessage(sender, "<red>No tienes permiso.");
            return true;
        }
        String[] subCommandArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subCommandArgs, 0, args.length - 1);
        subCommand.execute(sender, subCommandArgs);
        return true;
    }

    /**
     * --- MÉTODO NUEVO DE TABCOMPLETER ---
     */
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // Autocompletado para el primer argumento (los nombres de los subcomandos)
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String subCommandName : subcommands.keySet()) {
                if (subCommandName.startsWith(input) && sender.hasPermission(subcommands.get(subCommandName).getPermission())) {
                    completions.add(subCommandName);
                }
            }
            return completions;
        }

        // Si hay más de un argumento, delegamos al subcomando correspondiente
        if (args.length > 1) {
            SubCommand subCommand = subcommands.get(args[0].toLowerCase());
            if (subCommand != null && sender.hasPermission(subCommand.getPermission())) {
                String[] subCommandArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subCommandArgs, 0, args.length - 1);
                return subCommand.onTabComplete(sender, subCommandArgs);
            }
        }

        return new ArrayList<>(); // Devolvemos una lista vacía por defecto
    }
}