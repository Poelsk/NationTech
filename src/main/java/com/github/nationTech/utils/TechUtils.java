package com.github.nationTech.utils;

import com.github.nationTech.NationTech;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TechUtils {

    /**
     * Añade una nueva tecnología al archivo technologies.yml.
     * Este método ha sido actualizado para ser más robusto y corregir errores lógicos.
     *
     * @param sender El emisor del comando.
     * @param args   Los argumentos proporcionados al comando. El formato esperado es:
     * <id> <parent> <name> <icon> "<description>" <reward1> <reward2> ...
     */
    public static void addTech(CommandSender sender, String[] args) {
        // --- CORRECCIÓN DE LÓGICA ---
        // Se necesitan al menos 5 argumentos base: id, parent, name, icon, y description.
        // Las recompensas son opcionales.
        if (args.length < 5) {
            sender.sendMessage("§cUsage: /ntca addtech <id> <parent> <name> <icon> \"<description>\" [rewards...]");
            return;
        }

        File techFile = new File(NationTech.getInstance().getDataFolder(), "technologies.yml");
        FileConfiguration techConfig = YamlConfiguration.loadConfiguration(techFile);

        // --- CORRECCIÓN DE ÍNDICES ---
        // Los argumentos de un comando empiezan en el índice 0.
        String id = args[0];

        // --- CORRECCIÓN DE SINTAXIS Y LÓGICA ---
        // Usamos el índice 1 para el padre y corregimos el acceso al array.
        String parent = args[1].equalsIgnoreCase("null") ? null : args[1];

        String name = args[2];
        String icon = args[3];

        // --- LÓGICA MEJORADA PARA DESCRIPCIÓN Y RECOMPENSAS ---
        // El argumento de la descripción (índice 4) debe venir entre comillas.
        String description = args[4];

        // Recogemos todas las recompensas, que son los argumentos restantes a partir del índice 5.
        List<String> rewards = new ArrayList<>();
        if (args.length > 5) {
            // Arrays.copyOfRange crea una copia del array desde el índice 5 hasta el final.
            rewards.addAll(Arrays.asList(Arrays.copyOfRange(args, 5, args.length)));
        }

        // Creamos la ruta en el archivo de configuración.
        String path = "technologies." + id;

        // Comprobamos si la tecnología ya existe para evitar sobrescribirla accidentalmente.
        if (techConfig.contains(path)) {
            sender.sendMessage("§cError: La tecnología con ID '" + id + "' ya existe.");
            return;
        }

        techConfig.set(path + ".parent", parent);
        techConfig.set(path + ".name", name);
        techConfig.set(path + ".icon", icon);
        techConfig.set(path + ".description", description);
        techConfig.set(path + ".rewards", rewards);

        try {
            techConfig.save(techFile);
            sender.sendMessage("§aTecnología '" + id + "' añadida con éxito.");
        } catch (IOException e) {
            sender.sendMessage("§cError: No se pudo guardar la tecnología en el archivo.");
            e.printStackTrace();
        }
    }

    /**
     * Comprueba si una tecnología con un ID específico ya existe en el archivo de configuración.
     *
     * @param id El ID de la tecnología a comprobar.
     * @return true si la tecnología existe, false en caso contrario.
     */
    public static boolean technologyExists(String id) {
        File techFile = new File(NationTech.getInstance().getDataFolder(), "technologies.yml");
        // Es importante no dejar el archivo en memoria. Es mejor cargarlo cada vez
        // para asegurar que tenemos la versión más reciente.
        FileConfiguration techConfig = YamlConfiguration.loadConfiguration(techFile);
        return techConfig.contains("technologies." + id);
    }
}