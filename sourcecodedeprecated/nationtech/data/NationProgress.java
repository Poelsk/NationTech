package com.tunacion.nationtech.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Representa el progreso de una nación en el árbol de tecnologías.
 */
public class NationProgress {

    private final UUID nationUUID;
    private final Set<String> unlockedTechs;

    public NationProgress(UUID nationUUID) {
        this.nationUUID = nationUUID;
        this.unlockedTechs = new HashSet<>();
    }

    public UUID getNationUUID() {
        return nationUUID;
    }

    public Set<String> getUnlockedTechs() {
        return unlockedTechs;
    }

    public boolean hasTech(String techId) {
        return unlockedTechs.contains(techId.toLowerCase());
    }

    public void addTech(String techId) {
        unlockedTechs.add(techId.toLowerCase());
    }

    public void removeTech(String techId) {
        unlockedTechs.remove(techId.toLowerCase());
    }
}