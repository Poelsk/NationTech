package com.github.nationTech.requirements;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemRequirement implements Requirement {

    private final Material material;
    private final int amount;

    public ItemRequirement(Material material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    @Override
    public boolean check(Player player) {
        return player.getInventory().contains(material, amount);
    }

    @Override
    public void consume(Player player) {
        player.getInventory().removeItem(new ItemStack(material, amount));
    }

    @Override
    public String getLoreText() {
        // Formatea el nombre del material para que sea más legible.
        String materialName = material.name().replace("_", " ").toLowerCase();
        return String.format("%dx %s", amount, materialName);
    }
}