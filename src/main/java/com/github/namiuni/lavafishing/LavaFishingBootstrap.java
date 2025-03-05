package com.github.namiuni.lavafishing;

import com.github.namiuni.lavafishing.config.ConfigLoader;
import com.github.namiuni.lavafishing.util.LavaFishingPermissions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class LavaFishingBootstrap implements PluginBootstrap {

    private @MonotonicNonNull ConfigLoader configLoader;

    @Override
    public void bootstrap(final BootstrapContext bootstrapContext) {
        this.configLoader = new ConfigLoader(bootstrapContext.getLogger(), bootstrapContext.getDataDirectory());
        this.configLoader.primaryConfig(); // If not executed here, the configurations will be generated when fishing.
        bootstrapContext.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::registerCommands);
    }

    @Override
    public JavaPlugin createPlugin(final PluginProviderContext context) {
        return new LavaFishing(this.configLoader);
    }

    private void registerCommands(final ReloadableRegistrarEvent<Commands> event) {
        final LiteralCommandNode<CommandSourceStack> command = Commands
                .literal("lavafishing")
                .then(Commands
                        .literal("reload")
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission(LavaFishingPermissions.COMMAND_RELOAD))
                        .executes(commandContext -> {
                            this.configLoader.reloadPrimaryConfig();
                            commandContext.getSource().getSender().sendRichMessage("Config reload is complete!");
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
        event.registrar().register(command);
    }
}
