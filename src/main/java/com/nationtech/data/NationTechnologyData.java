package com.nationtech.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NationTechnologyData {

    private UUID nationUUID;
    private int technologyPoints;
    private List<String> unlockedTechnologies;

    public NationTechnologyData(UUID nationUUID) {
        this.nationUUID = nationUUID;
        this.technologyPoints = 0;
        this.unlockedTechnologies = new ArrayList<>();
    }

    public NationTechnologyData(UUID nationUUID, int technologyPoints, List<String> unlockedTechnologies) {
        this.nationUUID = nationUUID;
        this.technologyPoints = technologyPoints;
        this.unlockedTechnologies = unlockedTechnologies != null ? new ArrayList<>(unlockedTechnologies) : new ArrayList<>();
    }

    public UUID getNationUUID() {
        return nationUUID;
    }

    public int getTechnologyPoints() {
        return technologyPoints;
    }

    public void addTechnologyPoints(int amount) {
        if (amount > 0) {
            this.technologyPoints += amount;
        }
    }

    public void removeTechnologyPoints(int amount) {
        if (amount > 0) {
            this.technologyPoints -= amount;
            if (this.technologyPoints < 0) {
                this.technologyPoints = 0;
            }
        }
    }

    public List<String> getUnlockedTechnologies() {
        return new ArrayList<>(unlockedTechnologies);
    }

    public boolean isTechnologyUnlocked(String techId) {
        return unlockedTechnologies.contains(techId);
    }

    public void unlockTechnology(String techId) {
        if (!isTechnologyUnlocked(techId)) {
            unlockedTechnologies.add(techId);
        }
    }

    public void setTechnologyPoints(int technologyPoints) {
        this.technologyPoints = technologyPoints;
    }

    public void setUnlockedTechnologies(List<String> unlockedTechnologies) {
        this.unlockedTechnologies = unlockedTechnologies != null ? new ArrayList<>(unlockedTechnologies) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "NationTechnologyData{" +
                "nationUUID=" + nationUUID +
                ", technologyPoints=" + technologyPoints +
                ", unlockedTechnologies=" + unlockedTechnologies.size() + " technologies" +
                '}';
    }
}