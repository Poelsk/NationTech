package com.nationtech;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class TownyHandler {

    // We cache the methods after looking them up the first time for performance.
    private Method getTownyAPIInstance;
    private Method getResident;
    private Method hasNation;
    private Method getNationOrNull;
    private Method getNationUUID;

    public TownyHandler() {
        try {
            // Find all the classes and methods we need to use from Towny.
            Class<?> townyAPIClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            this.getTownyAPIInstance = townyAPIClass.getMethod("getInstance");

            Class<?> residentClass = Class.forName("com.palmergames.bukkit.towny.object.Resident");
            this.getResident = townyAPIClass.getMethod("getResident", UUID.class);
            this.hasNation = residentClass.getMethod("hasNation");
            this.getNationOrNull = residentClass.getMethod("getNationOrNull");

            Class<?> nationClass = Class.forName("com.palmergames.bukkit.towny.object.Nation");
            this.getNationUUID = nationClass.getMethod("getUUID");

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // If any class or method is not found, log it and this handler will be disabled.
            Bukkit.getLogger().log(Level.SEVERE, "[NationTech] Failed to initialize Towny reflection handler. Towny might be missing or outdated.", e);
        }
    }

    /**
     * Checks if the handler was successfully initialized.
     * @return true if all reflection methods were found.
     */
    private boolean isInitialized() {
        return getTownyAPIInstance != null && getResident != null && hasNation != null && getNationOrNull != null && getNationUUID != null;
    }

    public Optional<UUID> getNationUUIDFromPlayer(Player player) {
        // Do not proceed if the handler failed to initialize.
        if (!isInitialized()) {
            return Optional.empty();
        }

        try {
            // Step 1: Get the TownyAPI instance -> TownyAPI.getInstance()
            Object townyAPI = getTownyAPIInstance.invoke(null);

            // Step 2: Get the player's Resident object -> townyAPI.getResident(player.getUniqueId())
            Object resident = getResident.invoke(townyAPI, player.getUniqueId());
            if (resident == null) return Optional.empty();

            // Step 3: Check if the resident has a nation -> resident.hasNation()
            boolean residentHasNation = (boolean) hasNation.invoke(resident);
            if (!residentHasNation) return Optional.empty();

            // Step 4: Get the Nation object -> resident.getNationOrNull()
            Object nation = getNationOrNull.invoke(resident);
            if (nation == null) return Optional.empty();

            // Step 5: Get the UUID from the Nation object -> nation.getUUID()
            UUID nationUUID = (UUID) getNationUUID.invoke(nation);
            return Optional.ofNullable(nationUUID);

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[NationTech] An error occurred while using Towny reflection.", e);
            return Optional.empty();
        }
    }
}