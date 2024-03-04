package net.foulest.chatter;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.reactor.ReactorEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.foulest.chatter.input.Input;
import net.foulest.chatter.input.type.KeyInput;
import net.foulest.chatter.input.type.MouseInput;
import net.foulest.chatter.util.Application;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class for Chatter.
 *
 * @author Foulest
 * @project Chatter
 */
@Slf4j(topic = "Chatter")
public class Chatter {

    public static long lastKeyInput = System.currentTimeMillis();
    public static Thread keyInputThread = null;

    public static long lastMouseInput = System.currentTimeMillis();
    public static Thread mouseInputThread = null;

    // List of applications to monitor and translate inputs for
    public static final List<Application> APPLICATIONS = new ArrayList<>();

    static {
        // DS Emulators
        APPLICATIONS.add(new Application("DS Emulators (DeSmuME, Citra, etc.)",
                Arrays.asList("DeSmuME", "Citra"),
                Arrays.asList(
                        new KeyInput("UP", KeyEvent.VK_UP),
                        new KeyInput("DOWN", KeyEvent.VK_DOWN),
                        new KeyInput("LEFT", KeyEvent.VK_LEFT),
                        new KeyInput("RIGHT", KeyEvent.VK_RIGHT),
                        new KeyInput("X", KeyEvent.VK_A),
                        new KeyInput("Y", KeyEvent.VK_S),
                        new KeyInput("A", KeyEvent.VK_Z),
                        new KeyInput("B", KeyEvent.VK_X),
                        new KeyInput("L", KeyEvent.VK_Q),
                        new KeyInput("R", KeyEvent.VK_W),
                        new KeyInput("START", KeyEvent.VK_ENTER),
                        new KeyInput("SELECT", KeyEvent.VK_BACK_SPACE)
                )));

        // Gameboy Emulators
        APPLICATIONS.add(new Application("Gameboy Emulators (mGBA, etc.)",
                Arrays.asList("mGBA", "VisualBoyAdvance"),
                Arrays.asList(
                        new KeyInput("UP", KeyEvent.VK_UP),
                        new KeyInput("DOWN", KeyEvent.VK_DOWN),
                        new KeyInput("LEFT", KeyEvent.VK_LEFT),
                        new KeyInput("RIGHT", KeyEvent.VK_RIGHT),
                        new KeyInput("A", KeyEvent.VK_Z),
                        new KeyInput("B", KeyEvent.VK_X),
                        new KeyInput("L", KeyEvent.VK_A),
                        new KeyInput("R", KeyEvent.VK_S),
                        new KeyInput("START", KeyEvent.VK_ENTER),
                        new KeyInput("SELECT", KeyEvent.VK_BACK_SPACE)
                )));

        // Minecraft
        APPLICATIONS.add(new Application("Minecraft",
                Arrays.asList("Minecraft", "Cinnamon", "Lunar Client", "Badlion Client"),
                Arrays.asList(
                        new KeyInput("W", KeyEvent.VK_W),
                        new KeyInput("A", KeyEvent.VK_A),
                        new KeyInput("S", KeyEvent.VK_S),
                        new KeyInput("D", KeyEvent.VK_D),
                        new KeyInput("SPACE", KeyEvent.VK_SPACE),
                        new KeyInput("SHIFT", KeyEvent.VK_SHIFT),
                        new KeyInput("CTRL", KeyEvent.VK_CONTROL),
                        new KeyInput("E", KeyEvent.VK_E),
                        new KeyInput("Q", KeyEvent.VK_Q),
                        new KeyInput("1", KeyEvent.VK_1),
                        new KeyInput("2", KeyEvent.VK_2),
                        new KeyInput("3", KeyEvent.VK_3),
                        new KeyInput("4", KeyEvent.VK_4),
                        new KeyInput("5", KeyEvent.VK_5),
                        new KeyInput("6", KeyEvent.VK_6),
                        new KeyInput("7", KeyEvent.VK_7),
                        new KeyInput("8", KeyEvent.VK_8),
                        new KeyInput("9", KeyEvent.VK_9),
                        new KeyInput("MOUSE1", InputEvent.BUTTON1_DOWN_MASK),
                        new KeyInput("MOUSE2", InputEvent.BUTTON2_DOWN_MASK),
                        new KeyInput("MOUSE3", InputEvent.BUTTON3_DOWN_MASK),
                        new MouseInput("LEFT", MouseInput.Direction.LEFT),
                        new MouseInput("RIGHT", MouseInput.Direction.RIGHT),
                        new MouseInput("UP", MouseInput.Direction.UP),
                        new MouseInput("DOWN", MouseInput.Direction.DOWN)
                )));
    }

    public static void main(String[] args) {
        System.out.println("\nStarting Chatter...");

        // Asks the user for their Twitch OAuth token.
        // This is used to authenticate the bot with Twitch.
        // Get yours here: https://twitchapps.com/tmi
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter your Twitch OAuth token: ");
        String token = scanner.nextLine().trim();

        // Validates the OAuth token.
        if (!token.startsWith("oauth:") || token.length() != 36) {
            log.warn("Invalid OAuth token. It must start with 'oauth:'.");
            return;
        }

        // Sets the OAuth credentials.
        OAuth2Credential credential = new OAuth2Credential("twitch", token);

        // Asks the user for the Twitch channel they want to monitor.
        // This is the channel the bot will listen to for events.
        System.out.print("\nEnter the Twitch channel username you want to monitor: ");
        String channel = scanner.nextLine().trim();

        // Validates the channel username.
        if (channel.length() > 25 || channel.length() < 4 || channel.matches(".*[^a-zA-Z0-9_].*")) {
            log.warn("Invalid channel username. It must be between 4 and 25 characters"
                    + " and only contain letters, numbers, and underscores.");
            return;
        }

        // Asks the user for the application they want to monitor.
        // This is the application the bot will translate inputs for.
        System.out.println("\nSupported Applications:");
        for (int i = 0; i < APPLICATIONS.size(); i++) {
            System.out.println((i + 1) + ". " + APPLICATIONS.get(i).name);
        }
        System.out.print("\nEnter the application # you want to monitor: ");

        String appName = scanner.nextLine().trim();
        Application application;

        // Check if the input is numeric
        if (appName.matches("\\d+")) {
            int index = Integer.parseInt(appName) - 1;

            if (index >= 0 && index < APPLICATIONS.size()) {
                application = APPLICATIONS.get(index);
            } else {
                application = null;
            }
        } else {
            // Treat the input as an application name
            application = APPLICATIONS.stream()
                    .filter(app -> app.name.equalsIgnoreCase(appName))
                    .findFirst()
                    .orElse(null);
        }

        // Validates the application name.
        if (application == null) {
            log.warn("Invalid application name. It must be one of the applications listed above.");
            return;
        }

        // Sets up the Twitch client.
        System.out.println();
        log.info("Setting up the Twitch client...");
        TwitchClient twitchClient = TwitchClientBuilder.builder()
                .withDefaultEventHandler(ReactorEventHandler.class)
                .withDefaultAuthToken(credential)
                .withEnableChat(true)
                .withChatAccount(credential)
                .build();

        // Sets up the other variables.
        EventManager eventManager = twitchClient.getEventManager();
        TwitchChat chat = twitchClient.getChat();

        // Joins the channel and sends a message.
        log.info("Joining the channel and sending a message...");
        chat.leaveChannel(channel);
        chat.joinChannel(channel);

        synchronized (chat) {
            chat.sendMessage(channel, "[Chatter] Input monitoring is enabled!");
            chat.sendMessage(channel, "Valid inputs: " + application.getInputs().stream()
                    .map(Input::getInputName).collect(Collectors.joining(", "))
                    + " (Note: Uppercase inputs hold the button down for one"
                    + " second; lowercase inputs press the button once.)"
            );
        }

        // Listens for chat messages and processes them as inputs.
        log.info("Setting up the input listener...");
        eventManager.onEvent(IRCMessageEvent.class, event -> event.getMessage().ifPresent(message -> {
            String trimmedMessage = message.trim();

            for (Input inputs : application.getInputs()) {
                String input = inputs.getInputName();

                // Check if the trimmed message matches any allowed input (case-insensitive)
                if (trimmedMessage.equalsIgnoreCase(input)) {
                    boolean isLongInput = trimmedMessage.equals(input);
                    String inputType = isLongInput ? "long" : "short";

                    log.info("Received " + inputType + " input: " + message);

                    // Check if the current window title is allowed
                    String windowTitle = getForegroundWindowText();
                    if (windowTitle == null || application.getWindowTitles().stream().noneMatch(windowTitle::contains)) {
                        log.info("Ignoring " + inputType + " input due to invalid window title: " + windowTitle);
                        return;
                    }

                    // If all checks pass, start the input
                    log.info("Inputting " + inputType + " input: " + message);

                    if (inputs instanceof KeyInput) {
                        KeyInput keyInput = (KeyInput) inputs;
                        startKeyInput(keyInput.getKeyCode(), isLongInput);
                    } else {
                        MouseInput mouseInput = (MouseInput) inputs;
                        startMouseInput(mouseInput.getDirection(), isLongInput);
                    }
                    break;
                }
            }
        }));

        log.info("Chatter is now running!");
    }

    /**
     * Starts a key input.
     *
     * @param keyCode   The key code to press.
     * @param longInput Whether the input is long or short.
     */
    @Synchronized
    public static void startKeyInput(int keyCode, boolean longInput) {
        // Update the lastInput timestamp
        lastKeyInput = System.currentTimeMillis();

        // If there is an ongoing input, interrupt it
        if (keyInputThread != null && keyInputThread.isAlive()) {
            keyInputThread.interrupt();
        }

        // Create a new thread for the new input
        keyInputThread = new Thread(() -> {
            try {
                Robot robot = new Robot();
                boolean mouseInput = keyCode == InputEvent.BUTTON1_DOWN_MASK || keyCode == InputEvent.BUTTON2_DOWN_MASK;

                // Press the key (or mouse button)
                if (mouseInput) {
                    robot.mousePress(keyCode);
                } else {
                    robot.keyPress(keyCode);
                }

                // Sleep for the input duration
                if (longInput) {
                    Thread.sleep(1000L);
                } else {
                    Thread.sleep(200L);
                }

                // Release the key (or mouse button)
                if (mouseInput) {
                    robot.mouseRelease(keyCode);
                } else {
                    robot.keyRelease(keyCode);
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted: Releasing key press early.");

                try {
                    Robot robot = new Robot();
                    robot.keyRelease(keyCode);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Start the new input thread
        keyInputThread.start();
    }

    /**
     * Starts a mouse input.
     *
     * @param direction The direction to move the mouse in.
     * @param longInput Whether the input is long or short.
     */
    @SuppressWarnings("BusyWait")
    @Synchronized
    public static void startMouseInput(MouseInput.Direction direction, boolean longInput) {
        // Update the lastInput timestamp
        lastMouseInput = System.currentTimeMillis();

        // If there is an ongoing input, interrupt it
        if (mouseInputThread != null && mouseInputThread.isAlive()) {
            mouseInputThread.interrupt();
        }

        // Create a new thread for the new input
        mouseInputThread = new Thread(() -> {
            try {
                Robot robot = new Robot();
                Point currentPosition = MouseInfo.getPointerInfo().getLocation();
                int x = currentPosition.x;
                int y = currentPosition.y;
                final int movementDistance = 10; // Customize this value as needed
                int duration = longInput ? 500 : 250; // Duration in milliseconds
                long endTime = System.currentTimeMillis() + duration;

                while (System.currentTimeMillis() < endTime) {
                    switch (direction) {
                        case UP:
                            y -= movementDistance;
                            break;
                        case DOWN:
                            y += movementDistance;
                            break;
                        case LEFT:
                            x -= movementDistance;
                            break;
                        case RIGHT:
                            x += movementDistance;
                            break;
                    }

                    robot.mouseMove(x, y);
                    Thread.sleep(25);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Start the new input thread
        mouseInputThread.start();
    }

    /**
     * Interface for the user32.dll library.
     * This is used to get the title of the current foreground window.
     */
    interface MyUser32 extends StdCallLibrary {

        MyUser32 INSTANCE = Native.load("user32", MyUser32.class);

        WinDef.HWND GetForegroundWindow();

        void GetWindowTextA(WinDef.HWND hWnd, byte[] lpString, int nMaxCount);
    }

    /**
     * Gets the title of the current foreground window.
     *
     * @return The title of the current foreground window.
     */
    public static @Nullable String getForegroundWindowText() {
        WinDef.HWND hwnd = MyUser32.INSTANCE.GetForegroundWindow();

        if (hwnd == null) {
            return null;
        }

        byte[] windowText = new byte[512];
        MyUser32.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
        return Native.toString(windowText).trim();
    }
}
