# Prompt Overlay API

The Prompt Overlay API lets Fabric mods show custom prompts that use the keybindings configured by Prompt Overlay. An integration can remain optional: when Prompt Overlay is absent or rejects the overlay, `setOverlay` returns `false` so the caller can use its normal fallback.

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

For example, the API version in this repository produces version `26.1.2-0.2.0`.

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

val shown = PromptOverlayApi.setOverlay(TradeRequestOverlay())
if (!shown) {
    // Preserve the original chat prompt or use another fallback.
}
```

Call `setOverlay` where the prompt originates. Prompt Overlay catches integration errors, reports them to the user, and returns `false` rather than requiring an installed-mod check in the caller.

`description` is always a `Component`. Overlays that do not need a description can omit the
property, which then uses the default `Component.empty()`.

Call `setOverlay` where the prompt originates. Prompt Overlay catches integration errors, reports them to the user, and returns `false` rather than requiring an installed-mod check in the caller.

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
