import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Used only for 1.21.11 - the last Minecraft release before obfuscation was dropped (26.1+ uses build.gradle.kts).
plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("com.gradleup.shadow") version "9.4.2"
    id("maven-publish")
}

version = property("mod.version") as String
group = property("mod.group") as String

base {
    archivesName.set(property("mod.id") as String)
}

val targetJavaVersion = 21

java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}

// The API module lives in api/src and is published as its own "prompt-overlay-api" jar for other mods
// to compile against. It's compiled as part of this project (not a separate Stonecutter project) because
// Fabric Loom's Minecraft setup crashes when two sibling projects target the same Minecraft version.
// Manually sc.process() each file since it's outside Stonecutter's own src/main tree, so it wouldn't
// otherwise get the version replacements (see stonecutter.gradle.kts) applied to it.
val apiSourceRoot = rootDir.resolve("api/src/main/kotlin")
val processedApiSourceRoot = layout.projectDirectory.dir("generated/apiSrc").asFile
apiSourceRoot.walkTopDown().filter { it.isFile }.forEach { file ->
    val relativePath = file.relativeTo(apiSourceRoot).path.replace('\\', '/')
    sc.process(file, "generated/apiSrc/$relativePath")
}

sourceSets {
    main {
        kotlin.srcDir(processedApiSourceRoot)
    }
}

loom {

}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        content {
            includeGroup("me.djtheredstoner")
        }
    }

    maven("https://maven.teamresourceful.com/repository/maven-public/") {
        name = "Team Resourceful Maven"
        content {
            includeGroup("com.teamresourceful.resourcefulconfig")
            includeGroup("com.teamresourceful.resourcefulconfigkt")
        }
    }
}

fun dep(key: String): String = sc.properties["deps.$key"] as String

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${dep("fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${dep("fabric_language_kotlin")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${dep("fabric_api")}")

    // Published against intermediary like any other Fabric mod jar, so this needs remapping too.
    modImplementation("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-${dep("resourcefulconfig_artifact")}:${dep("resourcefulconfig")}")
    // Also references Identifier (see TypeBuilders.kt's renderer property), so it needs remapping too.
    val resourcefulConfigKt = "com.teamresourceful.resourcefulconfigkt:${dep("resourcefulconfigkt_artifact")}:${dep("resourcefulconfigkt")}"
    modImplementation(resourcefulConfigKt)
    shadow(resourcefulConfigKt) { // TODO just use the dependency on the mod once (if) it becomes available on Modrinth
        isTransitive = false
    }

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")

    //Testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    testImplementation(kotlin("test"))
}

tasks.processResources {
    val modVersion = project.property("mod.version") as String
    val minecraftVersion = sc.current.version
    val loaderVersion = dep("fabric_loader")
    val kotlinLoaderVersion = dep("fabric_language_kotlin")
    val resourcefulConfigVersion = dep("resourcefulconfig")
    val mixinJavaCompat = "JAVA_$targetJavaVersion"

    inputs.property("version", modVersion)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("loader_version", loaderVersion)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to modVersion,
            "minecraft_version" to minecraftVersion,
            "loader_version" to loaderVersion,
            "kotlin_loader_version" to kotlinLoaderVersion,
            "resourcefulconfig_version" to resourcefulConfigVersion
        )
    }

    filesMatching("*.mixins.json") {
        expand("java_compatibility_level" to mixinJavaCompat)
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("$rootDir/LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

// Configure shadow jar to bundle dependencies
tasks.shadowJar {
    archiveClassifier.set("") // No classifier so it becomes the main jar
    configurations = listOf(project.configurations.shadow.get())

    // Exclude ONLY libraries provided by fabric-language-kotlin
    exclude("org/jetbrains/kotlin/**")
    exclude("kotlin/**")
    exclude("kotlinx/coroutines/**")
    exclude("kotlinx/serialization/**")
    exclude("kotlinx/atomicfu/**")
    exclude("kotlinx/datetime/**")
    exclude("kotlinx/io/**")

    // Exclude unnecessary libraries
    exclude("io/swagger/**")
    exclude("org/jetbrains/annotations/**")
    exclude("org/intellij/**")
    exclude("com/google/errorprone/**")
    exclude("org/slf4j/**")

    // Relocate ALL other dependencies to avoid conflicts
    relocate("com.squareup.moshi", "net.dungeonhub.promptoverlay.libs.moshi")
    relocate("com.squareup.okio", "net.dungeonhub.promptoverlay.libs.okio")
    relocate("okio", "net.dungeonhub.promptoverlay.libs.okio")
    relocate("okhttp3", "net.dungeonhub.promptoverlay.libs.okhttp3")
    relocate("io.ktor", "net.dungeonhub.promptoverlay.libs.ktor")
    relocate("com.hypercubetools", "net.dungeonhub.promptoverlay.libs.hypercubetools")
    relocate("com.google.gson", "net.dungeonhub.promptoverlay.libs.gson")
    relocate("com.fasterxml.jackson", "net.dungeonhub.promptoverlay.libs.jackson")
    relocate("jakarta", "net.dungeonhub.promptoverlay.libs.jakarta")
    relocate("org.apache.commons", "net.dungeonhub.promptoverlay.libs.apache.commons")
    relocate("org.yaml.snakeyaml", "net.dungeonhub.promptoverlay.libs.snakeyaml")
    relocate("io.github.oshai.kotlinlogging", "net.dungeonhub.promptoverlay.libs.kotlinlogging")
    relocate("com.teamresourceful.resourcefulconfigkt", "net.dungeonhub.promptoverlay.libs.resourcefulconfigkt")

    from("$rootDir/LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }

    // remapJar below does the final classifier + remap pass; this is just its (intermediary-named) input.
    archiveClassifier = "dev"
}

// Disable the regular jar task, use shadow jar (remapped below) instead.
tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

// Unlike the unobfuscated build, 1.21.11 still needs a remap pass from intermediary to Mojang mappings,
// so shadowJar's output ("dev" jar above) has to go through remapJar to produce the final artifact.
tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    archiveClassifier.set(sc.current.version)
}

tasks.build {
    dependsOn(tasks.remapJar)
}

val apiId = property("api.id") as String
val apiVersion = "${sc.current.version}-${property("api.version")}"

// Thin jar containing only the api/src classes, so other mods can depend on the API without the whole mod.
val apiJar = tasks.register<Jar>("apiJar") {
    archiveBaseName.set(apiId)
    archiveVersion.set(apiVersion)
    from(sourceSets.main.get().output) {
        include("net/dungeonhub/promptoverlay/api/**")
        include("net/dungeonhub/promptoverlay/PromptOverlayApi.class")
    }
}

val apiSourcesJar = tasks.register<Jar>("apiSourcesJar") {
    archiveBaseName.set(apiId)
    archiveVersion.set(apiVersion)
    archiveClassifier.set("sources")
    from(processedApiSourceRoot)
}

tasks.build {
    dependsOn(apiJar, apiSourcesJar)
}

// configure the maven publications
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("mod.id") as String
            from(components["java"])
        }

        create<MavenPublication>("mavenApi") {
            groupId = "net.dungeon-hub.prompt-overlay"
            artifactId = apiId
            version = apiVersion
            artifact(apiJar)
            artifact(apiSourcesJar)
        }
    }

    repositories {
    }
}
