# Prompt Overlay API

API for creating custom overlays that integrate with the Prompt Overlay mod. Your mod users will be able to interact with your overlays using the keybindings configured in Prompt Overlay.

## Usage

**1. Add the dependency to your `build.gradle.kts`:**

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    include(implementation("net.dungeon-hub.prompt-overlay:api:${minecraft_version}-${version}"))
}
```

> We are using `include` here for simplicity. You are free to use a shadowed JAR as well.

Optionally, include it in your `suggests` block of your `fabric.mod.json` to show users that they can use this mod:

```
"suggests": {
    "prompt-overlay": "*"
}
```

> Note: This guide focuses on you adding this mod as an optional dependency.\
> Due to this, we're including the API code in your JAR, so that you can prompt an overlay without users having the mod installed.\
> If you require this mod for your mod to function, include it in your `depends` in your `fabric.mod.json` instead and only use `implementation()` (instead of `include(implementation())`) here.

**2. Create your custom overlay:**

```kotlin
import net.dungeonhub.promptoverlay.api.render.*
import java.awt.Color

class MyCustomOverlay : TwoActionsOverlay, AcceptableOverlay, DeniableOverlay {
    override val borderColor = Color.CYAN
    override val message = "Player sent you a trade request!"
    override val firstText = "[${acceptKey()}] Accept"
    override val secondText = "[${denyKey()}] Deny"
    
    override fun accept() {
        // Handle accept action
    }
    
    override fun deny() {
        // Handle deny action
    }
}
```

**3. Register your overlay with Prompt Overlay's rendering system**

```kotlin
import net.dungeonhub.promptoverlay.PromptOverlayApi

PromptOverlayApi.setOverlay(MyCustomOverlay())
```

You add this piece of code in the place where you want your users to get a popup to accept or deny a request - and that's it!

The API package you included in step 1 already handles all cases: If the mod isn't (properly) loaded, the `setOverlay()` method returns false, in case you need to handle the confirmation in another way.
In case there are errors, the user will be notified in the chat, and the method returns false as well, to make sure that your mod can properly use the fallback solution.
Those errors should usually not happen, and are rare edge-cases of using a modified version of the API or me accidentally not implementing a new version with backwards-compatibility in mind.


## Available Overlay Types

- **`Overlay`**: Base interface for all overlays
- **`AcceptableOverlay`**: Adds accept action capability
- **`DeniableOverlay`**: Adds deny action capability  
- **`OneActionOverlay`**: Template for overlays with one custom action (accept or deny) + dismiss
- **`TwoActionsOverlay`**: Template for overlays with two custom actions (accept and deny) + dismiss
