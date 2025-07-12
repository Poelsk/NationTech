package com.tunacion.nationtech.tech;

import org.bukkit.Material;
import java.util.List;

public class Tech {

    private final String id;
    private final String name;
    private final List<String> description;
    private final Material material;
    private final int slot;
    private final List<String> rewardCommands; // Campo para los comandos de recompensa

    public Tech(String id, String name, List<String> description, Material material, int slot, List<String> rewardCommands) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.material = material;
        this.slot = slot;
        this.rewardCommands = rewardCommands;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    public Material getMaterial() {
        return material;
    }

    public int getSlot() {
        return slot;
    }

    // Getter para los comandos de recompensa
    public List<String> getRewardCommands() {
        return rewardCommands;
    }
}