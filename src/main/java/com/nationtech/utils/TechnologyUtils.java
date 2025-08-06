package com.nationtech.utils;

import com.nationtech.NationTech;
import com.nationtech.data.NationTechnologyData;
import com.nationtech.tech.Technology;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TechnologyUtils {

    private final NationTech plugin;

    public TechnologyUtils(NationTech plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the appropriate UUID for technology data (player or nation)
     */
    public UUID getTargetUUID(Player player) {
        UUID targetUUID = player.getUniqueId();

        if (plugin.isTownyIntegrationEnabled() && plugin.getConfig().getBoolean("towny.use_nation_points", true)) {
            var nationUUID = plugin.getTownyHandler().getNationUUIDFromPlayer(player);
            if (nationUUID.isPresent()) {
                targetUUID = nationUUID.get();
            }
        }

        return targetUUID;
    }

    /**
     * Get context message for point awards (personal/nation)
     */
    public String getContextMessage(Player player) {
        if (plugin.isTownyIntegrationEnabled() && plugin.getConfig().getBoolean("towny.use_nation_points", true)) {
            var nationUUID = plugin.getTownyHandler().getNationUUIDFromPlayer(player);
            if (nationUUID.isPresent()) {
                return "for your nation";
            }
        }
        return "personally";
    }

    /**
     * Check if a player can unlock a specific technology
     */
    public boolean canUnlockTechnology(Player player, String techId) {
        Technology tech = plugin.getTechnologyManager().getTechnology(techId);
        if (tech == null) return false;

        UUID targetUUID = getTargetUUID(player);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        // Already unlocked
        if (data.hasUnlockedTechnology(techId)) return false;

        // Check points
        if (!data.hasEnoughPoints(tech.getCost())) return false;

        // Check prerequisites
        for (String prereq : tech.getPrerequisites()) {
            if (!data.hasUnlockedTechnology(prereq)) return false;
        }

        return true;
    }

    /**
     * Get all technologies that a player can currently unlock
     */
    public List<Technology> getAvailableTechnologies(Player player) {
        List<Technology> available = new ArrayList<>();
        UUID targetUUID = getTargetUUID(player);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        for (Technology tech : plugin.getTechnologyManager().getAllTechnologies().values()) {
            if (canUnlockTechnology(player, tech.getId())) {
                available.add(tech);
            }
        }

        return available;
    }

    /**
     * Get missing prerequisites for a technology
     */
    public List<String> getMissingPrerequisites(Player player, String techId) {
        Technology tech = plugin.getTechnologyManager().getTechnology(techId);
        if (tech == null) return new ArrayList<>();

        UUID targetUUID = getTargetUUID(player);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        List<String> missing = new ArrayList<>();
        for (String prereq : tech.getPrerequisites()) {
            if (!data.hasUnlockedTechnology(prereq)) {
                missing.add(prereq);
            }
        }

        return missing;
    }

    /**
     * Calculate technology tree completion percentage
     */
    public double getCompletionPercentage(Player player) {
        UUID targetUUID = getTargetUUID(player);
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        int totalTechs = plugin.getTechnologyManager().getAllTechnologies().size();
        int unlockedTechs = data.getUnlockedTechnologies().size();

        if (totalTechs == 0) return 0.0;
        return (double) unlockedTechs / totalTechs * 100.0;
    }

    /**
     * Format technology points with proper formatting
     */
    public String formatPoints(int points) {
        if (points >= 1000000) {
            return String.format("%.1fM", points / 1000000.0);
        } else if (points >= 1000) {
            return String.format("%.1fK", points / 1000.0);
        } else {
            return String.valueOf(points);
        }
    }

    /**
     * Send formatted message to player
     */
    public void sendMessage(Player player, String message) {
        player.sendMessage("§8[§6NationTech§8] §r" + message);
    }

    /**
     * Send success message
     */
    public void sendSuccess(Player player, String message) {
        sendMessage(player, "§a" + message);
    }

    /**
     * Send error message
     */
    public void sendError(Player player, String message) {
        sendMessage(player, "§c" + message);
    }

    /**
     * Send warning message
     */
    public void sendWarning(Player player, String message) {
        sendMessage(player, "§e" + message);
    }

    /**
     * Check if point notifications are enabled
     */
    public boolean arePointNotificationsEnabled() {
        return plugin.getConfig().getBoolean("notifications.point_gain_messages", true);
    }

    /**
     * Check if unlock notifications are enabled
     */
    public boolean areUnlockNotificationsEnabled() {
        return plugin.getConfig().getBoolean("notifications.tech_unlock_messages", true);
    }

    /**
     * Get maximum points limit (0 = unlimited)
     */
    public int getMaxPoints() {
        return plugin.getConfig().getInt("technology_points.max_points", 0);
    }

    /**
     * Get starting points for new players/nations
     */
    public int getStartingPoints() {
        return plugin.getConfig().getInt("technology_points.starting_points", 10);
    }
}