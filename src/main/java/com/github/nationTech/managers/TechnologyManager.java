package com.github.nationTech.managers;

import com.github.nationTech.NationTech;
import com.github.nationTech.model.Technology;
import com.github.nationTech.model.TechnologyType;
import com.github.nationTech.requirements.Requirement;
import com.github.nationTech.utils.MessageManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TechnologyManager {

    private final NationTech plugin;
    public static final String OFFICIAL_TREE_ID = "oficial";
    private final Map<String, Map<String, Technology>> techTrees = new ConcurrentHashMap<>();

    public TechnologyManager(NationTech plugin) {
        this.plugin = plugin;
    }

    public void loadAllTrees() {
        plugin.getDatabaseManager().loadAllTechnologies().thenAccept(technologies -> {
            techTrees.clear();
            Map<String, List<Technology>> technologiesByTree = technologies.stream()
                    .collect(Collectors.groupingBy(Technology::getTreeId));

            technologiesByTree.forEach((treeId, techList) -> {
                Map<String, Technology> treeMap = new ConcurrentHashMap<>();
                for (Technology tech : techList) {
                    treeMap.put(tech.getId(), tech);
                }
                techTrees.put(treeId, treeMap);
            });

            techTrees.computeIfAbsent(OFFICIAL_TREE_ID, k -> new ConcurrentHashMap<>());
            plugin.getLogger().info(techTrees.size() + " árbol(es) tecnológico(s) cargado(s) en la caché.");
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error crítico al cargar tecnologías de la base de datos.", ex);
            return null;
        });
    }

    public boolean createDraft(String draftName) {
        if (techTrees.containsKey(draftName)) {
            return false;
        }
        techTrees.put(draftName, new ConcurrentHashMap<>());
        return true;
    }

    public Set<String> getTreeNames() {
        return techTrees.keySet();
    }

    public Map<String, Technology> getTechnologyTree(String treeId) {
        return Collections.unmodifiableMap(techTrees.getOrDefault(treeId, Collections.emptyMap()));
    }

    public void createTechnology(String treeId, String id, int row, int column, String nombre, String padreId, TechnologyType tipo, String requisitos, String icono, String recompensa) {
        Map<String, Technology> tree = techTrees.computeIfAbsent(treeId, k -> new ConcurrentHashMap<>());
        if (tree.containsKey(id)) {
            return;
        }

        Technology newTech = new Technology(treeId, id, row, column, nombre, padreId, tipo, requisitos, icono, recompensa);
        plugin.getDatabaseManager().saveTechnology(newTech);
        tree.put(id, newTech);
    }

    // --- CORRECCIÓN AQUÍ: Se devuelve un CompletableFuture<Boolean> ---
    public CompletableFuture<Boolean> deleteTechnology(String treeId, String techId) {
        Map<String, Technology> tree = techTrees.get(treeId);
        if (tree == null || !tree.containsKey(techId)) {
            plugin.getLogger().warning("Se intentó eliminar una tecnología que no existe: " + techId + " en el árbol " + treeId);
            return CompletableFuture.completedFuture(false);
        }

        // Primero, removemos de la caché.
        tree.remove(techId);

        // Llamamos a la operación asíncrona de la base de datos y devolvemos su futuro.
        return plugin.getDatabaseManager().deleteTechnology(treeId, techId).thenApply(v -> {
            plugin.getLogger().info("Tecnología " + techId + " eliminada del árbol " + treeId + " con éxito.");
            return true;
        });
    }

    public void attemptUnlockTechnology(Player player, Nation nation, Technology tech) {
        if (!tech.getTreeId().equals(OFFICIAL_TREE_ID)) {
            MessageManager.sendMessage(player, "<red>Solo se pueden desbloquear tecnologías del árbol oficial.");
            return;
        }

        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
        if (resident == null || !nation.isKing(resident)) {
            MessageManager.sendMessage(player, "<red>Solo el líder de la nación puede desbloquear tecnologías.");
            return;
        }

        plugin.getDatabaseManager().getNationUnlockedTechs(nation.getUUID()).thenAccept(unlockedTechs -> {
            if (unlockedTechs.contains(tech.getId())) {
                MessageManager.sendMessage(player, "<yellow>Tu nación ya ha desbloqueado esta tecnología.");
                return;
            }

            if (tech.getPadreId() != null && !unlockedTechs.contains(tech.getPadreId())) {
                MessageManager.sendMessage(player, "<red>Debes desbloquear la tecnología anterior primero.");
                return;
            }

            List<Requirement> requirements = tech.getParsedRequirements();
            for (Requirement req : requirements) {
                if (!req.check(player)) {
                    MessageManager.sendMessage(player, "<red>No cumples con todos los requisitos. Necesitas: " + req.getLoreText());
                    return;
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Requirement req : requirements) {
                    req.consume(player);
                }

                plugin.getDatabaseManager().addUnlockedTechnology(nation.getUUID(), tech.getId());

                String rawReward = tech.getRecompensa();
                String commandToExecute = rawReward;
                if (rawReward.contains("|")) {
                    String[] parts = rawReward.split("\\|", 2);
                    if (parts.length == 2) {
                        commandToExecute = parts[1];
                    }
                }
                commandToExecute = commandToExecute.replace("%player%", player.getName()).replace("%nation%", nation.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);

                MessageManager.sendMessage(player, "<green>¡Tu nación ha desbloqueado la tecnología <white>"+tech.getNombre()+"</white>!");

                plugin.getGuiManager().openNationTechGUI(player, nation, OFFICIAL_TREE_ID);
            });
        });
    }
}