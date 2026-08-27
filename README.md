# Prompt Overlay

Prompt Overlay is a client-side Fabric mod that turns supported Hypixel chat prompts into compact, keyboard-driven overlays. It avoids opening chat to click a response while retaining the commands and choices supplied by the server.

> [!WARNING]
> Prompt Overlay is under active development. Supported prompts and the public API may change before the first stable release.

## Features

- Keyboard controls with configurable bindings for accepting, denying, dismissing, and selecting up to five options.
- Individual feature toggles and configurable display duration, background, and prompt colors.
- Support for friend, party, guild, duel, and SkyBlock trade requests.
- Support for Trophy Fish GG, Catacombs requeue, Abiphone, Trapper, and Dark Auction prompts.
- Support for confirmation prompts and NPC dialogs with up to five choices.
- A public API through which other Fabric mods can display their own prompts.

The project aims to provide a lightweight, intuitive way to interact with common prompts while remaining highly configurable and extensible.

## Configuration

Run `/prompt-overlay` in game to open the configuration screen. Controls can also be changed in Minecraft's keybind settings.

Configuration includes per-feature toggles, overlay duration and colors, ignored Abiphone contacts, and feature-specific timing options.

## Developer API

Other mods can submit custom overlays without requiring Prompt Overlay to be installed. The API supports action and option templates, prompt-specific lifetime limits, and a failure result so integrations can fall back to their original interaction.

See the [API integration guide](api/README.md) for dependency setup, a complete example, and further information.

## Building from source

The Gradle wrapper downloads the required build tooling. A Java 25 JDK is required.

```shell
./gradlew build
```

Build artifacts are written below `build/libs/`; API artifacts are written below `api/build/libs/`.

## Credits

Prompt Overlay was inspired by [Popup Events](https://github.com/Sk1erLLC/PopupEvents/) by Sk1er LLC. It is an independent implementation for modern Minecraft versions with a new codebase and expanded scope.
