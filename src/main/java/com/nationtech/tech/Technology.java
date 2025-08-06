// En tu archivo com/nationtech/tech/Technology.java

package com.nationtech.tech;

import java.util.List;
import java.util.ArrayList;

public class Technology {

    private String id;
    private String name;
    private String description;
    private int cost;
    private String icon;
    private List<String> prerequisites;
    private List<String> effects;

    public Technology(String id, String name, String description, int cost, String icon, List<String> prerequisites, List<String> effects) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.icon = (icon == null || icon.isEmpty()) ? "STONE" : icon; // Usamos STONE como ícono por defecto
        this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>();
        this.effects = effects != null ? new ArrayList<>(effects) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    // 3. AÑADE ESTE MÉTODO
    public String getIcon() {
        return icon;
    }

    public List<String> getPrerequisites() {
        return new ArrayList<>(prerequisites);
    }

    public List<String> getEffects() {
        return new ArrayList<>(effects);
    }

    @Override
    public String toString() {
        return "Technology{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", cost=" + cost +
                ", prerequisites=" + prerequisites +
                '}';
    }
}