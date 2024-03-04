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
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;

/**
 * Main class for Chatter.
 *
 * @author Foulest
 * @project Chatter
 */
@Slf4j(topic = "Chatter")
public class Chatter {

    public static long lastInput = System.currentTimeMillis();
    public static Thread inputThread = null;

    public static final long LONG_INPUT_DURATION = 1000L; // Duration of long inputs in milliseconds
    public static final long SHORT_INPUT_DURATION = 200L; // Duration of short inputs in milliseconds

    public static final Map<String, Integer> ALLOWED_INPUTS = new LinkedHashMap<>();
    public static final List<String> ALLOWED_WINDOW_TITLES = new ArrayList<>();

    static {
        ALLOWED_INPUTS.put("UP", KeyEvent.VK_UP); // Forward
        ALLOWED_INPUTS.put("DOWN", KeyEvent.VK_DOWN); // Backward
        ALLOWED_INPUTS.put("LEFT", KeyEvent.VK_LEFT); // Left
        ALLOWED_INPUTS.put("RIGHT", KeyEvent.VK_RIGHT); // Right
        ALLOWED_INPUTS.put("X", KeyEvent.VK_A); // X Button
        ALLOWED_INPUTS.put("Y", KeyEvent.VK_S); // Y Button
        ALLOWED_INPUTS.put("A", KeyEvent.VK_Z); // A Button
        ALLOWED_INPUTS.put("B", KeyEvent.VK_X); // B Button
        ALLOWED_INPUTS.put("START", KeyEvent.VK_ENTER); // Start Button
        ALLOWED_INPUTS.put("SELECT", KeyEvent.VK_BACK_SPACE); // Select Button

        ALLOWED_WINDOW_TITLES.add("DeSmuME"); // DS Emulator
    }

    public static void main(String[] args) {
        System.out.println("Starting Chatter...");

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
            chat.sendMessage(channel, "Valid inputs: " + String.join(", ", ALLOWED_INPUTS.keySet())
                    + " (Note: Uppercase inputs hold the button down for one"
                    + " second; lowercase inputs press the button once.)"
            );
        }

        // Listens for chat messages and processes them as inputs.
        log.info("Setting up the input listener...");
        eventManager.onEvent(IRCMessageEvent.class, event -> event.getMessage().ifPresent(message -> {
            String trimmedMessage = message.trim();

            for (Map.Entry<String, Integer> entry : ALLOWED_INPUTS.entrySet()) {
                String input = entry.getKey();

                // Check if the trimmed message matches any allowed input (case-insensitive)
                if (trimmedMessage.equalsIgnoreCase(input)) {
                    boolean longInput = trimmedMessage.equals(input);
                    String inputType = longInput ? "long" : "short";
                    log.info("Received " + inputType + " input: " + message);

                    // Check if the current window title is allowed
                    String windowTitle = getForegroundWindowText();
                    if (windowTitle == null || ALLOWED_WINDOW_TITLES.stream().noneMatch(windowTitle::contains)) {
                        log.info("Ignoring " + inputType + " input due to invalid window title: " + windowTitle);
                        return;
                    }

                    // If all checks pass, press the key
                    log.info("Inputting " + inputType + " input: " + message);
                    startKeyPress(entry.getValue(), longInput);

                    // Since a match was found, no need to continue checking other inputs
                    break;
                }
            }
        }));

        log.info("Chatter is now running!");
    }

    /**
     * Starts a key press.
     *
     * @param keyCode   The key code to press.
     * @param longInput Whether the input is long or short.
     */
    @Synchronized
    public static void startKeyPress(int keyCode, boolean longInput) {
        // Update the lastInput timestamp
        lastInput = System.currentTimeMillis();

        // If there is an ongoing input, interrupt it
        if (inputThread != null && inputThread.isAlive()) {
            inputThread.interrupt();
        }

        // Create a new thread for the new input
        inputThread = new Thread(() -> {
            try {
                Robot robot = new Robot();

                // Press the key
                robot.keyPress(keyCode);

                // Sleep for the input duration
                if (longInput) {
                    Thread.sleep(LONG_INPUT_DURATION);
                } else {
                    Thread.sleep(SHORT_INPUT_DURATION);
                }

                // Release the key
                robot.keyRelease(keyCode);
            } catch (InterruptedException e) {
                // If the sleep is interrupted, ensure the key is released
                System.out.println("Interrupted: Releasing key early.");

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
        inputThread.start();
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
