package com.tunacion.nationtech.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.tunacion.nationtech.NationTech;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DataManager {

    private final NationTech plugin;
    private final File dataFolder;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ConcurrentHashMap<UUID, NationProgress> nationProgressCache = new ConcurrentHashMap<>();

    public DataManager(NationTech plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public NationProgress getNationProgress(UUID nationUUID) {
        return nationProgressCache.computeIfAbsent(nationUUID, this::loadNationProgress);
    }

    private NationProgress loadNationProgress(UUID nationUUID) {
        File nationFile = new File(dataFolder, nationUUID.toString() + ".json");
        if (nationFile.exists()) {
            try (FileReader reader = new FileReader(nationFile)) {
                return gson.fromJson(reader, NationProgress.class);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo cargar el archivo de datos para la nación " + nationUUID, e);
            }
        }
        return new NationProgress(nationUUID);
    }

    public void saveNationProgress(NationProgress nationProgress) {
        File nationFile = new File(dataFolder, nationProgress.getNationUUID().toString() + ".json");
        try (FileWriter writer = new FileWriter(nationFile)) {
            gson.toJson(nationProgress, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar el archivo de datos para la nación " + nationProgress.getNationUUID(), e);
        }
    }

    public void saveAllNationProgress() {
        nationProgressCache.values().forEach(this::saveNationProgress);
    }

    /**
     * Otorga una tecnología a una nación y le da la recompensa correspondiente.
     */
    public void grantTech(UUID nationUUID, String techId) {
        NationProgress progress = getNationProgress(nationUUID);
        // Evita dar la recompensa si ya tiene la tecnología
        if (progress.hasTech(techId)) {
            return;
        }
        progress.addTech(techId);

        // Llama al RewardManager para dar la recompensa.
        plugin.getRewardManager().giveReward(nationUUID, techId);

        saveProgressAsync(progress);
    }

    public void revokeTech(UUID nationUUID, String techId) {
        NationProgress progress = getNationProgress(nationUUID);
        progress.removeTech(techId);
        saveProgressAsync(progress);
    }

    private void saveProgressAsync(NationProgress progress) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveNationProgress(progress));
    }
}