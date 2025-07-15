package com.github.nationTech.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/**
 * Gestor centralizado para el envío de mensajes usando MiniMessage.
 */
public class MessageManager {

    // PREFIJO ACTUALIZADO con el nuevo formato.
    private static final String PREFIX = "<dark_gray>[</dark_gray><b><red>NationTech</red></b><dark_gray>] </dark_gray> ";
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Envía un mensaje formateado a un jugador o a la consola.
     * @param sender El destinatario del mensaje.
     * @param message El mensaje a enviar, usando la sintaxis de MiniMessage.
     */
    public static void sendMessage(CommandSender sender, String message) {
        // Se ha corregido el formato para que el color del mensaje no se vea afectado por el gris del final del prefijo.
        Component parsedMessage = miniMessage.deserialize(
                PREFIX + "<white>" + message + "</white>"
        );
        sender.sendMessage(parsedMessage);
    }

    /**
     * Envía un mensaje sin el prefijo.
     * @param sender El destinatario del mensaje.
     * @param message El mensaje a enviar.
     */
    public static void sendRawMessage(CommandSender sender, String message) {
        Component parsedMessage = miniMessage.deserialize(message);
        sender.sendMessage(parsedMessage);
    }
}