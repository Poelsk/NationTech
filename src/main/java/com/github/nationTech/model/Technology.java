package com.github.nationTech.model;

import com.github.nationTech.requirements.Requirement;
import com.github.nationTech.requirements.RequirementParser;

import java.util.List;

public final class Technology {

    private final String id;
    private final String treeId;
    private final String nombre;
    private final int row;
    private final int column;
    private final String padreId;
    private final TechnologyType tipo;
    private final String requisitos;
    private final String icono;
    private final String recompensa;
    private transient List<Requirement> parsedRequirements;

    public Technology(String treeId, String id, int row, int column, String nombre, String padreId, TechnologyType tipo, String requisitos, String icono, String recompensa) {
        this.treeId = treeId;
        this.id = id;
        this.row = row;
        this.column = column;
        this.nombre = nombre;
        this.padreId = padreId;
        this.tipo = tipo;
        this.requisitos = requisitos;
        this.icono = icono;
        this.recompensa = recompensa;
    }

    public String getId() { return id; }
    public String getTreeId() { return treeId; }
    public String getNombre() { return nombre; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public String getPadreId() { return padreId; }
    public TechnologyType getTipo() { return tipo; }
    public String getRequisitos() { return requisitos; }
    public String getIcono() { return icono; }
    public String getRecompensa() { return recompensa; }

    public int getSlot() {
        return this.column + (this.row * 9);
    }

    public List<Requirement> getParsedRequirements() {
        if (this.parsedRequirements == null) {
            this.parsedRequirements = RequirementParser.parse(this.requisitos);
        }
        return this.parsedRequirements;
    }
}