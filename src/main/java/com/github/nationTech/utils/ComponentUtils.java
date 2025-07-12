package com.github.nationTech.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
// --- CORRECCIÓN AQUÍ ---
// Importamos el serializador de texto plano correcto.
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Clase de utilidad para manejar la creación de componentes de texto con MiniMessage.
 */
public class ComponentUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Parsea un string con formato MiniMessage a un Component.
     * @param text El texto a parsear.
     * @return El Component formateado.
     */
    public static Component parse(String text) {
        if (text == null) return Component.empty();
        return miniMessage.deserialize(text);
    }

    /**
     * Convierte un Component a un string de texto plano (sin colores).
     * @param component El component a convertir.
     * @return El texto plano.
     */
    public static String toPlainText(Component component) {
        // --- CORRECCIÓN AQUÍ ---
        // Usamos el serializador correcto para obtener el texto plano.
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}