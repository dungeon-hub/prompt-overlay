pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    // Check the latest version on https://stonecutter.kikugie.dev/blog/changes/0.9
    id("dev.kikugie.stonecutter") version "0.9.7"
}

val mcVersions = listOf("1.21.11", "26.1.2", "26.2")

// 1.21.11 predates Minecraft dropping obfuscation (26.1) - it needs the *-remap Loom flavor and its own buildscript.
fun dev.kikugie.stonecutter.settings.tree.BranchBuilder.registerVersions() {
    for (mc in mcVersions) {
        val obfuscated = sc.eval(mc, "<26.1")
        version(mc, mc).buildscript(if (obfuscated) "build-obfuscated.gradle.kts" else "build.gradle.kts")
    }
}

stonecutter {
    create(rootProject) {
        registerVersions()
        vcsVersion = "26.1.2"

        // NOTE: :api is intentionally NOT a separate Stonecutter branch/project here.
        // Fabric Loom's Minecraft setup crashes (ClassCastException on LoomGradleExtension) when two
        // sibling projects in the same Gradle invocation target the exact same Minecraft version - which
        // is exactly what a "1.21.11" + "1.21.11-api" pair would be. Instead, api/src is compiled as an
        // extra source directory inside each main build script, and published as a second, filtered jar.
        // See build.gradle.kts / build-obfuscated.gradle.kts.
    }
}

rootProject.name = "prompt-overlay"
