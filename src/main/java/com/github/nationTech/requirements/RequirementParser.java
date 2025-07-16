package com.github.nationTech.requirements;

import com.github.nationTech.NationTech;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RequirementParser {

    public static List<Requirement> parse(String requirementString) {
        List<Requirement> requirements = new ArrayList<>();
        if (requirementString == null || requirementString.trim().isEmpty()) {
            return requirements;
        }

        NationTech plugin = NationTech.getInstance();
        String[] parts = requirementString.split(",");
        for (String part : parts) {
            String[] details = part.split(":");
            if (details.length < 2) {
                plugin.getLogger().warning("Requisito malformado (ignorado): " + part);
                continue;
            }

            String type = details[0].toLowerCase();

            try {
                if (type.equals("item") && details.length == 3) {
                    Material material = Material.matchMaterial(details[1].toUpperCase());
                    int amount = Integer.parseInt(details[2]);
                    if (material != null) {
                        requirements.add(new ItemRequirement(material, amount));
                    } else {
                        plugin.getLogger().warning("Material en requisito no encontrado (ignorado): " + details[1]);
                    }
                } else if (type.equals("money") && details.length == 2) {
                    double amount = Double.parseDouble(details[1]);
                    requirements.add(new MoneyRequirement(amount));
                } else if (type.equals("mmoitem") && details.length == 3) {
                    String[] mmoIdParts = details[1].split("\\.");
                    if (mmoIdParts.length != 2) {
                        plugin.getLogger().warning("Formato de MMOItem incorrecto (ignorado): " + details[1] + ". Debe ser TIPO.ID");
                        continue;
                    }
                    String itemType = mmoIdParts[0].toUpperCase();
                    String itemId = mmoIdParts[1].toUpperCase();
                    int amount = Integer.parseInt(details[2]);
                    requirements.add(new MMOItemRequirement(itemType, itemId, amount));
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().log(Level.WARNING, "Número en requisito inválido (ignorado): " + part, e);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error inesperado al parsear requisito: " + part, e);
            }
        }
        return requirements;
    }
}