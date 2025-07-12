package com.github.nationTech.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.github.nationTech.NationTech;
import com.github.nationTech.managers.TechnologyManager;
import com.github.nationTech.model.TechnologyType;
import com.github.nationTech.utils.MessageManager;
import com.palmergames.bukkit.towny.object.Nation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("ntca")
@CommandPermission("nationtech.admin")
@Description("Comandos de administración de NationTech")
public class NationTechAdminCommand extends BaseCommand {

    private final TechnologyManager techManager = NationTech.getInstance().getTechnologyManager();

    @Subcommand("addtech")
    @Syntax("[nombre_arbol] <id> <fila> <col> <nombre_con_guiones> <padreId|null> <F|O> <requisitos> <icono> <beneficio_con_guiones...>")
    @CommandCompletion("@treenames @nothing @nothing @nothing @nothing @tech_ids_and_null F|O item:|money:|mmoitem: @materials @nothing")
    @Description("Crea o modifica una tecnología.")
    public void onAddTech(CommandSender sender, @Flags("tree") @Optional @Default("oficial") String treeId, String id, int row, int col, String nombre, String padreId, String tipo, String requisitos, String icono, String[] beneficioArray) {

        // Se reemplazan los guiones bajos por espacios para permitir nombres y beneficios complejos.
        String finalNombre = nombre.replace('_', ' ');
        String beneficio = String.join(" ", beneficioArray).replace('_', ' ');

        if (techManager.getTechnologyTree(treeId).containsKey(id)) {
            MessageManager.sendMessage(sender, "<red>Ya existe una tecnología con ese ID en el árbol '" + treeId + "'.");
            return;
        }

        TechnologyType techType;
        if (tipo.equalsIgnoreCase("F")) {
            techType = TechnologyType.FINAL;
        } else if (tipo.equalsIgnoreCase("O")) {
            techType = TechnologyType.OPEN;
        } else {
            MessageManager.sendMessage(sender, "<red>Tipo inválido. Usa 'F' para Final o 'O' para Abierta.");
            return;
        }

        String finalPadreId = padreId.equalsIgnoreCase("null") ? null : padreId;

        techManager.createTechnology(treeId, id, row, col, finalNombre, finalPadreId, techType, requisitos, icono, beneficio);
        MessageManager.sendMessage(sender, "<green>Tecnología '" + finalNombre + "' creada en el árbol '" + treeId + "'.");
    }

    @Subcommand("removetech")
    @Syntax("<id_tecnologia> [nombre_arbol]")
    @CommandCompletion("@technologies @treenames")
    @Description("Elimina una tecnología.")
    public void onRemoveTech(CommandSender sender, String techId, @Optional @Default("oficial") String treeId) {
        techManager.deleteTechnology(treeId, techId).thenAccept(success -> {
            if (success) {
                MessageManager.sendMessage(sender, "<green>Tecnología eliminada.");
            } else {
                MessageManager.sendMessage(sender, "<red>No se pudo encontrar esa tecnología en ese árbol.");
            }
        });
    }

    @Subcommand("give")
    @Syntax("<id_tecnologia> <nacion>")
    @CommandCompletion("@technologies @nations")
    @Description("Otorga una tecnología a una nación.")
    public void onGiveTech(CommandSender sender, String techId, Nation nation) {
        NationTech.getInstance().getDatabaseManager().addUnlockedTechnology(nation.getUUID(), techId);
        MessageManager.sendMessage(sender, "<green>Tecnología otorgada.");
    }

    @Subcommand("quit")
    @Syntax("<id_tecnologia> <nacion>")
    @CommandCompletion("@technologies @nations")
    @Description("Quita una tecnología a una nación.")
    public void onQuitTech(CommandSender sender, String techId, Nation nation) {
        NationTech.getInstance().getDatabaseManager().removeUnlockedTechnology(nation.getUUID(), techId);
        MessageManager.sendMessage(sender, "<green>Tecnología quitada.");
    }

    @Subcommand("create")
    @Syntax("<nombreBorrador>")
    @Description("Crea un nuevo borrador de árbol tecnológico.")
    public void onCreateDraft(CommandSender sender, String draftName) {
        if (techManager.createDraft(draftName)) {
            MessageManager.sendMessage(sender, "<green>Borrador '" + draftName + "' creado.");
        } else {
            MessageManager.sendMessage(sender, "<red>Ese borrador ya existe.");
        }
    }

    @Subcommand("listtest")
    @Description("Muestra la lista de árboles tecnológicos para editar.")
    public void onListDrafts(Player sender) {
        NationTech.getInstance().getGuiManager().openDraftSelectionGUI(sender);
    }

    @Subcommand("reload")
    @Description("Recarga la configuración y tecnologías.")
    public void onReload(CommandSender sender) {
        NationTech.getInstance().reloadConfig();
        techManager.loadAllTrees();
        MessageManager.sendMessage(sender, "<green>NationTech recargado.");
    }

    @Subcommand("setpack")
    @Syntax("<url>")
    @Description("Establece la URL del paquete de recursos.")
    public void onSetPack(CommandSender sender, String url) {
        NationTech.getInstance().getConfig().set("resource-pack-url", url);
        NationTech.getInstance().saveConfig();
        MessageManager.sendMessage(sender, "<green>Paquete de recursos establecido.");
    }
}