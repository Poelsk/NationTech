package com.nationtech.listeners;

import com.nationtech.NationTech;
import com.nationtech.TownyHandler;
import com.nationtech.data.NationTechnologyData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

import java.util.Optional;
import java.util.UUID;

public class TechnologyPointsListener implements Listener {

    private final NationTech plugin;

    public TechnologyPointsListener(NationTech plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Material brokenBlock = event.getBlock().getType();

        // Check if notifications are enabled
        boolean sendMessages = plugin.getConfig().getBoolean("notifications.point_gain_messages", true);

        // Get points for mining this material
        int points = getMiningPoints(brokenBlock);
        if (points <= 0) return;

        UUID targetUUID = getTargetUUID(player);
        String context = getContextMessage(player);

        // Award points
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        // Check max points limit
        int maxPoints = plugin.getConfig().getInt("technology_points.max_points", 0);
        if (maxPoints > 0 && data.getTechnologyPoints() >= maxPoints) {
            if (sendMessages) {
                player.sendMessage("§cTechnology points are at maximum capacity (" + maxPoints + ")!");
            }
            return;
        }

        data.addTechnologyPoints(points);
        plugin.getNationDataManager().saveNationData(targetUUID);

        // Send notification
        if (sendMessages) {
            player.sendMessage("§a+§e" + points + "§a technology points " + context + "! §7(Total: " + data.getTechnologyPoints() + ")");
        }

        // Log transaction if enabled
        if (plugin.getConfig().getBoolean("development.log_point_transactions", false)) {
            plugin.getLogger().info("Player " + player.getName() + " gained " + points + " points from mining " + brokenBlock.name());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerHarvest(PlayerHarvestBlockEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Material harvestedCrop = event.getHarvestedBlock().getType();

        // Get points for farming this crop
        int points = getFarmingPoints(harvestedCrop);
        if (points <= 0) return;

        boolean sendMessages = plugin.getConfig().getBoolean("notifications.point_gain_messages", true);
        UUID targetUUID = getTargetUUID(player);
        String context = getContextMessage(player);

        // Award points
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        // Check max points limit
        int maxPoints = plugin.getConfig().getInt("technology_points.max_points", 0);
        if (maxPoints > 0 && data.getTechnologyPoints() >= maxPoints) {
            if (sendMessages) {
                player.sendMessage("§cTechnology points are at maximum capacity!");
            }
            return;
        }

        data.addTechnologyPoints(points);
        plugin.getNationDataManager().saveNationData(targetUUID);

        // Send notification
        if (sendMessages) {
            player.sendMessage("§a+§e" + points + "§a technology points from farming " + context + "! §7(Total: " + data.getTechnologyPoints() + ")");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player player = (Player) event.getEntity().getKiller();
        EntityType entityType = event.getEntity().getType();

        // Get points for killing this mob
        int points = getMobKillPoints(entityType);
        if (points <= 0) return;

        boolean sendMessages = plugin.getConfig().getBoolean("notifications.point_gain_messages", true);
        UUID targetUUID = getTargetUUID(player);
        String context = getContextMessage(player);

        // Award points
        NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);

        // Check max points limit
        int maxPoints = plugin.getConfig().getInt("technology_points.max_points", 0);
        if (maxPoints > 0 && data.getTechnologyPoints() >= maxPoints) {
            if (sendMessages) {
                player.sendMessage("§cTechnology points are at maximum capacity!");
            }
            return;
        }

        data.addTechnologyPoints(points);
        plugin.getNationDataManager().saveNationData(targetUUID);

        // Send notification
        if (sendMessages) {
            player.sendMessage("§a+§e" + points + "§a technology points from combat " + context + "! §7(Total: " + data.getTechnologyPoints() + ")");
        }
    }

    private int getMiningPoints(Material material) {
        ConfigurationSection miningSection = plugin.getConfig().getConfigurationSection("technology_points.sources.mining");
        if (miningSection == null) return 0;

        return miningSection.getInt(material.name(), 0);
    }

    private int getFarmingPoints(Material material) {
        ConfigurationSection farmingSection = plugin.getConfig().getConfigurationSection("technology_points.sources.farming");
        if (farmingSection == null) return 0;

        return farmingSection.getInt(material.name(), 0);
    }

    private int getMobKillPoints(EntityType entityType) {
        ConfigurationSection mobSection = plugin.getConfig().getConfigurationSection("technology_points.sources.mob_kills");
        if (mobSection == null) return 0;

        return mobSection.getInt(entityType.name(), 0);
    }

    private UUID getTargetUUID(Player player) {
        UUID targetUUID = player.getUniqueId();

        if (plugin.isTownyIntegrationEnabled() && plugin.getConfig().getBoolean("towny.use_nation_points", true)) {
            TownyHandler townyHandler = plugin.getTownyHandler();
            Optional<UUID> nationUUIDOptional = townyHandler.getNationUUIDFromPlayer(player);

            if (nationUUIDOptional.isPresent()) {
                targetUUID = nationUUIDOptional.get();
            } else if (!plugin.getConfig().getBoolean("towny.fallback_to_individual", true)) {
                // If fallback is disabled and player has no nation, don't award points
                return null;
            }
        }

        return targetUUID;
    }

    private String getContextMessage(Player player) {
        if (plugin.isTownyIntegrationEnabled() && plugin.getConfig().getBoolean("towny.use_nation_points", true)) {
            TownyHandler townyHandler = plugin.getTownyHandler();
            Optional<UUID> nationUUIDOptional = townyHandler.getNationUUIDFromPlayer(player);

            if (nationUUIDOptional.isPresent()) {
                return "for your nation";
            } else {
                return "personally";
            }
        }

        return "personally";
    }
}