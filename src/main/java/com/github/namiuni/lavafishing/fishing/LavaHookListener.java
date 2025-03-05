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
package com.github.namiuni.lavafishing.fishing;

import com.github.namiuni.lavafishing.config.ConfigLoader;
import com.github.namiuni.lavafishing.util.LavaFishingPermissions;
import com.github.namiuni.lavafishing.util.LavaFishingUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
@ApiStatus.Internal
public final class LavaHookListener implements Listener {

    private final ConfigLoader configLoader;
    private final LavaHookHandler lavaFishingHandler;

    public LavaHookListener(
            final ConfigLoader configLoader,
            final LavaHookHandler lavaFishingHandler
    ) {
        this.configLoader = configLoader;
        this.lavaFishingHandler = lavaFishingHandler;
    }

    @EventHandler
    private void onFishing(final PlayerFishEvent event) {
        // Check permission
        if (!event.getPlayer().hasPermission(LavaFishingPermissions.PLAY_LAVA_FISHING)) {
            return;
        }

        // Check world
        if (!this.configLoader.primaryConfig().allowWorlds().contains(event.getHook().getWorld().key())) {
            return;
        }

        switch (event.getState()) {
            case FISHING -> this.lavaFishingHandler.cast(event.getHook());
            case REEL_IN -> {
                if (this.lavaFishingHandler.wind(event.getPlayer(), Objects.requireNonNull(event.getHand()))) {
                    event.setCancelled(true); // If a player fish event is called, it will be doubled, so cancel it.
                }
            }
            // Events other than the above do not require listen.
        }
    }

    @EventHandler
    private void onProjectileHit(final ProjectileHitEvent event) {
        final Entity projectile = event.getEntity();
        final Entity target = event.getHitEntity();
        if (!LavaFishingUtil.isLavaHooksVehicle(event.getEntity())) {
            return;
        }

        if (target != null) {
            this.lavaFishingHandler.hook(projectile, target);
        }

        event.setCancelled(true);
    }

    @EventHandler
    private void onEntityRemove(final EntityRemoveEvent event) {
        if (event.getEntity() instanceof FishHook fishHook && LavaFishingUtil.isLavaHook(fishHook)) {
            this.lavaFishingHandler.cleanUp(fishHook);
        }
    }

    // Remove the fake bobber left in the spawn chunk, although it's as unlikely as possible.
    @EventHandler
    private void onServerLoad(final ServerLoadEvent event) {
        this.lavaFishingHandler.removeAllVehicles();
    }
}
