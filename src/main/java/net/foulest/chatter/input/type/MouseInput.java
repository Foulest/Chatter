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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.foulest.chatter.input.Input;

@Getter
@Setter
@AllArgsConstructor
public class MouseInput implements Input {

    private String inputName;
    private Direction direction;
    private long shortDuration;
    private long longDuration;

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
