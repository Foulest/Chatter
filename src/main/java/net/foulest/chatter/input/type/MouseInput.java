package net.foulest.chatter.input.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.foulest.chatter.input.Input;

@Getter
@Setter
@AllArgsConstructor
public class MouseInput extends Input {

    public String inputName;
    public Direction direction;
    public long shortDuration;
    public long longDuration;

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
