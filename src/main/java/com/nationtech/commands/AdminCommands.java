package com.nationtech.commands;

import com.nationtech.NationTech;
import com.nationtech.data.NationTechnologyData;
import com.nationtech.tech.Technology;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AdminCommands implements CommandExecutor, TabCompleter {

    private final NationTech plugin;

    public AdminCommands(NationTech plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nationtech.admin.*")) {
            sender.sendMessage("§cYou don't have permission to use admin commands!");
            return true;
        }

        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /techadmin give <player> <points>");
                    return true;
                }
                givePoints(sender, args[1], args[2]);
                break;
            case "take":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /techadmin take <player> <points>");
                    return true;
                }
                takePoints(sender, args[1], args[2]);
                break;
            case "set":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /techadmin set <player> <points>");
                    return true;
                }
                setPoints(sender, args[1], args[2]);
                break;
            case "unlock":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /techadmin unlock <player> <technology>");
                    return true;
                }
                forceUnlock(sender, args[1], args[2]);
                break;
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /techadmin reset <player>");
                    return true;
                }
                resetProgress(sender, args[1]);
                break;
            case "info":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /techadmin info <player>");
                    return true;
                }
                showPlayerInfo(sender, args[1]);
                break;
            case "reload":
                reloadPlugin(sender);
                break;
            default:
                showAdminHelp(sender);
                break;
        }

        return true;
    }

    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage("§6=== NationTech Admin Commands ===");
        sender.sendMessage("§e/techadmin give <player> <points> §7- Give technology points");
        sender.sendMessage("§e/techadmin take <player> <points> §7- Take technology points");
        sender.sendMessage("§e/techadmin set <player> <points> §7- Set technology points");
        sender.sendMessage("§e/techadmin unlock <player> <tech> §7- Force unlock technology");
        sender.sendMessage("§e/techadmin reset <player> §7- Reset all progress");
        sender.sendMessage("§e/techadmin info <player> §7- Show player information");
        sender.sendMessage("§e/techadmin reload §7- Reload plugin configuration");
    }

    private void givePoints(CommandSender sender, String playerName, String pointsStr) {
        try {
            int points = Integer.parseInt(pointsStr);
            if (points <= 0) {
                sender.sendMessage("§cPoints must be a positive number!");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + playerName + "' not found!");
                return;
            }

            UUID targetUUID = getTargetUUID(target);
            NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

            int oldPoints = data.getTechnologyPoints();
            data.addTechnologyPoints(points);
            plugin.getNationDataManager().saveNationData(targetUUID);

            sender.sendMessage("§aGave " + points + " technology points to " + target.getName() +
                    " (" + oldPoints + " → " + data.getTechnologyPoints() + ")");
            target.sendMessage("§aYou received " + points + " technology points from an administrator!");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number: " + pointsStr);
        }
    }

    private void takePoints(CommandSender sender, String playerName, String pointsStr) {
        try {
            int points = Integer.parseInt(pointsStr);
            if (points <= 0) {
                sender.sendMessage("§cPoints must be a positive number!");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + playerName + "' not found!");
                return;
            }

            UUID targetUUID = getTargetUUID(target);
            NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

            int oldPoints = data.getTechnologyPoints();
            data.spendPoints(Math.min(points, oldPoints)); // Don't go negative
            plugin.getNationDataManager().saveNationData(targetUUID);

            sender.sendMessage("§aTook " + points + " technology points from " + target.getName() +
                    " (" + oldPoints + " → " + data.getTechnologyPoints() + ")");
            target.sendMessage("§c" + points + " technology points were removed by an administrator!");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number: " + pointsStr);
        }
    }

    private void setPoints(CommandSender sender, String playerName, String pointsStr) {
        try {
            int points = Integer.parseInt(pointsStr);
            if (points < 0) {
                sender.sendMessage("§cPoints cannot be negative!");
                return;
            }

            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + playerName + "' not found!");
                return;
            }

            UUID targetUUID = getTargetUUID(target);
            NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

            int oldPoints = data.getTechnologyPoints();
            // Reset to 0 then add the desired amount
            data.spendPoints(oldPoints);
            data.addTechnologyPoints(points);
            plugin.getNationDataManager().saveNationData(targetUUID);

            sender.sendMessage("§aSet " + target.getName() + "'s technology points to " + points +
                    " (was " + oldPoints + ")");
            target.sendMessage("§eYour technology points were set to " + points + " by an administrator!");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number: " + pointsStr);
        }
    }

    private void forceUnlock(CommandSender sender, String playerName, String techId) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer '" + playerName + "' not found!");
            return;
        }

        Technology tech = plugin.getTechnologyManager().getTechnology(techId);
        if (tech == null) {
            sender.sendMessage("§cTechnology '" + techId + "' not found!");
            return;
        }

        UUID targetUUID = getTargetUUID(target);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        if (data.hasUnlockedTechnology(techId)) {
            sender.sendMessage("§c" + target.getName() + " already has '" + tech.getName() + "' unlocked!");
            return;
        }

        data.unlockTechnology(techId);
        plugin.getNationDataManager().saveNationData(targetUUID);

        sender.sendMessage("§aForce unlocked '" + tech.getName() + "' for " + target.getName());
        target.sendMessage("§aTechnology '" + tech.getName() + "' was unlocked by an administrator!");

        // Update GUI if available
        if (plugin.getTechnologyGUI() != null) {
            plugin.getTechnologyGUI().updatePlayerProgress(target);
        }
    }

    private void resetProgress(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer '" + playerName + "' not found!");
            return;
        }

        UUID targetUUID = getTargetUUID(target);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        // Reset points and technologies
        int oldPoints = data.getTechnologyPoints();
        int techCount = data.getUnlockedTechnologies().size();

        data.spendPoints(oldPoints);
        data.getUnlockedTechnologies().clear();

        // Give starting points
        int startingPoints = plugin.getConfig().getInt("technology_points.starting_points", 10);
        data.addTechnologyPoints(startingPoints);

        plugin.getNationDataManager().saveNationData(targetUUID);

        sender.sendMessage("§aReset " + target.getName() + "'s progress: " +
                oldPoints + " points and " + techCount + " technologies removed");
        target.sendMessage("§cYour technology progress was reset by an administrator!");

        // Update GUI if available
        if (plugin.getTechnologyGUI() != null) {
            plugin.getTechnologyGUI().updatePlayerProgress(target);
        }
    }

    private void showPlayerInfo(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cPlayer '" + playerName + "' not found!");
            return;
        }

        UUID targetUUID = getTargetUUID(target);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        sender.sendMessage("§6=== Technology Info: " + target.getName() + " ===");
        sender.sendMessage("§ePoints: §f" + data.getTechnologyPoints());
        sender.sendMessage("§eUnlocked Technologies: §f" + data.getUnlockedTechnologies().size());

        if (!data.getUnlockedTechnologies().isEmpty()) {
            sender.sendMessage("§eTechnologies:");
            for (String techId : data.getUnlockedTechnologies()) {
                Technology tech = plugin.getTechnologyManager().getTechnology(techId);
                String techName = tech != null ? tech.getName() : techId;
                sender.sendMessage("  §7- " + techName + " §8(" + techId + ")");
            }
        }

        // Show context (personal vs nation)
        String context = "Personal";
        if (plugin.isTownyIntegrationEnabled()) {
            var nationUUID = plugin.getTownyHandler().getNationUUIDFromPlayer(target);
            if (nationUUID.isPresent()) {
                context = "Nation";
            }
        }
        sender.sendMessage("§eContext: §f" + context);
    }

    private void reloadPlugin(CommandSender sender) {
        try {
            plugin.reloadConfiguration();
            sender.sendMessage("§aPlugin reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage("§cError reloading plugin: " + e.getMessage());
            plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
        }
    }

    private UUID getTargetUUID(Player player) {
        UUID targetUUID = player.getUniqueId();

        if (plugin.isTownyIntegrationEnabled() && plugin.getConfig().getBoolean("towny.use_nation_points", true)) {
            var nationUUID = plugin.getTownyHandler().getNationUUIDFromPlayer(player);
            if (nationUUID.isPresent()) {
                targetUUID = nationUUID.get();
            }
        }

        return targetUUID;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("nationtech.admin.*")) {
            return completions;
        }

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("give", "take", "set", "unlock", "reset", "info", "reload");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2) {
            // Player name completion for most commands
            if (!args[0].equalsIgnoreCase("reload")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("unlock")) {
                // Technology completion
                for (String techId : plugin.getTechnologyManager().getAllTechnologies().keySet()) {
                    if (techId.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(techId);
                    }
                }
            } else if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set")) {
                // Number suggestions
                completions.add("10");
                completions.add("50");
                completions.add("100");
                completions.add("500");
                completions.add("1000");
            }
        }

        return completions;
    }
}