package com.github.namiuni.lavafishing.fishing;

import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
public final class LavaHookTask extends BukkitRunnable {

    private final LavaHook lavaHook;

    public LavaHookTask(final LavaHook lavaHook) {
        this.lavaHook = lavaHook;
    }

    @Override
    public void run() {
        if (!this.lavaHook.tick()) {
            this.cancel();
        }
    }
}
