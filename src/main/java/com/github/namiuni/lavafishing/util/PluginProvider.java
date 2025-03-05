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
