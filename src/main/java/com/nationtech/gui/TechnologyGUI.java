package com.nationtech.gui;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.nationtech.NationTech;
import com.nationtech.data.NationTechnologyData;
import com.nationtech.tech.Technology;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class TechnologyGUI {

    private final NationTech plugin;
    private final UltimateAdvancementAPI api;
    private AdvancementTab techTab;
    private final Map<String, Advancement> technologyAdvancements;
    private boolean initialized = false;

    public TechnologyGUI(NationTech plugin, UltimateAdvancementAPI api) {
        this.plugin = plugin;
        this.api = api;
        this.technologyAdvancements = new HashMap<>();

        initializeTechnologyTree();
    }

    private void initializeTechnologyTree() {
        try {
            // Create the technology tab with proper background
            ItemStack background = new ItemStack(Material.STONE);
            this.techTab = api.createAdvancementTab("nationtech_tech", background);

            Map<String, Technology> technologies = plugin.getTechnologyManager().getAllTechnologies();

            if (technologies.isEmpty()) {
                plugin.getLogger().warning("No technologies found to create advancement tree!");
                return;
            }

            plugin.getLogger().info("Creating advancement tree with " + technologies.size() + " technologies");

            // First, create all root technologies (no prerequisites)
            List<Technology> rootTechs = new ArrayList<>();
            for (Technology tech : technologies.values()) {
                if (tech.getPrerequisites().isEmpty()) {
                    rootTechs.add(tech);
                }
            }

            if (rootTechs.isEmpty()) {
                plugin.getLogger().warning("No root technologies found! Creating fallback root.");
                // Create a fallback if no root technologies exist
                createFallbackRoot();
                return;
            }

            // Create root advancements
            for (int i = 0; i < rootTechs.size(); i++) {
                Technology tech = rootTechs.get(i);
                createRootAdvancement(tech, i);
            }

            // Create dependent technologies in waves to handle complex dependencies
            createDependentTechnologies(technologies);

            this.initialized = true;
            plugin.getLogger().info("Technology advancement tree created successfully!");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize technology tree: " + e.getMessage(), e);
            this.initialized = false;
        }
    }

    private void createFallbackRoot() {
        try {
            ItemStack icon = new ItemStack(Material.BOOK);
            AdvancementDisplay display = new AdvancementDisplay(
                    icon,
                    "Technology Tree",
                    "Welcome to the NationTech system!",
                    AdvancementFrameType.TASK,
                    true,
                    false,
                    0.0f,
                    0.0f
            );

            RootAdvancement fallbackRoot = new RootAdvancement(
                    techTab,
                    "nationtech_root",
                    display,
                    "textures/block/stone.png",
                    player -> true // Always visible
            );

            techTab.registerAdvancement(fallbackRoot);
            technologyAdvancements.put("nationtech_root", fallbackRoot);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create fallback root: " + e.getMessage(), e);
        }
    }

    private void createRootAdvancement(Technology tech, int index) {
        try {
            ItemStack icon = createIcon(tech);

            AdvancementDisplay display = new AdvancementDisplay(
                    icon,
                    tech.getName(),
                    tech.getDescription() + "\n§7Cost: " + tech.getCost() + " points",
                    getFrameType(tech),
                    true, // Show toast
                    false, // Not hidden
                    index * 2.0f, // Spread roots horizontally
                    0.0f
            );

            RootAdvancement rootAdv = new RootAdvancement(
                    techTab,
                    tech.getId(),
                    display,
                    "textures/block/stone.png",
                    player -> hasUnlockedTechnology(player, tech.getId())
            );

            techTab.registerAdvancement(rootAdv);
            technologyAdvancements.put(tech.getId(), rootAdv);

            plugin.getLogger().info("Created root advancement: " + tech.getName());

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create root advancement for " + tech.getId() + ": " + e.getMessage(), e);
        }
    }

    private void createDependentTechnologies(Map<String, Technology> technologies) {
        Set<String> processed = new HashSet<>(technologyAdvancements.keySet());
        Set<String> toProcess = new HashSet<>();

        // Find all technologies that aren't roots
        for (Technology tech : technologies.values()) {
            if (!tech.getPrerequisites().isEmpty() && !processed.contains(tech.getId())) {
                toProcess.add(tech.getId());
            }
        }

        // Process in waves until all are done or we can't make progress
        int maxIterations = 10;
        int iteration = 0;

        while (!toProcess.isEmpty() && iteration < maxIterations) {
            Set<String> processedThisRound = new HashSet<>();

            for (String techId : new HashSet<>(toProcess)) {
                Technology tech = technologies.get(techId);
                if (tech == null) continue;

                // Check if all prerequisites are already created
                boolean canCreate = true;
                Advancement parent = null;

                for (String prereqId : tech.getPrerequisites()) {
                    if (!technologyAdvancements.containsKey(prereqId)) {
                        canCreate = false;
                        break;
                    }
                    if (parent == null) {
                        parent = technologyAdvancements.get(prereqId);
                    }
                }

                if (canCreate && parent != null) {
                    createChildAdvancement(tech, parent);
                    processedThisRound.add(techId);
                    processed.add(techId);
                }
            }

            toProcess.removeAll(processedThisRound);
            iteration++;

            if (processedThisRound.isEmpty()) {
                // No progress made, break to avoid infinite loop
                break;
            }
        }

        if (!toProcess.isEmpty()) {
            plugin.getLogger().warning("Could not create advancements for technologies: " + toProcess);
        }
    }

    private void createChildAdvancement(Technology tech, Advancement parent) {
        try {
            ItemStack icon = createIcon(tech);

            AdvancementDisplay display = new AdvancementDisplay(
                    icon,
                    tech.getName(),
                    tech.getDescription() + "\n§7Cost: " + tech.getCost() + " points",
                    getFrameType(tech),
                    true, // Show toast
                    false, // Not hidden
                    0.0f, // Let API calculate position
                    0.0f
            );

            BaseAdvancement childAdv = new BaseAdvancement(
                    techTab,
                    tech.getId(),
                    display,
                    parent,
                    player -> hasUnlockedTechnology(player, tech.getId())
            );

            techTab.registerAdvancement(childAdv);
            technologyAdvancements.put(tech.getId(), childAdv);

            plugin.getLogger().info("Created child advancement: " + tech.getName());

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create child advancement for " + tech.getId() + ": " + e.getMessage(), e);
        }
    }

    private ItemStack createIcon(Technology tech) {
        Material material = parseMaterial(tech.getIcon());
        return new ItemStack(material);
    }

    private Material parseMaterial(String materialString) {
        if (materialString == null || materialString.isEmpty()) {
            return Material.STONE;
        }

        try {
            return Material.valueOf(materialString.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialString + ", using STONE as default");
            return Material.STONE;
        }
    }

    private AdvancementFrameType getFrameType(Technology tech) {
        // You can customize this based on technology properties
        if (tech.getCost() >= 300) {
            return AdvancementFrameType.CHALLENGE;
        } else if (tech.getCost() >= 150) {
            return AdvancementFrameType.GOAL;
        } else {
            return AdvancementFrameType.TASK;
        }
    }

    private boolean hasUnlockedTechnology(Player player, String techId) {
        try {
            UUID targetUUID = getTargetUUID(player);
            NationTechnologyData data = plugin.getNationDataManager().getNationData(targetUUID);
            return data.hasUnlockedTechnology(techId);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking technology unlock status for " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    private UUID getTargetUUID(Player player) {
        UUID targetUUID = player.getUniqueId();

        if (plugin.isTownyIntegrationEnabled() && plugin.getConfig().getBoolean("towny.use_nation_points", true)) {
            try {
                var nationUUID = plugin.getTownyHandler().getNationUUIDFromPlayer(player);
                if (nationUUID.isPresent()) {
                    targetUUID = nationUUID.get();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error getting nation UUID for " + player.getName() + ": " + e.getMessage());
            }
        }

        return targetUUID;
    }

    public void showTechnologyTree(Player player) {
        if (!initialized) {
            player.sendMessage("§cTechnology GUI is not properly initialized. Please contact an administrator.");
            plugin.getLogger().warning("Attempt to show technology tree but GUI is not initialized");
            return;
        }

        if (techTab == null) {
            player.sendMessage("§cTechnology tree is not available. Please contact an administrator.");
            return;
        }

        try {
            // Update player progress first
            updatePlayerProgress(player);

            // Show the tab
            api.showTab(player, techTab);
            player.sendMessage("§8[§6NationTech§8] §aTechnology tree opened!");

        } catch (Exception e) {
            player.sendMessage("§cFailed to open technology tree: " + e.getMessage());
            plugin.getLogger().log(Level.SEVERE, "Error showing technology tree for " + player.getName() + ": " + e.getMessage(), e);
        }
    }

    public void updatePlayerProgress(Player player) {
        if (!initialized || techTab == null) {
            return;
        }

        try {
            // Update all advancement criteria for the player
            for (Map.Entry<String, Advancement> entry : technologyAdvancements.entrySet()) {
                try {
                    Advancement advancement = entry.getValue();
                    boolean shouldBeGranted = hasUnlockedTechnology(player, entry.getKey());

                    if (shouldBeGranted && !advancement.isGranted(player)) {
                        advancement.grant(player, false); // Grant without showing toast to avoid spam
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error updating advancement " + entry.getKey() + " for " + player.getName() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error updating player progress for " + player.getName() + ": " + e.getMessage(), e);
        }
    }

    public AdvancementTab getTechnologyTab() {
        return techTab;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void cleanup() {
        try {
            if (techTab != null) {
                // Cleanup advancement tab if needed
                technologyAdvancements.clear();
                plugin.getLogger().info("Technology GUI cleanup completed");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error during GUI cleanup: " + e.getMessage(), e);
        }
    }

    // Method to reinitialize the GUI (useful for reloads)
    public void reinitialize() {
        try {
            cleanup();
            this.initialized = false;
            this.technologyAdvancements.clear();
            initializeTechnologyTree();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error reinitializing technology GUI: " + e.getMessage(), e);
        }
    }
}