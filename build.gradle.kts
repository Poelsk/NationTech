import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.plugins
import org.gradle.kotlin.dsl.shadow

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
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.Indyuce.dev/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://nexus.frengor.com/repository/public/")
    maven("https://repo.purpurmc.org/snapshots/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.phoenix616.dev/")
    maven("https://repo.townyadvanced.com/repository/maven-public/") // Towny Repo
}

dependencies {
    // APIs proporcionadas por el servidor
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.purpurmc.purpur:purpur-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")

    // Dependencias esenciales de Mythic Tree & MMOItems
    compileOnly("io.lumine:Mythic-Dist:5.9.0")
    compileOnly("net.Indyuce:MMOItems:6.7.3")

    // Plugins comunes
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly("org.bstats:bstats-bukkit:3.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    // Librerías empaquetadas
    implementation("com.zaxxer:HikariCP:5.0.1")

    // ACF (comandos)
    compileOnly("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    // ACF locales: se incluye automáticamente con acf-paper; no es necesario dependencia separada

    // Other missing dependencies
    compileOnly("com.github.TownyAdvanced:Towny:0.101.1.10")
    compileOnly("com.frengor:ultimateadvancementapi:2.5.1")
    compileOnly("net.wesjd:anvilgui:1.10.6-SNAPSHOT")
// https://mvnrepository.com/artifact/com.cjcrafter/foliascheduler
    implementation("com.cjcrafter:foliascheduler:0.7.0")
}

// Configurar la tarea ShadowJar
tasks.named<ShadowJar>("shadowJar") {
    relocate("com.zaxxer.hikari", "com.github.nationTech.libs.hikaricp")
    relocate("org.slf4j", "com.github.nationTech.libs.slf4j")
    relocate("org.bstats", "com.github.nationTech.libs.bstats")
    relocate("me.clip.placeholderapi", "com.github.nationTech.libs.placeholderapi")
    relocate("co.aikar.commands", "com.github.nationTech.libs.acf")
    relocate("net.wesjd.anvilgui", "com.github.nationTech.libs.anvilgui")
    relocate("com.cjcrafter.foliascheduler", "com.github.nationTech.libs.foliascheduler")
    minimize()
}

// Make build depend on shadowJar
tasks.named("build") {
    dependsOn("shadowJar")
}