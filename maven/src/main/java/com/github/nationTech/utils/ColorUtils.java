package com.github.nationTech.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public final class ColorUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.gradient())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.reset())
                    .build())
            .build();

    private ColorUtils() {
        // Clase de utilidad, no instanciable
    }

    /**
     * Parsea una cadena con formato MiniMessage a un Component de Adventure.
     * @param text La cadena a formatear.
     * @return Un Component formateado, o un Component vacío si la entrada es nula/vacía.
     */
    public static Component format(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

    /**
     * Elimina todas las etiquetas de formato MiniMessage de una cadena.
     * @param text La cadena con formato.
     * @return La cadena en texto plano.
     */
    public static String strip(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return MINI_MESSAGE.stripTags(text);
    }
}