package com.nationtech.listeners;

import com.nationtech.NationTech;
import com.nationtech.TownyHandler;
import com.nationtech.data.NationTechnologyData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;
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
            UUID targetUUID = player.getUniqueId();

            if (plugin.isTownyIntegrationEnabled()) {
                TownyHandler townyHandler = plugin.getTownyHandler();
                Optional<UUID> nationUUIDOptional = townyHandler.getNationUUIDFromPlayer(player);

                if (nationUUIDOptional.isPresent()) {
                    targetUUID = nationUUIDOptional.get();
                    player.sendMessage("You earned " + pointsPerBlock + " technology points for your nation!");
                } else {
                    player.sendMessage("You must be in a nation to earn points for it. Points will be assigned to you for now.");
                }
            } else {
                player.sendMessage("Towny is not active. Points will be assigned to you.");
            }

            NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);
            data.addTechnologyPoints(pointsPerBlock);
            plugin.getNationDataManager().saveNationData(targetUUID);
        }
    }
}