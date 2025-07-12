package com.tunacion.nationtech.api;

import com.palmergames.bukkit.towny.object.Nation;
import com.tunacion.nationtech.NationTech;
import com.tunacion.nationtech.data.NationProgress;
import com.tunacion.nationtech.tech.Tech;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * API pública para interactuar con el plugin NationTech.
 * Permite a otros plugins obtener información sobre el progreso de las naciones y las tecnologías.
 */
public class NationTechAPI {

    private final NationTech plugin;

    public NationTechAPI(NationTech plugin) {
        this.plugin = plugin;
    }

    /**
     * Verifica si una nación ha desbloqueado una tecnología específica.
     *
     * @param nation La nación a verificar.
     * @param techId El ID de la tecnología.
     * @return true si la tecnología está desbloqueada, false en caso contrario.
     */
    public boolean hasTech(Nation nation, String techId) {
        NationProgress progress = plugin.getDataManager().getNationProgress(nation.getUUID());
        return progress != null && progress.hasTech(techId);
    }

    /**
     * Otorga una tecnología a una nación.
     *
     * @param nation La nación a la que se le otorgará la tecnología.
     * @param techId El ID de la tecnología a otorgar.
     */
    public void grantTech(Nation nation, String techId) {
        plugin.getDataManager().grantTech(nation.getUUID(), techId);
    }

    /**
     * Obtiene el progreso de una nación.
     *
     * @param nationUUID El UUID de la nación.
     * @return El objeto NationProgress o null si no existe.
     */
    public NationProgress getNationProgress(UUID nationUUID) {
        return plugin.getDataManager().getNationProgress(nationUUID);
    }

    /**
     * Obtiene todas las tecnologías registradas.
     *
     * @return Un conjunto de todas las tecnologías.
     */
    public Set<Tech> getAllTechs() {
        // CORRECCIÓN: Se cambió getAllTech() por getAllTechs() para que coincida con el método en TechManager.
        return new HashSet<>(plugin.getTechManager().getAllTechs());
    }
}