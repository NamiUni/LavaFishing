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

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;

@NullMarked
@ApiStatus.Internal
public final class PDCUtil {

    private static final String PLUGIN_NAME = "lava_fishing";
    private static final Function<String, NamespacedKey> KEY_FACTORY = key -> new NamespacedKey(PLUGIN_NAME, key);

    private PDCUtil() {

    }

    public static void setInOpenLava(final PersistentDataContainer container, boolean value) {
        container.set(KEY_FACTORY.apply("in_open_lava"), PersistentDataType.BOOLEAN, value);
    }
}
