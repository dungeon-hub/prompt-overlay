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
    }
}

// NOTE: :api is NOT included here as a branch/subproject. Fabric Loom uses cross-project shared
// build services (e.g. JarManifestService) that get corrupted (ClassCastException) whenever Loom is
// applied to two sibling projects targeting the same Minecraft version within a single Gradle
// invocation - which any "1.21.11" + "1.21.11-api" pair would be, regardless of whether they depend
// on each other. api/ is instead its own fully independent Stonecutter/Gradle build - see
// api/settings.gradle.kts - built and published separately (`./gradlew -p api build`). This mod still
// compiles api/src directly (see build.gradle.kts) so there's no source duplication, just two
// separate Gradle invocations reading the same files.

rootProject.name = "prompt-overlay"
