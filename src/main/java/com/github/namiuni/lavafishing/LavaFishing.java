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
import com.github.namiuni.lavafishing.fishing.LavaHookListener;
import com.github.namiuni.lavafishing.fishing.LavaHookHandler;
import com.github.namiuni.lavafishing.util.PluginProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
public final class LavaFishing extends JavaPlugin {

    private final LavaHookHandler lavaHookHandler;
    private final LavaHookListener lavaHookListener;

    LavaFishing(final ConfigLoader configLoader) {
        this.lavaHookHandler = new LavaHookHandler(this.getComponentLogger(), configLoader, this::runAtFixedRate);
        this.lavaHookListener = new LavaHookListener(configLoader, this.lavaHookHandler);
    }

    @Override
    public void onLoad() {
        PluginProvider.register(this);
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this.lavaHookListener, this);
    }

    @Override
    public void onDisable() {
        this.lavaHookHandler.removeAllVehicles();
    }

    private void runAtFixedRate(final BukkitRunnable task) {
        task.runTaskTimer(this, 0L, 1L);
    }
}
