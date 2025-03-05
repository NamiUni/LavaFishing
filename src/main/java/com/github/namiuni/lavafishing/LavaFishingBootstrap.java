/*
 * LavaFishing
 *
 * Copyright (c) 2025. NamiUni
 *                     Contributors []
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.namiuni.lavafishing;

import com.github.namiuni.lavafishing.config.ConfigLoader;
import com.github.namiuni.lavafishing.exception.PluginConfigurationException;
import com.github.namiuni.lavafishing.util.LavaFishingPermissions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.command.CommandSender;
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
    public void bootstrap(final BootstrapContext context) {
        this.configLoader = new ConfigLoader(context.getLogger(), context.getDataDirectory());
        this.configLoader.loadConfiguration();

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final var command = this.createCommand(context.getLogger());
            event.registrar().register(command);
        });
    }

    @Override
    public JavaPlugin createPlugin(final PluginProviderContext context) {
        return new LavaFishing(this.configLoader);
    }

    private LiteralCommandNode<CommandSourceStack> createCommand(final ComponentLogger logger) {
        return Commands
                .literal("lavafishing")
                .then(Commands
                        .literal("reload")
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission(LavaFishingPermissions.COMMAND_RELOAD))
                        .executes(commandContext -> {
                            final CommandSender sender = commandContext.getSource().getSender();
                            try {
                                this.configLoader.loadConfiguration();
                                sender.sendRichMessage("<#00B06B>Configuration successfully reloaded.</#00B06B>");
                                return Command.SINGLE_SUCCESS;
                            } catch (final PluginConfigurationException exception) {
                                sender.sendRichMessage("<#FF4B00>Configuration failed to reload! See the console log for further details!!</#FF4B00>");
                                logger.error("Failed to reload config", exception);
                                return 0;
                            }
                        }))
                .build();
    }
}
