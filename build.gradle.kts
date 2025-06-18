import xyz.jpenilla.resourcefactory.bukkit.Permission.Default

version = "1.1.0"

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta16"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.kyori.indra.licenser.spotless") version "3.1.3"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

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
    implementation("net.kyori:adventure-serializer-configurate4:4.22.0")
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    property("name", rootProject.name)
    property("author", paperPluginYaml.author)
    property("contributors", paperPluginYaml.contributors)
}

paperPluginYaml {
    main = "com.github.namiuni.lavafishing.LavaFishing"
    bootstrapper = "com.github.namiuni.lavafishing.LavaFishingBootstrap"
    version = rootProject.version.toString()
    author = "NamiUni"
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
