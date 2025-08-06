package com.nationtech.utils.managers;

import com.nationtech.NationTech;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessageManager {

    private final NationTech plugin;
    private final Map<String, FileConfiguration> languages;
    private String defaultLanguage;
    private FileConfiguration currentMessages;

    public MessageManager(NationTech plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
        this.defaultLanguage = plugin.getConfig().getString("general.language", "en-US");

        loadLanguages();
        setLanguage(defaultLanguage);
    }

    private void loadLanguages() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Create default language files if they don't exist
        createDefaultLanguageFiles(langFolder);

        // Load all language files
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File langFile : langFiles) {
                String langCode = langFile.getName().replace(".yml", "").replace("messages_", "");
                try {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
                    languages.put(langCode, config);
                    plugin.getLogger().info("Loaded language: " + langCode);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load language file: " + langFile.getName(), e);
                }
            }
        }

        plugin.getLogger().info("Total languages loaded: " + languages.size());
    }

    private void createDefaultLanguageFiles(File langFolder) {
        // Create English file
        createLanguageFile(langFolder, "messages_en-US.yml");
        // Create Spanish file
        createLanguageFile(langFolder, "messages_es-ES.yml");
    }

    private void createLanguageFile(File langFolder, String filename) {
        File langFile = new File(langFolder, filename);
        if (!langFile.exists()) {
            try {
                // Try to copy from plugin resources
                InputStream resourceStream = plugin.getResource(filename);
                if (resourceStream != null) {
                    Files.copy(resourceStream, langFile.toPath());
                    plugin.getLogger().info("Created language file: " + filename);
                } else {
                    // Create empty file if resource doesn't exist
                    langFile.createNewFile();
                    plugin.getLogger().warning("Created empty language file: " + filename);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create language file: " + filename, e);
            }
        }
    }

    public void setLanguage(String languageCode) {
        FileConfiguration config = languages.get(languageCode);
        if (config != null) {
            this.currentMessages = config;
            this.defaultLanguage = languageCode;
            plugin.getLogger().info("Language set to: " + languageCode);
        } else {
            // Fallback to English
            config = languages.get("en-US");
            if (config != null) {
                this.currentMessages = config;
                plugin.getLogger().warning("Language '" + languageCode + "' not found, using English fallback");
            } else {
                plugin.getLogger().severe("No language files available! Plugin may not work correctly.");
            }
        }
    }

    public String getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessageFromConfig(key);

        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        // Apply color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String getMessageFromConfig(String key) {
        if (currentMessages == null) {
            return "§cMessage system not initialized: " + key;
        }

        String message = currentMessages.getString(key);
        if (message == null) {
            // Try fallback to English if current language doesn't have the key
            FileConfiguration englishConfig = languages.get("en-US");
            if (englishConfig != null && !defaultLanguage.equals("en-US")) {
                message = englishConfig.getString(key);
            }

            // If still null, return the key itself
            if (message == null) {
                plugin.getLogger().warning("Missing message key: " + key);
                return "§cMissing message: " + key;
            }
        }

        return message;
    }

    // Convenience methods for common message types
    public void sendMessage(Player player, String key) {
        sendMessage(player, key, new HashMap<>());
    }

    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
    }

    public void sendMessage(Player player, String key, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        sendMessage(player, key, placeholders);
    }

    public String getPrefix() {
        return getMessage("plugin.prefix");
    }

    public void sendPrefixedMessage(Player player, String key) {
        sendPrefixedMessage(player, key, new HashMap<>());
    }

    public void sendPrefixedMessage(Player player, String key, Map<String, String> placeholders) {
        String prefix = getPrefix();
        String message = getMessage(key, placeholders);
        player.sendMessage(prefix + message);
    }

    // Admin message helpers
    public void sendAdminMessage(Player admin, String key, Map<String, String> placeholders) {
        sendMessage(admin, key, placeholders);
    }

    public void sendPlayerNotification(Player player, String key, Map<String, String> placeholders) {
        sendPrefixedMessage(player, key, placeholders);
    }

    // Technology-specific helpers
    public void sendTechUnlocked(Player player, String techName, int pointsSpent, int remaining) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("tech", techName);
        placeholders.put("points", String.valueOf(pointsSpent));
        placeholders.put("remaining", String.valueOf(remaining));

        sendMessage(player, "technology.unlocked", placeholders);
        sendMessage(player, "technology.spent_points", placeholders);
    }

    public void sendPointsGained(Player player, int points, int total, String context, String source) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("points", String.valueOf(points));
        placeholders.put("total", String.valueOf(total));
        placeholders.put("context", getMessage("points.context." + context));

        String messageKey = "points.gained";
        if (source != null) {
            messageKey = "points.gained_" + source;
        }

        sendMessage(player, messageKey, placeholders);
    }

    public void sendTechInfo(Player player, String techId, String techName, String description,
                             int cost, boolean unlocked) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("id", techId);
        placeholders.put("name", techName);
        placeholders.put("description", description);
        placeholders.put("cost", String.valueOf(cost));
        placeholders.put("status", unlocked ? "§aYes" : "§cNo");

        sendMessage(player, "technology.info.header");
        sendMessage(player, "technology.info.id", placeholders);
        sendMessage(player, "technology.info.name", placeholders);
        sendMessage(player, "technology.info.description", placeholders);
        sendMessage(player, "technology.info.cost", placeholders);
        sendMessage(player, "technology.info.unlocked", placeholders);
    }

    // Reload language files
    public void reloadLanguages() {
        languages.clear();
        loadLanguages();
        setLanguage(defaultLanguage);
        plugin.getLogger().info("Language files reloaded successfully!");
    }

    // Utility methods for placeholders
    public Map<String, String> createPlaceholders(String... keyValuePairs) {
        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            placeholders.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return placeholders;
    }

    // Get available languages
    public String[] getAvailableLanguages() {
        return languages.keySet().toArray(new String[0]);
    }

    // Check if a language is available
    public boolean isLanguageAvailable(String languageCode) {
        return languages.containsKey(languageCode);
    }

    // Get current language
    public String getCurrentLanguage() {
        return defaultLanguage;
    }

    // Format numbers
    public String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }

    // Color utility methods
    public String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String stripColors(String text) {
        return ChatColor.stripColor(text);
    }

    // Quick message methods with common patterns
    public void sendSuccess(Player player, String key) {
        sendMessage(player, key);
    }

    public void sendSuccess(Player player, String key, Map<String, String> placeholders) {
        sendMessage(player, key, placeholders);
    }

    public void sendError(Player player, String key) {
        sendMessage(player, key);
    }

    public void sendError(Player player, String key, Map<String, String> placeholders) {
        sendMessage(player, key, placeholders);
    }

    public void sendWarning(Player player, String key) {
        sendMessage(player, key);
    }

    public void sendWarning(Player player, String key, Map<String, String> placeholders) {
        sendMessage(player, key, placeholders);
    }
}