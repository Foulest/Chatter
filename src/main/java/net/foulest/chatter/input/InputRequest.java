package net.foulest.chatter.input;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputRequest {

    public String message;
    public Input input;
    public long timestamp;
}
