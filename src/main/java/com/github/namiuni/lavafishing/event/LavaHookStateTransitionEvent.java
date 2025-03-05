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
package com.github.namiuni.lavafishing.event;

import com.github.namiuni.lavafishing.fishing.LavaHook;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Called just before a {@link LavaHook}'s hook state is changed.
 *
 * @see FishHook.HookState
 * @since 1.0.0
 */
@NullMarked
@ApiStatus.Experimental
@SuppressWarnings("unused")
public final class LavaHookStateTransitionEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final LavaHook lavaHook;
    private final FishHook.HookState newHookState;

    @ApiStatus.Internal
    public LavaHookStateTransitionEvent(final LavaHook lavaHook, final FishHook.HookState newHookState) {
        this.lavaHook = lavaHook;
        this.newHookState = newHookState;
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
     * Get the <strong>new</strong> hook state of the {@link LavaHook}.
     *
     * <p>Refer to {@link LavaHook#getHookState()} to get the current hook state.</p>
     *
     * @return the <strong>new</strong> hook state
     * @since 1.0.0
     */
    public FishHook.HookState getNewHookState() {
        return this.newHookState;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
