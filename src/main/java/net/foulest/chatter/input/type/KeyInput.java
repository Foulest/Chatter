package net.foulest.chatter.input.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.foulest.chatter.input.Input;

@Getter
@Setter
@AllArgsConstructor
public class KeyInput extends Input {

    public String inputName;
    public int keyCode;
}
