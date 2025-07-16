package com.github.nationTech.requirements;

import com.github.nationTech.NationTech;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class MoneyRequirement implements Requirement {

    private final double amount;

    public MoneyRequirement(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean check(Player player) {
        Economy economy = NationTech.getInstance().getEconomy();
        if (economy == null) return false; // Si no hay economía, no se puede cumplir.
        return economy.has(player, amount);
    }

    @Override
    public void consume(Player player) {
        Economy economy = NationTech.getInstance().getEconomy();
        if (economy == null) return;
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public String getLoreText() {
        Economy economy = NationTech.getInstance().getEconomy();
        // Muestra el nombre de la moneda si está disponible (ej: 100.0 Dólares)
        String currencyName = economy != null ? economy.currencyNamePlural() : "";
        return String.format("%.2f %s", amount, currencyName).trim();
    }
}