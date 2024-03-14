package net.foulest.chatter;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.reactor.ReactorEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.chat.events.channel.UserStateEvent;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.foulest.chatter.input.Input;
import net.foulest.chatter.input.InputRequest;
import net.foulest.chatter.input.type.KeyInput;
import net.foulest.chatter.input.type.MouseInput;
import net.foulest.chatter.util.Application;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
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

    public static Queue<InputRequest> inputQueue = new ConcurrentLinkedQueue<>();

    public static boolean isBroadcaster = false;
    public static Application application = null;

    // List of applications to monitor and translate inputs for
    public static final List<Application> APPLICATIONS = new ArrayList<>();

    static {
        // DS Emulators
        APPLICATIONS.add(new Application("DS Emulators (DeSmuME, Citra, etc.)",
                Arrays.asList("DeSmuME", "Citra"),
                Arrays.asList(
                        new KeyInput("UP", KeyEvent.VK_UP, 130, 1000),
                        new KeyInput("DOWN", KeyEvent.VK_DOWN, 130, 1000),
                        new KeyInput("LEFT", KeyEvent.VK_LEFT, 130, 1000),
                        new KeyInput("RIGHT", KeyEvent.VK_RIGHT, 130, 1000),
                        new KeyInput("X", KeyEvent.VK_A, 130, 1000),
                        new KeyInput("Y", KeyEvent.VK_S, 130, 1000),
                        new KeyInput("A", KeyEvent.VK_Z, 130, 1000),
                        new KeyInput("B", KeyEvent.VK_X, 130, 1000),
                        new KeyInput("L", KeyEvent.VK_Q, 130, 1000),
                        new KeyInput("R", KeyEvent.VK_W, 130, 1000),
                        new KeyInput("START", KeyEvent.VK_ENTER, 130, 1000),
                        new KeyInput("SELECT", KeyEvent.VK_BACK_SPACE, 130, 1000)
                )));

        // Gameboy Emulators
        APPLICATIONS.add(new Application("Gameboy Emulators (mGBA, etc.)",
                Arrays.asList("mGBA", "VisualBoyAdvance"),
                Arrays.asList(
                        new KeyInput("UP", KeyEvent.VK_UP, 130, 1000),
                        new KeyInput("DOWN", KeyEvent.VK_DOWN, 130, 1000),
                        new KeyInput("LEFT", KeyEvent.VK_LEFT, 130, 1000),
                        new KeyInput("RIGHT", KeyEvent.VK_RIGHT, 130, 1000),
                        new KeyInput("A", KeyEvent.VK_Z, 130, 1000),
                        new KeyInput("B", KeyEvent.VK_X, 130, 1000),
                        new KeyInput("L", KeyEvent.VK_A, 130, 1000),
                        new KeyInput("R", KeyEvent.VK_S, 130, 1000),
                        new KeyInput("START", KeyEvent.VK_ENTER, 130, 1000),
                        new KeyInput("SELECT", KeyEvent.VK_BACK_SPACE, 130, 1000)
                )));

        // Minecraft
        APPLICATIONS.add(new Application("Minecraft",
                Arrays.asList("Minecraft", "Cinnamon", "Lunar Client", "Badlion Client"),
                Arrays.asList(
                        new KeyInput("W", KeyEvent.VK_W, 200, 1000),
                        new KeyInput("A", KeyEvent.VK_A, 200, 1000),
                        new KeyInput("S", KeyEvent.VK_S, 200, 1000),
                        new KeyInput("D", KeyEvent.VK_D, 200, 1000),
                        new KeyInput("SPACE", KeyEvent.VK_SPACE, 200, 1000),
                        new KeyInput("SHIFT", KeyEvent.VK_SHIFT, 200, 1000),
                        new KeyInput("CTRL", KeyEvent.VK_CONTROL, 200, 1000),
                        new KeyInput("E", KeyEvent.VK_E, 200, 1000),
                        new KeyInput("Q", KeyEvent.VK_Q, 200, 1000),
                        new KeyInput("1", KeyEvent.VK_1, 200, 1000),
                        new KeyInput("2", KeyEvent.VK_2, 200, 1000),
                        new KeyInput("3", KeyEvent.VK_3, 200, 1000),
                        new KeyInput("4", KeyEvent.VK_4, 200, 1000),
                        new KeyInput("5", KeyEvent.VK_5, 200, 1000),
                        new KeyInput("6", KeyEvent.VK_6, 200, 1000),
                        new KeyInput("7", KeyEvent.VK_7, 200, 1000),
                        new KeyInput("8", KeyEvent.VK_8, 200, 1000),
                        new KeyInput("9", KeyEvent.VK_9, 200, 1000),
                        new KeyInput("MOUSE1", InputEvent.BUTTON1_DOWN_MASK, 200, 1000),
                        new KeyInput("MOUSE2", InputEvent.BUTTON2_DOWN_MASK, 200, 1000),
                        new KeyInput("MOUSE3", InputEvent.BUTTON3_DOWN_MASK, 200, 1000),
                        new MouseInput("LEFT", MouseInput.Direction.LEFT, 250, 500),
                        new MouseInput("RIGHT", MouseInput.Direction.RIGHT, 250, 500),
                        new MouseInput("UP", MouseInput.Direction.UP, 250, 500),
                        new MouseInput("DOWN", MouseInput.Direction.DOWN, 250, 500)
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

        // Check if the input is numeric
        if (appName.matches("\\d+")) {
            int index = Integer.parseInt(appName) - 1;

            if (index >= 0 && index < APPLICATIONS.size()) {
                application = APPLICATIONS.get(index);
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

        // Listens for the broadcaster status on UserStateEvent.
        eventManager.onEvent(UserStateEvent.class, event -> {
            if (event.isBroadcaster()) {
                isBroadcaster = true;
            }
        });

        // Attempts to verify the broadcaster status.
        log.info("Attempting to verify broadcaster status...");
        long startTime = System.currentTimeMillis(); // Capture start time
        long timeout = 5000; // Timeout in milliseconds (5 seconds)

        // Waits for the broadcaster status to be verified.
        // If the status is not verified within the timeout period, the program will exit.
        while (!isBroadcaster) {
            if (System.currentTimeMillis() - startTime > timeout) {
                log.error("Failed to verify broadcaster status within the timeout period.");
                System.exit(0);
                return;
            }

            // Wait for the broadcaster status to be verified
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.error("Thread was interrupted.", ex);
                return;
            }
        }

        // Verifies the broadcaster status.
        log.info("Broadcaster status verified.");

        // Sends a message to the channel.
        log.info("Sending a message to the channel...");
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
            // Ignores messages that aren't sent by chatters.
            if (!event.getCommandType().equals("PRIVMSG")) {
                return;
            }

            // Ignores messages if the user is not the broadcaster.
            if (!isBroadcaster) {
                log.info("Ignoring message: " + message + " (not the broadcaster)");
                return;
            }

            // Ignores messages if the application's window is not in focus.
            String windowTitle = getForegroundWindowTitle();
            if (windowTitle == null || application.getWindowTitles().stream().noneMatch(windowTitle::contains)) {
                log.info("Ignoring message: " + message + " (application not in focus)"
                        + " | " + event
                );
                return;
            }

            message = message.trim();

            for (Input input : application.getInputs()) {
                String inputName = input.getInputName();

                // Check if the trimmed message matches any allowed input (case-insensitive)
                if (message.equalsIgnoreCase(inputName)) {
                    log.info("Adding input to queue: " + message);
                    inputQueue.add(new InputRequest(message, input, System.currentTimeMillis()));

                    new Thread(() -> {
                        // Add a delay to allow accumulation of inputs
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }

                        // Process the queue
                        if (!inputQueue.isEmpty()) {
                            String mostFrequentInputName = findMostFrequentInput();

                            if (mostFrequentInputName != null) {
                                // Find the corresponding InputRequest for the most frequent input
                                for (InputRequest request : inputQueue) {
                                    if (request.input.getInputName().equalsIgnoreCase(mostFrequentInputName)) {
                                        boolean isLongInput = request.message.toUpperCase().equals(request.message);
                                        log.info("Processing input: " + request.message);

                                        if (request.input instanceof KeyInput) {
                                            startKeyInput((KeyInput) request.input, isLongInput);
                                        } else if (request.input instanceof MouseInput) {
                                            startMouseInput((MouseInput) request.input, isLongInput);
                                        }
                                        break;
                                    }
                                }
                            }

                            inputQueue.clear(); // Clear the queue after processing
                        }
                    }).start();
                    break;
                }
            }
        }));

        log.info("Chatter is now running!");
    }

    /**
     * Finds the most frequent input in the input queue.
     *
     * @return The name of the most frequent input, or null if the queue is empty.
     */
    public static @Nullable String findMostFrequentInput() {
        if (inputQueue.isEmpty()) {
            return null;
        }

        Map<String, Integer> inputCount = new HashMap<>();
        String mostFrequentInput = null;
        int maxCount = 0;

        // Tally occurrences of each input
        for (InputRequest request : inputQueue) {
            String inputName = request.input.getInputName().toLowerCase(); // Normalize input name to lower case
            int count = inputCount.getOrDefault(inputName, 0) + 1;
            inputCount.put(inputName, count);

            if (count > maxCount) {
                maxCount = count;
                mostFrequentInput = inputName;
            }
        }
        return mostFrequentInput;
    }

    /**
     * Starts a key input.
     *
     * @param keyInput  The key input to start.
     * @param longInput Whether the input is long or short.
     */
    @Synchronized
    public static void startKeyInput(KeyInput keyInput, boolean longInput) {
        // Update the lastInput timestamp
        lastKeyInput = System.currentTimeMillis();

        // If there is an ongoing input, interrupt it
        if (keyInputThread != null && keyInputThread.isAlive()) {
            keyInputThread.interrupt();
        }

        int keyCode = keyInput.getKeyCode();

        boolean mouseInput = keyCode == InputEvent.BUTTON1_DOWN_MASK
                || keyCode == InputEvent.BUTTON2_DOWN_MASK;

        // Create a new thread for the new input
        keyInputThread = new Thread(() -> {
            try {
                Robot robot = new Robot();

                // Press the key (or mouse button)
                if (mouseInput) {
                    robot.mousePress(keyCode);
                } else {
                    robot.keyPress(keyCode);
                }

                // Sleep for the input duration
                if (longInput) {
                    Thread.sleep(keyInput.getLongDuration());
                } else {
                    Thread.sleep(keyInput.getShortDuration());
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
     * @param mouseInput The mouse input to start.
     * @param longInput  Whether the input is long or short.
     */
    @Synchronized
    @SuppressWarnings("BusyWait")
    public static synchronized void startMouseInput(MouseInput mouseInput, boolean longInput) {
        // Update the lastMouseInput timestamp
        lastMouseInput = System.currentTimeMillis();

        // If there is an ongoing input, interrupt it
        if (mouseInputThread != null && mouseInputThread.isAlive()) {
            mouseInputThread.interrupt();
        }

        MouseInput.Direction direction = mouseInput.getDirection();

        // Create a new thread for the new input
        mouseInputThread = new Thread(() -> {
            try {
                Robot robot = new Robot();
                WinDef.RECT rect = new WinDef.RECT();

                if (MyUser32.INSTANCE.GetWindowRect(MyUser32.INSTANCE.GetForegroundWindow(), rect)) {
                    // Define the boundaries of the window
                    // This is used to ensure the mouse stays within the window's boundaries
                    int buffer = 10;
                    int minX = rect.left + buffer;
                    int maxX = rect.right - buffer;
                    int minY = rect.top + buffer;
                    int maxY = rect.bottom - buffer;

                    Point currentPosition = MouseInfo.getPointerInfo().getLocation();
                    int x = currentPosition.x;
                    int y = currentPosition.y;

                    long duration = longInput ? mouseInput.getLongDuration() : mouseInput.getShortDuration();
                    long endTime = System.currentTimeMillis() + duration;

                    int movementDistance = 10;

                    while (System.currentTimeMillis() < endTime) {
                        switch (direction) {
                            case UP:
                                y = Math.max(y - movementDistance, minY);
                                break;
                            case DOWN:
                                y = Math.min(y + movementDistance, maxY);
                                break;
                            case LEFT:
                                x = Math.max(x - movementDistance, minX);
                                break;
                            case RIGHT:
                                x = Math.min(x + movementDistance, maxX);
                                break;
                            default:
                                break;
                        }

                        // Ensure the mouse stays within the window's boundaries
                        x = Math.max(minX, Math.min(x, maxX));
                        y = Math.max(minY, Math.min(y, maxY));

                        // Moves the mouse
                        robot.mouseMove(x, y);

                        // Sleep for a short duration to prevent the mouse from moving too quickly
                        Thread.sleep(25);
                    }
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

        boolean GetWindowRect(WinDef.HWND hWnd, WinDef.RECT rect);
    }

    /**
     * Gets the title of the current foreground window.
     *
     * @return The title of the current foreground window.
     */
    public static @Nullable String getForegroundWindowTitle() {
        WinDef.HWND hwnd = MyUser32.INSTANCE.GetForegroundWindow();

        if (hwnd == null) {
            return null;
        }

        byte[] windowText = new byte[512];
        MyUser32.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
        return Native.toString(windowText).trim();
    }
}
