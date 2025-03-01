package com.github.namiuni.lavafishing.fishing;

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

    private final LavaHookHandler lavaFishingHandler;

    public LavaHookListener(final LavaHookHandler lavaFishingHandler) {
        this.lavaFishingHandler = lavaFishingHandler;
    }

    @EventHandler
    private void onFishing(final PlayerFishEvent event) {
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
