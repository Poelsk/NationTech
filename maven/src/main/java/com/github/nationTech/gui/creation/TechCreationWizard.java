package com.github.nationTech.gui.creation;

import com.github.nationTech.NationTech;
import com.github.nationTech.utils.ColorUtils;
import com.github.nationTech.utils.TechUtils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class TechCreationWizard {

    private final Player player;
    private final TechnologyBuilder builder;
    private final NationTech plugin;

    public TechCreationWizard(Player player, NationTech plugin) {
        this.player = player;
        this.plugin = plugin;
        this.builder = new TechnologyBuilder();
    }

    public void start() {
        askForId();
    }

    private void askForId() {
        new AnvilGUI.Builder()
                .onComplete((completion) -> {
                    String input = completion.getText().trim();
                    if (input.isEmpty() |

                            | input.contains(" ") |

                            | TechUtils.technologyExists(input)) {
                        player.sendMessage(ColorUtils.format("<red>ID inválido, vacío, con espacios o ya existente."));
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("Inténtalo de nuevo"));
                    }
                    builder.id = input;
                    askForName();
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text("ID único para la tecnología")
                .title("Paso 1: ID de la Tecnología")
                .plugin(plugin)
                .open(player);
    }

    private void askForName() {
        new AnvilGUI.Builder()
                .onComplete((completion) -> {
                    builder.name = completion.getText();
                    askForDescription();
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text("Nombre (ej: <green>Agricultura)")
                .title("Paso 2: Nombre (MiniMessage)")
                .plugin(plugin)
                .open(player);
    }

    private void askForDescription() {
        new AnvilGUI.Builder()
                .onComplete((completion) -> {
                    builder.description = Arrays.asList(completion.getText().split("\\|"));
                    askForIcon();
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text("Línea 1|Línea 2|...")
                .title("Paso 3: Descripción (usar |)")
                .plugin(plugin)
                .open(player);
    }

    private void askForIcon() {
        new AnvilGUI.Builder()
                .onComplete((completion) -> {
                    Material material = Material.matchMaterial(completion.getText().toUpperCase());
                    if (material == null) {
                        player.sendMessage(ColorUtils.format("<red>Material no válido."));
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("Inténtalo de nuevo"));
                    }
                    builder.icon = material;
                    askForParent();
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text("ej: STONE, DIAMOND_SWORD")
                .title("Paso 4: Icono (Material)")
                .plugin(plugin)
                .open(player);
    }

    private void askForParent() {
        new AnvilGUI.Builder()
                .onComplete((completion) -> {
                    String input = completion.getText().trim();
                    if (!input.equalsIgnoreCase("null") &&!TechUtils.technologyExists(input)) {
                        player.sendMessage(ColorUtils.format("<red>La tecnología padre no existe."));
                        return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("Inténtalo de nuevo"));
                    }
                    builder.parentId = input.equalsIgnoreCase("null")? null : input;
                    askForRewards();
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text("ID de la tecnología padre o 'null'")
                .title("Paso 5: Dependencia")
                .plugin(plugin)
                .open(player);
    }

    private void askForRewards() {
        new AnvilGUI.Builder()
                .onComplete((completion) -> {
                    builder.rewards = Arrays.asList(completion.getText().split(";"));
                    finish();
                    return Collections.singletonList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text("comando1;comando2;...")
                .title("Paso 6: Recompensas (usar ;)")
                .plugin(plugin)
                .open(player);
    }

    private void finish() {
        if (!builder.isComplete()) {
            player.sendMessage(ColorUtils.format("<red>Error: No se completaron todos los pasos."));
            return;
        }

        String parentIdArg = builder.parentId!= null? builder.parentId : "null";
        String descriptionArgs = builder.description.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(" "));
        String rewardsArg = String.join(";", builder.rewards);

        String commandToDispatch = String.format("ntca addtech %s %s \"%s\" %s %s \"%s\"",
                builder.id,
                parentIdArg,
                builder.name,
                builder.icon.name(),
                descriptionArgs,
                rewardsArg
        );

        player.sendMessage(ColorUtils.format("<green>Ejecutando comando..."));

        // Ejecutar el comando desde la consola para asegurar permisos y consistencia
        CommandSender console = plugin.getServer().getConsoleSender();
        plugin.getServer().dispatchCommand(console, commandToDispatch);

        player.sendMessage(ColorUtils.format("<gold>¡Tecnología creada! Recargando árbol de tecnologías..."));

        // Recargar el árbol de logros para que se muestre inmediatamente
        plugin.getAdvancementTreeManager().reloadTechnologies();
    }
}