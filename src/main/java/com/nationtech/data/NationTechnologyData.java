package com.nationtech.data;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NationTechnologyData {

    private final UUID ownerUUID;
    private int technologyPoints;
    private final List<String> unlockedTechnologies;

    public NationTechnologyData(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.technologyPoints = 0;
        this.unlockedTechnologies = new ArrayList<>();
    }

    private NationTechnologyData(UUID ownerUUID, int points, List<String> unlocked) {
        this.ownerUUID = ownerUUID;
        this.technologyPoints = points;
        this.unlockedTechnologies = new ArrayList<>(unlocked);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public int getTechnologyPoints() {
        return technologyPoints;
    }

    public void addTechnologyPoints(int amount) {
        this.technologyPoints += amount;
    }

    public boolean hasEnoughPoints(int cost) {
        return this.technologyPoints >= cost;
    }

    public void spendPoints(int amount) {
        this.technologyPoints -= amount;
    }

    public List<String> getUnlockedTechnologies() {
        return new ArrayList<>(unlockedTechnologies);
    }

    public boolean hasUnlockedTechnology(String techId) {
        return unlockedTechnologies.contains(techId.toLowerCase());
    }

    public void unlockTechnology(String techId) {
        if (!hasUnlockedTechnology(techId)) {
            unlockedTechnologies.add(techId.toLowerCase());
        }
    }

    public void save(FileConfiguration config) {
        config.set("points", technologyPoints);
        config.set("unlocked", unlockedTechnologies);
    }

    public static NationTechnologyData fromConfiguration(UUID owner, FileConfiguration config) {
        int points = config.getInt("points", 0);
        List<String> unlocked = config.getStringList("unlocked");
        return new NationTechnologyData(owner, points, unlocked);
    }
}