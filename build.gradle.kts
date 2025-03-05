import xyz.jpenilla.resourcefactory.bukkit.Permission.Default

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta8"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.2.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.spongepowered:configurate-hocon:4.2.0")
    implementation("net.kyori:adventure-serializer-configurate4:4.19.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

paperPluginYaml {
    main = "com.github.namiuni.lavafishing.LavaFishing"
    bootstrapper = "com.github.namiuni.lavafishing.LavaFishingBootstrap"
    version = rootProject.version.toString()
    authors.add("NamiUni")
    apiVersion = "1.21"

    permissions {
        register("lavafishing.command.reload") {
            description = "Reloads LavaFishing's config."
            default = Default.OP
        }

        register("lavafishing.play.fishing") {
            description = "Play lava fishing."
            default = Default.TRUE
        }
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
        downloadPlugins {
            url("https://download.luckperms.net/1573/bukkit/loader/LuckPerms-Bukkit-5.4.156.jar")
        }
    }

    shadowJar {
        archiveClassifier = null as String?
    }
}
