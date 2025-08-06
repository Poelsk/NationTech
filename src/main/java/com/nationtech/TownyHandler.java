package com.nationtech;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class TownyHandler {

    public Optional<UUID> getNationUUIDFromPlayer(Player player) {
        try {
            Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
            if (resident != null && resident.hasNation()) {
                return Optional.ofNullable(resident.getNationOrNull()).map(nation -> nation.getUUID());
            }
            return Optional.empty();
        } catch (Exception e) {
            NationTech.getInstance().getLogger().log(Level.SEVERE, "An error occurred while accessing the TownyAPI.", e);
            return Optional.empty();
        }
    }
}