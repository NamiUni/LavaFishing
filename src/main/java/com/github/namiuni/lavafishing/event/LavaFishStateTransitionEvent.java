package com.github.namiuni.lavafishing.event;

import com.github.namiuni.lavafishing.fishing.LavaHook;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Called just before a {@link LavaHook}'s {@link FishState} is changed.
 *
 * <p>Fish state is diverted from Bukkit API</p>
 *
 * @since 1.0.0
 */
@NullMarked
@ApiStatus.Experimental
@SuppressWarnings("unused")
public final class LavaFishStateTransitionEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final LavaHook lavaHook;
    private final FishState fishState;

    private boolean cancelled = false;

    @ApiStatus.Internal
    public LavaFishStateTransitionEvent(final LavaHook lavaHook, final FishState fishState) {
        this.lavaHook = lavaHook;
        this.fishState =fishState;
    }

    /**
     * Gets the lava hook.
     *
     * @return the lava hook.
     * @since 1.0.0
     */
    public LavaHook getLavaHook() {
        return this.lavaHook;
    }

    /**
     * Get the <strong>new</strong> fish state of the lava hook.
     *
     * @return the <strong>new</strong> fish state.
     * @since 1.0.0
     */
    public FishState getNewFishState() {
        return this.fishState;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean canceled) {
        this.cancelled = canceled;
    }

    public enum FishState { //TODO more!!
        BITE,
        LURED,
        ESCAPE
    }
}
