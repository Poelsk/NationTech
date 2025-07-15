// settings.gradle.kts

rootProject.name = "NationTech"

pluginManagement {
    repositories {
        gradlePluginPortal()  // para plugins Gradle
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://mvn.lumine.io/repository/maven-public/")  // Mythic & Mythic-Dist
        maven("https://repo.Indyuce.dev/repository/maven-public/") // MMOItems
        maven("https://repo.purpurmc.org/snapshots/")         // Purpur API
        maven("https://repo.aikar.co/content/groups/aikar/") // ACF & Locales
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://nexus.frengor.com/repository/public/")
        maven("https://repo.phoenix616.dev/")              // Alternative PlaceholderAPI
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://repo.Indyuce.dev/repository/maven-public/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://nexus.frengor.com/repository/public/")
        maven("https://repo.purpurmc.org/snapshots/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://repo.phoenix616.dev/")
    }
}