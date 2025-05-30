# Chatter

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![CodeQL](https://github.com/Foulest/Chatter/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/Foulest/Chatter/actions/workflows/github-code-scanning/codeql)

> **Note:** Support for this project has been officially dropped as of May 23, 2025.

**Chatter** is a Twitch chat utility that translates chat messages into key presses.

## Usage

Chatter's main usage is allowing Twitch chat to play video games like Pokemon by themselves, but it can also be
used for other purposes. Chatter also has a random inputs mode that will randomly press keys and move the mouse
around the screen. This mode does not require Twitch chat to function.

In order to start using Chatter, you will need to clone the repository and run the `main` method in the `Chatter` class.

After inputting your **[OAuth Token](https://twitchapps.com/tmi/)** and the channel you want to listen to, Chatter will
ask you to select one of the supported applications. These are customizable in the `APPLICATIONS` list in the main
class. After that, Chatter will start listening to chat and sending inputs to the application's active window
(and only the active window).

Chatter supports case-sensitive inputs for chatters to specify input duration: **uppercase** messages hold buttons down
for one second and doubles mouse movement distance; **lowercase** messages press buttons once and move the mouse at
half the distance.

## Getting Help

For support or queries, please open an issue in the [Issues section](https://github.com/Foulest/Chatter/issues).
