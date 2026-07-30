// This is a fully independent Gradle/Stonecutter build - see ../settings.gradle.kts for why it's not
// a subproject of the main mod. Build/publish it with `./gradlew -p api build` (or `publish`) from the
// repo root, reusing the root Gradle wrapper.
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

val mcVersions = listOf("1.21.11", "26.1.2", "26.2")

// 1.21.11 predates Minecraft dropping obfuscation (26.1) - it needs the *-remap Loom flavor and its own buildscript.
stonecutter {
    create(rootProject) {
        for (mc in mcVersions) {
            val obfuscated = sc.eval(mc, "<26.1")
            version(mc, mc).buildscript(if (obfuscated) "build-obfuscated.gradle.kts" else "build.gradle.kts")
        }
        vcsVersion = "26.1.2"
    }
}

rootProject.name = "prompt-overlay-api"
