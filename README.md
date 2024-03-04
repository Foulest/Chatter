# Chatter

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![CodeQL Badge](https://github.com/Foulest/Chatter/actions/workflows/codeql.yml/badge.svg)](https://github.com/Foulest/Chatter/actions/workflows/codeql.yml)
[![JitPack Badge](https://jitpack.io/v/Foulest/Chatter.svg)](https://jitpack.io/#Foulest/Chatter)

**Chatter** is a Twitch chat utility that translates chat messages into key presses.

## Usage

Chatter's main usage is, of course, allowing chat to play video games like Pokemon by themselves, but it can also be
used for other purposes.

In order to start using Chatter, you will need to clone the repository and run the `main` method in the `Chatter` class.

After inputting your **[OAuth Token](https://twitchapps.com/tmi/)**  and the channel you want to listen to, you can start sending messages to the chat
and the application will translate them into key presses.

Chatter will only send inputs if the active window is of the allowed window titles. You can modify the allowed
window titles in the `ALLOWED_WINDOW_TITLES` list in the main class.

All of Chatter's inputs can be modified in the `ALLOWED_INPUTS` list in the main class. Note: the inputs are
case-sensitive. Chat messages that are **uppercase** will press the key and hold it for one second.
Chat messages that are **lowercase** will press the key and release it immediately.

## Getting Help

For support or queries, please open an issue in the [Issues section](https://github.com/Foulest/Chatter/issues).
