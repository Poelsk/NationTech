import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.nationtech"
version = "0.1.1-BETA"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://jitpack.io") // <<-- REAÑADIDO: Repositorio de JitPack
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    // <<-- CAMBIO CLAVE: Volviendo a la dependencia de Towny a través de JitPack
    compileOnly("com.github.TownyAdvanced:Towny:0.101.1.10")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    relocate("com.google.common", "com.nationtech.libs.google.common")
}

tasks.processResources {
    from(sourceSets.main.get().resources.srcDirs) {
        expand(
            mutableMapOf(
                "version" to project.version,
                "name" to project.name,
                "main" to "com.nationtech.NationTech"
            )
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.shadowJar {
    dependsOn(tasks.processResources)
}

tasks.jar {
    archiveFileName.set("${project.name}-${project.version}.jar")
}