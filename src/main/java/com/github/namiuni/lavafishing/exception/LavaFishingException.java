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
package com.github.namiuni.lavafishing.exception;

import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class LavaFishingException extends RuntimeException {
    public LavaFishingException() {
        super();
    }

    public LavaFishingException(final String message) {
        super(message);
    }

    public LavaFishingException(final Throwable cause) {
        super(cause);
    }

    public LavaFishingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LavaFishingException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
