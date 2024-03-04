# Chatter

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![CodeQL Badge](https://github.com/Foulest/Chatter/actions/workflows/codeql.yml/badge.svg)](https://github.com/Foulest/Chatter/actions/workflows/codeql.yml)
[![JitPack Badge](https://jitpack.io/v/Foulest/Chatter.svg)](https://jitpack.io/#Foulest/Chatter)

**Chatter** is a Twitch chat utility that translates chat messages into key presses.

## Usage

Chatter's main usage is, of course, allowing chat to play video games like Pokemon by themselves, but it can also be
used for other purposes. In order to start using Chatter, you will need to clone the repository.

After cloning the repository, run the `main` method in the `Chatter` class.

You will need a Twitch account and an OAuth token to use the application. You can generate an OAuth token
[here](https://twitchapps.com/tmi/).

After inputting your OAuth token and the channel you want to listen to, you can start sending messages to the chat
and the application will translate them into key presses.

Chat messages that are **uppercase** will be translated into a key press and a key release one second later.

Chat messages that are **lowercase** will be translated into a key press and a key release immediately.

There are some pre-configured key bindings in the `ALLOWED_INPUTS` map in the main class, but you can modify them to
your liking.

Chatter will only translate messages into inputs if the active window is of the allowed window titles. You can modify
the allowed window titles in the `ALLOWED_WINDOW_TITLES` list in the main class. This is done to prevent the application
from sending inputs to the wrong window, which could be dangerous.

## Getting Help

For support or queries, please open an issue in the [Issues section](https://github.com/Foulest/Chatter/issues).
