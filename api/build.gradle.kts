import net.thebugmc.gradle.sonatypepublisher.PublishingType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Used for every unobfuscated target (26.1+). See build-obfuscated.gradle.kts for 1.21.11.
plugins {
    kotlin("jvm") version "2.3.21"
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

group = "net.dungeon-hub.prompt-overlay"
val artifactId = property("api.id") as String
version = "${sc.current.version}-${property("api.version")}"
description = "API for the Prompt Overlay mod - allows other mods to create custom overlays"
base.archivesName.set(artifactId)

val targetJavaVersion = 25
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    mavenCentral()
}

fun dep(key: String): String = sc.properties["deps.$key"] as String

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")

    compileOnly("net.fabricmc:fabric-loader:${dep("fabric_loader")}")
    compileOnly("net.fabricmc:fabric-language-kotlin:${dep("fabric_language_kotlin")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

kotlin {
    jvmToolchain(targetJavaVersion)
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

centralPortal {
    name = artifactId
    publishingType = PublishingType.USER_MANAGED

    pom {
        name = artifactId
        description = project.description
        url = "https://github.com/dungeon-hub/prompt-overlay"

        organization {
            name = "Dungeon Hub"
            url = "https://dungeon-hub.net/"
        }

        scm {
            url = "https://github.com/dungeon-hub/prompt-overlay"
            connection = "scm:git://github.com:dungeon-hub/prompt-overlay.git"
            developerConnection = "scm:git://github.com:dungeon-hub/prompt-overlay.git"
        }

        developers {
            developer {
                id = "taubsie"
                name = "Taubsie"
                email = "taubsie@dungeon-hub.net"
                url = "https://github.com/Taubsie/"
                organizationUrl = "https://dungeon-hub.net/"
            }
        }

        licenses {
            license {
                name = "EUPL-1.2"
                url = "https://eupl.eu/1.2/en/"
            }
        }
    }
}
