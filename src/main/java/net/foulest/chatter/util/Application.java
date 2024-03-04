package net.foulest.chatter.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.foulest.chatter.input.Input;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Application {

    public String name;
    public List<String> windowTitles;
    public List<Input> inputs;
}
