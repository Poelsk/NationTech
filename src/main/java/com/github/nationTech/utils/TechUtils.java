package com.github.nationTech.utils;

import com.github.nationTech.NationTech;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TechUtils {

    public static void addTech(CommandSender sender, String args) {
        if (args.length < 7) {
            sender.sendMessage("§cUsage: /ntca addtech <id> <parent> <name> <icon> <description...> <rewards...>");
            return;
        }

        File techFile = new File(NationTech.getInstance().getDataFolder(), "technologies.yml");
        FileConfiguration techConfig = YamlConfiguration.loadConfiguration(techFile);

        String id = args[8];
        String parent = args.[9]equalsIgnoreCase("null")? null : args[9];
        String name = args[10];
        String icon = args[11];

        int descriptionEndIndex = -1;
        for (int i = 5; i < args.length; i++) {
            if (args[i].startsWith("\"") && args[i].endsWith("\"")) {
                descriptionEndIndex = i;
                break;
            }
        }

        if (descriptionEndIndex == -1) {
            sender.sendMessage("§cError: La descripción debe estar entre comillas si contiene espacios.");
            return;
        }

        List<String> description = Arrays.stream(Arrays.copyOfRange(args, 5, descriptionEndIndex + 1))
                .map(s -> s.replace("\"", "")).collect(Collectors.toList());

        List<String> rewards = Arrays.stream(Arrays.copyOfRange(args, descriptionEndIndex + 1, args.length))
                .map(s -> s.replace("\"", "")).collect(Collectors.toList());

        String path = "technologies." + id;
        techConfig.set(path + ".parent", parent);
        techConfig.set(path + ".name", name);
        techConfig.set(path + ".icon", icon);
        techConfig.set(path + ".description", description);
        techConfig.set(path + ".rewards", rewards);

        try {
            techConfig.save(techFile);
            sender.sendMessage("§aTecnología '" + id + "' añadida con éxito.");
        } catch (IOException e) {
            sender.sendMessage("§cError al guardar la tecnología.");
            e.printStackTrace();
        }
    }

    public static boolean technologyExists(String id) {
        File techFile = new File(NationTech.getInstance().getDataFolder(), "technologies.yml");
        FileConfiguration techConfig = YamlConfiguration.loadConfiguration(techFile);
        return techConfig.contains("technologies." + id);
    }
}