/*
 * Chatter - a Twitch chat utility that translates chat messages into key presses.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.chatter.input.type;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.foulest.chatter.input.Input;

/**
 * Represents a mouse input.
 */
@Getter
@Setter
@ToString
public class MouseInput implements Input {

    private String inputName;
    private Direction direction;
    private long shortDuration;
    private long longDuration;

    /**
     * Represents the direction of the mouse input.
     */
    public enum Direction {
        /**
         * Represents the left direction.
         */
        LEFT,

        /**
         * Represents the right direction.
         */
        RIGHT,

        /**
         * Represents the up direction.
         */
        UP,

        /**
         * Represents the down direction.
         */
        DOWN
    }

    /**
     * Constructs a new mouse input.
     *
     * @param inputName The name of the input.
     * @param direction The direction of the input.
     * @param shortDuration The short duration of the input.
     * @param longDuration The long duration of the input.
     */
    public MouseInput(String inputName, Direction direction, long shortDuration, long longDuration) {
        this.inputName = inputName;
        this.direction = direction;
        this.shortDuration = shortDuration;
        this.longDuration = longDuration;
    }
}
