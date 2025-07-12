package com.tunacion.nationtech.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Enum para manejar todos los mensajes de idioma del plugin.
 */
public enum Lang {
    NO_PERMISSION("messages.no_permission", "&cNo tienes permiso para hacer eso."),
    ONLY_PLAYERS("messages.only_players", "&cEste comando solo puede ser ejecutado por un jugador."),
    NO_NATION("messages.no_nation", "&cNo perteneces a ninguna nación."),
    NATION_NOT_FOUND("messages.nation_not_found", "&cLa nación '%nation%' no fue encontrada."),
    TECH_NOT_FOUND("messages.tech_not_found", "&cLa tecnología '%tech%' no fue encontrada."),
    TECH_GRANTED("messages.tech_granted", "&aTecnología '%tech%' otorgada a la nación %nation%."),
    TECH_REVOKED("messages.tech_revoked", "&cTecnología '%tech%' revocada a la nación %nation%."),
    ADMIN_USAGE("messages.admin_usage", "&cUsage: /nta <grant|revoke> <nation> <tech_id>"),
    TECH_TREE_TITLE("gui.tech_tree_title", "&1Árbol de Tecnologías"),
    TECH_UNLOCKED("gui.tech_unlocked", "&a[Desbloqueada]"),
    TECH_LOCKED("gui.tech_locked", "&c[Bloqueada]");


    private final String path;
    private String message;

    Lang(String path, String defaultMessage) {
        this.path = path;
        this.message = defaultMessage;
    }

    /**
     * Carga los mensajes desde la configuración.
     * @param config El FileConfiguration de lang.yml
     */
    public static void load(FileConfiguration config) {
        for (Lang lang : values()) {
            lang.message = ChatColor.translateAlternateColorCodes('&', config.getString(lang.path, lang.message));
        }
    }

    /**
     * Obtiene el mensaje procesado.
     * @return El mensaje con códigos de color.
     */
    public String getMessage() {
        return message;
    }
}