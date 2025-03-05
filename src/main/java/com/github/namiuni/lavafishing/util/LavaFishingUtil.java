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

import io.papermc.paper.block.fluid.FluidData;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.FluidTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import org.bukkit.Fluid;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Experimental
@SuppressWarnings("UnstableApiUsage")
public final class LavaFishingUtil {

    public static final String LAVA_HOOK_META_DATA = "lava_hook";
    public static final String HOOK_VEHICLE_META_DATA = "lava_vehicle";

    private LavaFishingUtil() {

    }

    public static boolean isFishItem(final ItemStack itemStack) {
        final Registry<ItemType> itemRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);
        final TypedKey<ItemType> itemType = TypedKey.create(RegistryKey.ITEM, itemStack.getType().key());
        final Tag<ItemType> fishTag = itemRegistry.getTag(ItemTypeTagKeys.FISHES);
        return fishTag.contains(itemType);
    }

    public static boolean isLavaFluid(final FluidData fluidData) {
        final Registry<Fluid> fluidRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.FLUID);
        final TypedKey<Fluid> fluidType = TypedKey.create(RegistryKey.FLUID, fluidData.getFluidType().key());
        final Tag<Fluid> lavaTag = fluidRegistry.getTag(FluidTagKeys.LAVA);
        return lavaTag.contains(fluidType);
    }

    public static boolean isLavaHook(final FishHook fishHook) {
        return fishHook.hasMetadata(LAVA_HOOK_META_DATA);
    }

    public static boolean isLavaHooksVehicle(final Entity entity) {
        return entity.hasMetadata(HOOK_VEHICLE_META_DATA);
    }

    public static void markLavaHook(final FishHook fishHook) {
        fishHook.setMetadata(LAVA_HOOK_META_DATA, new FixedMetadataValue(PluginProvider.plugin(), null));
    }

    public static void markLavaHooksVehicle(final Entity entity) {
        entity.setMetadata(HOOK_VEHICLE_META_DATA, new FixedMetadataValue(PluginProvider.plugin(), null));
    }
}
