import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.nationTech"
version = "0.1.3-BETA"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    // Repositorio para PlaceholderAPI y bStats
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    // API del Servidor
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")

    // Dependencias de otros plugins (Hard-depends)
    compileOnly("com.github.TownyAdvanced:Towny:0.101.1.10")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // Dependencias que SÍ se empaquetan y relocalizan en tu plugin
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("org.bstats:bstats-bukkit:3.0.2")
// https://mvnrepository.com/artifact/com.github.placeholderapi/placeholderapi
    implementation("com.github.placeholderapi:placeholderapi:2.10.9")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("all")

        // Relocalizamos las librerías para evitar conflictos.
        relocate("com.zaxxer.hikari", "com.github.nationTech.libs.hikaricp")
        relocate("org.slf4j", "com.github.nationTech.libs.slf4j")
        relocate("org.bstats", "com.github.nationTech.libs.bstats")
        // Relocalización CRÍTICA para PlaceholderAPI, tal como lo indica la wiki
        relocate("me.clip.placeholderapi", "com.github.nationTech.libs.placeholderapi")

        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}