package com.github.namiuni.lavafishing.event;

import com.github.namiuni.lavafishing.fishing.LavaHook;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

/**
 * Called when an items are chosen from the {@link LootTable}.
 *
 * @since 1.0.0
 */
@NullMarked
@ApiStatus.Experimental
@SuppressWarnings("unused")
public final class LavaHookLootTableRollEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final LavaHook lavaHook;
    private final Collection<ItemStack> catchItems;

    @ApiStatus.Internal
    public LavaHookLootTableRollEvent(
            final LavaHook lavaHook,
            final Collection<ItemStack> catchItems
    ) {
        this.lavaHook = lavaHook;
        this.catchItems = catchItems;
    }

    /**
     * Gets the lava hook.
     *
     * @return the lava hook
     * @since 1.0.0
     */
    public LavaHook getLavaHook() {
        return this.lavaHook;
    }

    /**
     * Gets the chosen items.
     *
     * @return mutable selected items
     * @since 1.0.0
     */
    public Collection<ItemStack> getResultItems() {
        return this.catchItems;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
