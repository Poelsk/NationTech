package com.nationtech.commands;

import com.nationtech.NationTech;
import com.nationtech.data.NationTechnologyData;
import com.nationtech.tech.Technology;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TechnologyCommands implements CommandExecutor, TabCompleter {

    private final NationTech plugin;

    public TechnologyCommands(NationTech plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "tree":
            case "gui":
                showTechnologyTree(player);
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /tech info <technology_id>");
                    return true;
                }
                showTechnologyInfo(player, args[1]);
                break;
            case "unlock":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /tech unlock <technology_id>");
                    return true;
                }
                unlockTechnology(player, args[1]);
                break;
            case "points":
                showPoints(player);
                break;
            case "list":
                listTechnologies(player);
                break;
            case "reload":
                if (!player.hasPermission("nationtech.admin.reload")) {
                    player.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                reloadPlugin(player);
                break;
            default:
                showHelpMessage(player);
                break;
        }

        return true;
    }

    private void showHelpMessage(Player player) {
        player.sendMessage("§6=== NationTech Commands ===");
        player.sendMessage("§e/tech tree §7- Open the technology tree GUI");
        player.sendMessage("§e/tech info <tech_id> §7- Show technology information");
        player.sendMessage("§e/tech unlock <tech_id> §7- Unlock a technology");
        player.sendMessage("§e/tech points §7- Show your technology points");
        player.sendMessage("§e/tech list §7- List all available technologies");
        if (player.hasPermission("nationtech.admin.reload")) {
            player.sendMessage("§e/tech reload §7- Reload the plugin configuration");
        }
    }

    private void showTechnologyTree(Player player) {
        if (plugin.getTechnologyGUI() != null) {
            plugin.getTechnologyGUI().showTechnologyTree(player);
        } else {
            player.sendMessage("§cTechnology GUI is not available. Please contact an administrator.");
        }
    }

    private void showTechnologyInfo(Player player, String techId) {
        Technology tech = plugin.getTechnologyManager().getTechnology(techId);
        if (tech == null) {
            player.sendMessage("§cTechnology '" + techId + "' not found!");
            return;
        }

        UUID targetUUID = getTargetUUID(player);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        player.sendMessage("§6=== Technology Information ===");
        player.sendMessage("§eID: §f" + tech.getId());
        player.sendMessage("§eName: §f" + tech.getName());
        player.sendMessage("§eDescription: §f" + tech.getDescription());
        player.sendMessage("§eCost: §f" + tech.getCost() + " points");
        player.sendMessage("§eUnlocked: §f" + (data.hasUnlockedTechnology(techId) ? "§aYes" : "§cNo"));

        if (!tech.getPrerequisites().isEmpty()) {
            player.sendMessage("§ePrerequisites:");
            for (String prereq : tech.getPrerequisites()) {
                Technology prereqTech = plugin.getTechnologyManager().getTechnology(prereq);
                String prereqName = prereqTech != null ? prereqTech.getName() : prereq;
                boolean hasPrereq = data.hasUnlockedTechnology(prereq);
                player.sendMessage("  §7- " + prereqName + " " + (hasPrereq ? "§a✓" : "§c✗"));
            }
        }
    }

    private void unlockTechnology(Player player, String techId) {
        Technology tech = plugin.getTechnologyManager().getTechnology(techId);
        if (tech == null) {
            player.sendMessage("§cTechnology '" + techId + "' not found!");
            return;
        }

        UUID targetUUID = getTargetUUID(player);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        // Check if already unlocked
        if (data.hasUnlockedTechnology(techId)) {
            player.sendMessage("§cTechnology '" + tech.getName() + "' is already unlocked!");
            return;
        }

        // Check prerequisites
        for (String prereq : tech.getPrerequisites()) {
            if (!data.hasUnlockedTechnology(prereq)) {
                Technology prereqTech = plugin.getTechnologyManager().getTechnology(prereq);
                String prereqName = prereqTech != null ? prereqTech.getName() : prereq;
                player.sendMessage("§cYou must unlock '" + prereqName + "' first!");
                return;
            }
        }

        // Check if player has enough points
        if (!data.hasEnoughPoints(tech.getCost())) {
            player.sendMessage("§cNot enough technology points! You need " + tech.getCost() + " points but have " + data.getTechnologyPoints() + ".");
            return;
        }

        // Unlock the technology
        data.spendPoints(tech.getCost());
        data.unlockTechnology(techId);
        plugin.getNationDataManager().saveNationData(targetUUID);

        player.sendMessage("§aTechnology '" + tech.getName() + "' unlocked successfully!");
        player.sendMessage("§7Spent " + tech.getCost() + " points. Remaining: " + data.getTechnologyPoints());

        // Update GUI progress
        if (plugin.getTechnologyGUI() != null) {
            plugin.getTechnologyGUI().updatePlayerProgress(player);
        }
    }

    private void showPoints(Player player) {
        UUID targetUUID = getTargetUUID(player);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        String context = plugin.isTownyIntegrationEnabled() &&
                plugin.getTownyHandler().getNationUUIDFromPlayer(player).isPresent()
                ? "nation" : "personal";

        player.sendMessage("§6Technology Points (" + context + "): §e" + data.getTechnologyPoints());
        player.sendMessage("§7Unlocked technologies: " + data.getUnlockedTechnologies().size());
    }

    private void listTechnologies(Player player) {
        UUID targetUUID = getTargetUUID(player);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        player.sendMessage("§6=== Available Technologies ===");
        for (Technology tech : plugin.getTechnologyManager().getAllTechnologies().values()) {
            boolean unlocked = data.hasUnlockedTechnology(tech.getId());
            boolean canUnlock = !unlocked && data.hasEnoughPoints(tech.getCost()) &&
                    tech.getPrerequisites().stream().allMatch(data::hasUnlockedTechnology);

            String status;
            if (unlocked) {
                status = "§a✓ Unlocked";
            } else if (canUnlock) {
                status = "§e⚡ Available";
            } else {
                status = "§c✗ Locked";
            }

            player.sendMessage("§7- §f" + tech.getName() + " §7(" + tech.getCost() + " pts) " + status);
        }
    }

    private void reloadPlugin(Player player) {
        try {
            plugin.getTechnologyManager().loadTechnologies();
            plugin.getNationDataManager().loadAllNationData();
            player.sendMessage("§aPlugin reloaded successfully!");
        } catch (Exception e) {
            player.sendMessage("§cError reloading plugin: " + e.getMessage());
            plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
        }
    }

    private UUID getTargetUUID(Player player) {
        UUID targetUUID = player.getUniqueId();

        if (plugin.isTownyIntegrationEnabled()) {
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

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("tree", "gui", "info", "unlock", "points", "list");
            if (sender.hasPermission("nationtech.admin.reload")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("reload");
            }

            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("unlock"))) {
            for (String techId : plugin.getTechnologyManager().getAllTechnologies().keySet()) {
                if (techId.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(techId);
                }
            }
        }

        return completions;
    }
}