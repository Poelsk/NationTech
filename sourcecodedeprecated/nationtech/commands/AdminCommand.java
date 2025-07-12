package com.tunacion.nationtech.command;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.tunacion.nationtech.NationTech;
import com.tunacion.nationtech.config.Lang;
import com.tunacion.nationtech.tech.Tech;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Maneja los comandos de administrador para NationTech.
 */
public class AdminCommand implements CommandExecutor {

    private final NationTech plugin;

    public AdminCommand(NationTech plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("nationtech.admin")) {
            sender.sendMessage(Lang.NO_PERMISSION.getMessage());
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Lang.ADMIN_USAGE.getMessage());
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String nationName = args[1];
        Nation nation = TownyAPI.getInstance().getNation(nationName);

        if (nation == null) {
            sender.sendMessage(Lang.NATION_NOT_FOUND.getMessage().replace("%nation%", nationName));
            return true;
        }

        switch (subCommand) {
            case "grant":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /nta grant <nation> <tech_id>");
                    return true;
                }
                String techIdToGrant = args[2];
                Tech techToGrant = plugin.getTechManager().getTech(techIdToGrant);
                if (techToGrant == null) {
                    sender.sendMessage(Lang.TECH_NOT_FOUND.getMessage().replace("%tech%", techIdToGrant));
                    return true;
                }
                plugin.getDataManager().grantTech(nation.getUUID(), techIdToGrant);
                sender.sendMessage(Lang.TECH_GRANTED.getMessage()
                        .replace("%tech%", techToGrant.getName())
                        .replace("%nation%", nation.getName()));
                break;

            case "revoke":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /nta revoke <nation> <tech_id>");
                    return true;
                }
                String techIdToRevoke = args[2];
                Tech techToRevoke = plugin.getTechManager().getTech(techIdToRevoke);
                if (techToRevoke == null) {
                    sender.sendMessage(Lang.TECH_NOT_FOUND.getMessage().replace("%tech%", techIdToRevoke));
                    return true;
                }
                plugin.getDataManager().revokeTech(nation.getUUID(), techIdToRevoke);
                sender.sendMessage(Lang.TECH_REVOKED.getMessage()
                        .replace("%tech%", techToRevoke.getName())
                        .replace("%nation%", nation.getName()));
                break;

            default:
                sender.sendMessage(Lang.ADMIN_USAGE.getMessage());
                break;
        }
        return true;
    }
}