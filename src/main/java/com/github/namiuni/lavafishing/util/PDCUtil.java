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
