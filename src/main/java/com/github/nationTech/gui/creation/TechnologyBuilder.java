package com.github.nationTech.gui.creation;

import org.bukkit.Material;
import java.util.List;

public class TechnologyBuilder {
    String id;
    String name;
    List<String> description;
    Material icon;
    String parentId;
    List<String> rewards;

    public boolean isComplete() {
        return id!= null && name!= null && description!= null && icon!= null && rewards!= null;
    }
}