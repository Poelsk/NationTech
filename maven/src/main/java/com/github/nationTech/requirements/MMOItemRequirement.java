package com.github.nationTech.requirements;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData; // Importación corregida y verificada
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class MMOItemRequirement implements Requirement {

    private final String itemType;
    private final String itemId;
    private final int amount;

    public MMOItemRequirement(String itemType, String itemId, int amount) {
        this.itemType = itemType.toUpperCase();
        this.itemId = itemId.toUpperCase();
        this.amount = amount;
    }

    @Override
    public boolean check(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) continue;

            NBTItem nbtItem = NBTItem.get(item);
            if (nbtItem.hasTag("MMOITEMS_ITEM_TYPE") && nbtItem.hasTag("MMOITEMS_ITEM_ID")) {
                String type = nbtItem.getString("MMOITEMS_ITEM_TYPE");
                String id = nbtItem.getString("MMOITEMS_ITEM_ID");

                if (type.equalsIgnoreCase(this.itemType) && id.equalsIgnoreCase(this.itemId)) {
                    count += item.getAmount();
                }
            }
        }
        return count >= this.amount;
    }

    @Override
    public void consume(Player player) {
        PlayerInventory inventory = player.getInventory();
        int remaining = this.amount;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (remaining <= 0) return;

            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            NBTItem nbtItem = NBTItem.get(item);
            if (nbtItem.hasTag("MMOITEMS_ITEM_TYPE") && nbtItem.hasTag("MMOITEMS_ITEM_ID")) {
                String type = nbtItem.getString("MMOITEMS_ITEM_TYPE");
                String id = nbtItem.getString("MMOITEMS_ITEM_ID");

                if (type.equalsIgnoreCase(this.itemType) && id.equalsIgnoreCase(this.itemId)) {
                    if (item.getAmount() > remaining) {
                        item.setAmount(item.getAmount() - remaining);
                        remaining = 0;
                    } else {
                        remaining -= item.getAmount();
                        inventory.setItem(i, null);
                    }
                }
            }
        }
    }

    @Override
    public String getLoreText() {
        try {
            Type type = MMOItems.plugin.getTypes().get(this.itemType);
            if (type == null) {
                return String.format("%dx %s (Custom Item)", amount, itemId);
            }

            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, this.itemId);
            if (template == null) {
                return String.format("%dx %s (Custom Item)", amount, itemId);
            }

            // --- LÓGICA FINAL Y VERIFICADA ---
            // Se accede al mapa de estadísticas base de la plantilla
            Map<ItemStat, RandomStatData> baseData = template.getBaseItemData();

            // Se busca la estadística del nombre en el mapa
            if (baseData.containsKey(ItemStats.NAME)) {
                // Se castea al tipo correcto (StringData)
                StringData nameData = (StringData) baseData.get(ItemStats.NAME);
                String displayName = nameData.toString();
                // Se limpia el formato para mostrarlo en el lore
                return String.format("%dx %s", amount, ChatColor.stripColor(displayName));
            } else {
                // Si la plantilla no tiene un nombre definido, se usa el ID como fallback.
                return String.format("%dx %s (Custom Item)", amount, itemId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return String.format("%dx %s (Custom Item)", amount, itemId);
        }
    }
}