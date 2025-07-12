package com.github.nationTech.commands;

import com.github.nationTech.NationTech;
import com.github.nationTech.gui.creation.TechCreationWizard;
import com.github.nationTech.utils.ColorUtils;
import com.github.nationTech.utils.TechUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationTechCmd implements CommandExecutor {

    private final NationTech plugin;

    public NationTechCmd(NationTech plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getAdvancementTreeManager().showToPlayer((Player) sender);
            } else {
                sender.sendMessage("Usa /ntca help para ver los comandos.");
            }
            return true;
        }

        String subCommand = args.toLowerCase();

        switch (subCommand) {
            case "gui":
                if (sender instanceof Player) {
                    plugin.getAdvancementTreeManager().showToPlayer((Player) sender);
                } else {
                    sender.sendMessage("Este comando solo puede ser usado por un jugador.");
                }
                break;
            case "addtechwizard":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Este comando solo puede ser usado por un jugador.");
                    return true;
                }
                if (!sender.hasPermission("nationtech.admin.addtech")) {
                    sender.sendMessage(ColorUtils.format("<red>No tienes permiso para usar este comando."));
                    return true;
                }
                new TechCreationWizard((Player) sender, plugin).start();
                break;
            case "reload":
                if (!sender.hasPermission("nationtech.admin.reload")) {
                    sender.sendMessage(ColorUtils.format("<red>No tienes permiso para usar este comando."));
                    return true;
                }
                plugin.getAdvancementTreeManager().reloadTechnologies();
                sender.sendMessage(ColorUtils.format("<green>Árbol de tecnologías recargado."));
                break;
            case "addtech":
                // Lógica original de addtech, ahora principalmente para la consola o uso directo
                TechUtils.addTech(sender, args);
                break;
            default:
                sender.sendMessage(ColorUtils.format("<red>Comando desconocido. Usa /ntca gui o /ntca addtechwizard."));
                break;
        }

        return true;
    }
}