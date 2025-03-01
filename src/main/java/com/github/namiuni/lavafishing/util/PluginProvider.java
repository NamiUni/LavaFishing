package com.github.namiuni.lavafishing.util;

import com.github.namiuni.lavafishing.LavaFishing;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
public final class PluginProvider {

    private static @MonotonicNonNull LavaFishing lavaFishing;

    public static void register(final LavaFishing lavaFishing) {
        PluginProvider.lavaFishing = lavaFishing;
    }

    public static LavaFishing plugin() throws IllegalStateException {
        if (lavaFishing == null) {
            throw new IllegalStateException("LavaFishing class is not instantiated");
        }

        return lavaFishing;
    }
}
