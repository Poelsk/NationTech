package com.nationtech.listeners;

import com.nationtech.NationTech;
import com.nationtech.data.NationTechnologyData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class TechnologyPointsListener implements Listener {

    private final NationTech plugin;

    public TechnologyPointsListener(NationTech plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material brokenBlock = event.getBlock().getType();

        Material targetBlock = Material.IRON_ORE;
        int pointsPerBlock = 5;

        if (brokenBlock == targetBlock) {
            UUID playerUUID = player.getUniqueId();

            NationTechnologyData playerData = plugin.getNationDataManager().getNationData(playerUUID);
            playerData.addTechnologyPoints(pointsPerBlock);
            plugin.getNationDataManager().saveNationData(playerUUID);

            player.sendMessage("You earned " + pointsPerBlock + " technology points! Total: " + playerData.getTechnologyPoints());
            plugin.getLogger().info("Player " + player.getName() + " earned " + pointsPerBlock + " points. Total: " + playerData.getTechnologyPoints());
        }
    }
}