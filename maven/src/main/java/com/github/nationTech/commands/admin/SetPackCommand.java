package com.github.nationTech.commands.admin;

import com.github.nationTech.NationTech;
import com.github.nationTech.commands.SubCommand;
import com.github.nationTech.utils.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SetPackCommand implements SubCommand {

    private final NationTech plugin = NationTech.getInstance();

    @Override
    public String getName() {
        return "setpack";
    }

    @Override
    public String getSyntax() {
        return "/ntca setpack <url>";
    }

    @Override
    public String getPermission() {
        return "nationtech.admin.setpack";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            MessageManager.sendMessage(sender, "<red>Argumentos insuficientes. Sintaxis:");
            MessageManager.sendRawMessage(sender, "<gray>" + getSyntax());
            return;
        }

        String url = args[0];
        plugin.getConfig().set("resource-pack-url", url);
        plugin.saveConfig();

        MessageManager.sendMessage(sender, "<green>¡URL del paquete de recursos actualizada!");
        MessageManager.sendMessage(sender, "<gray>Los nuevos jugadores recibirán la solicitud al entrar. Los actuales pueden reconectarse.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // La URL es un texto libre, no necesita sugerencias.
        return new ArrayList<>();
    }
}