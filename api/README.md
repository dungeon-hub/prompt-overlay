# Prompt Overlay API

The Prompt Overlay API lets Fabric mods show custom prompts that use the keybindings configured by Prompt Overlay. An integration can remain optional: `setOverlay` reports whether the prompt was queued, Prompt Overlay is absent, or an error prevented the prompt from being queued, so the caller can use its normal fallback.

## Add the dependency

The artifact is published to Maven Central. Keep the API version separate from your mod version:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    include(implementation("net.dungeon-hub.prompt-overlay:api:${minecraft_version}-${promptOverlayApiVersion}"))
}
```

`include` makes optional integration straightforward because the small API is available even when Prompt Overlay is not installed. A shadowed JAR works too. If Prompt Overlay is a required dependency, use `implementation` without `include` and declare it in `depends` instead.

For optional integration, advertise Prompt Overlay in `fabric.mod.json`:

```json
{
  "suggests": {
    "prompt-overlay": "*"
  }
}
```

## Create and show an overlay

This example uses the built-in two-action layout and limits the prompt lifetime because a trade request may expire:

```kotlin
import kotlin.time.Duration.Companion.seconds
import net.dungeonhub.promptoverlay.PromptOverlayApi
import net.dungeonhub.promptoverlay.api.SetOverlayResult
import net.dungeonhub.promptoverlay.api.render.*
import net.minecraft.network.chat.Component
import java.awt.Color

class TradeRequestOverlay : TwoActionsOverlay, AcceptableOverlay, DeniableOverlay {
    override val borderColor = Color.CYAN
    override val message = Component.literal("Trade request")
    override val description = Component.literal("Player sent you a trade request!")
    override val maxDisplayDuration = 15.seconds
    override val firstText get() = "[${acceptKey()}] Accept"
    override val secondText get() = "[${denyKey()}] Deny"

    override fun accept() {
        // Accept the request.
    }

    override fun deny() {
        // Deny the request.
    }

    override fun dismiss() {
        // Optional action when the overlay is dismissed.
    }
}

when (val result = PromptOverlayApi.setOverlay(TradeRequestOverlay())) {
    SetOverlayResult.Queued -> Unit
    SetOverlayResult.ModNotInstalled -> {
        // Preserve the original chat prompt or use another fallback.
    }
    is SetOverlayResult.Error -> {
        // Prompt Overlay reports the error to the user. The cause is also
        // available as result.throwable if the integration needs to inspect it.
    }
}
```

Call `setOverlay` where the prompt originates. Its result is one of:

- `SetOverlayResult.Queued`: the overlay handler accepted and queued the prompt.
- `SetOverlayResult.ModNotInstalled`: Prompt Overlay is not installed. Use the calling mod's normal fallback.
- `SetOverlayResult.Error`: Prompt Overlay is installed, but the prompt could not be queued. Prompt Overlay logs the cause, shows an error in chat, and exposes the cause through `throwable`.

Because `ModNotInstalled` is a normal result, optional integrations do not need to check Fabric Loader before calling `setOverlay`. Handle both `ModNotInstalled` and `Error` if the original prompt should remain available whenever the overlay was not queued.

`description` is always a `Component`. Overlays that do not need a description can omit the
property, which then uses the default `Component.empty()`.

## Prompt lifetime

Every `Overlay` must provide `message` and `borderColor`. It can also provide a maximum lifetime when the underlying prompt expires:

| Property             | Default | Purpose                                                                                                                                                                                                       |
|----------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `maxDisplayDuration` | `null`  | Positive, finite upper bound for automatic dismissal. The effective duration is the shorter of this limit and the user's configured duration. `null` or an invalid duration applies no prompt-specific limit. |

Only set this property when the prompt stops being actionable after a known period. It never makes an overlay remain visible longer than the user's preference.

## Overlay interfaces

- `Overlay`: base rendering and lifecycle contract.
- `AcceptableOverlay` and `DeniableOverlay`: accept/deny callbacks and configured key names.
- `ZeroActionsOverlay`: informational layout with only the configured dismiss action.
- `OneActionOverlay` and `TwoActionsOverlay`: layouts for one or two actions plus dismiss.
- `OneOptionOverlay` and `TwoOptionsOverlay`: selection callbacks mixed into action layouts.
- `ThreeActionsOverlay`, `FourActionsOverlay`, and `FiveActionsOverlay`: multi-choice layouts with configured number-key names.

The action templates render labels but do not perform actions themselves. Implement the corresponding callback (`accept`, `deny`, `firstOption`, and so on); Prompt Overlay invokes it when the configured key is pressed.

### Informational overlays

Use `ZeroActionsOverlay` when a prompt only presents information and does not offer a custom action. It renders a single dismiss label using the user's configured dismiss key, so no action text properties or action callback interfaces are required:

```kotlin
class InformationOverlay : ZeroActionsOverlay {
    override val borderColor = Color.YELLOW
    override val message = Component.literal("Information")
    override val description = Component.literal("The event has started.")

    override fun dismiss() {
        // Optional cleanup when the user dismisses the overlay.
    }
}

PromptOverlayApi.setOverlay(InformationOverlay())
```

The `dismiss` implementation is optional. Prompt Overlay removes the overlay after the configured dismiss key is pressed regardless of whether the callback is overridden.
