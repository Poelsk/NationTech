package com.github.nationTech.model;

/**
 * Representa el tipo de una tecnología en el árbol.
 * Una tecnología puede ser el final de una rama o permitir nuevas ramificaciones.
 */
public enum TechnologyType {
    /**
     * Tipo 'F': Final. Esta tecnología no puede tener hijos. Cierra una rama.
     */
    FINAL,

    /**
     * Tipo 'O': Abierta. Esta tecnología puede ser el padre de otras tecnologías.
     */
    OPEN
}