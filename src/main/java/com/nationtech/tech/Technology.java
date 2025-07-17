package com.nationtech.tech;

import java.util.List;
import java.util.ArrayList;

public class Technology {

    private String id;
    private String name;
    private String description;
    private int cost;
    private List<String> prerequisites;
    private List<String> effects;

    public Technology(String id, String name, String description, int cost, List<String> prerequisites, List<String> effects) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
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