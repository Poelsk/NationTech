package com.github.nationTech.requirements;

import org.bukkit.entity.Player;

/**
 * Interfaz que representa un requisito genérico para desbloquear una tecnología.
 */
public interface Requirement {

    /**
     * Verifica si el jugador cumple con este requisito.
     * No consume los ítems/recursos, solo comprueba si los tiene.
     * @param player El jugador a verificar.
     * @return true si el jugador cumple el requisito, false si no.
     */
    boolean check(Player player);

    /**
     * Consume los ítems/recursos necesarios del inventario del jugador.
     * Este método solo debe llamarse después de que check() haya devuelto true.
     * @param player El jugador del que se consumirán los recursos.
     */
    void consume(Player player);

    /**
     * Devuelve una descripción legible del requisito para mostrar en la GUI.
     * @return Un String que describe el requisito.
     */
    String getLoreText();
}