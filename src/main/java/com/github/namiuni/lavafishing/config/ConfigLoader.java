package com.github.namiuni.lavafishing.config;

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.UncheckedIOException;
import java.nio.file.Path;

@NullMarked
@ApiStatus.Internal
public final class ConfigLoader {

    private static final String PRIMARY_CONFIG_FILE_NAME = "config.conf";

    private final ComponentLogger logger;
    private final Path dataDirectory;

    private @MonotonicNonNull PrimaryConfig primaryConfig = null;

    public ConfigLoader(
            final ComponentLogger logger,
            final Path dataDirectory
    ) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    public void reloadPrimaryConfig() {
        this.logger.info("Reloading {}...", PRIMARY_CONFIG_FILE_NAME);

        try {
            this.primaryConfig = this.loadConfig();
            this.logger.info("Successfully reloaded {}", PRIMARY_CONFIG_FILE_NAME);
        } catch (final ConfigurateException exception) {
            this.logger.error("Failed to reload {}, see above for further details", PRIMARY_CONFIG_FILE_NAME, exception);
        }
    }

    public PrimaryConfig primaryConfig() {
        if (this.primaryConfig != null) {
            return this.primaryConfig;
        }

        this.logger.info("Loading {}...", PRIMARY_CONFIG_FILE_NAME);
        try {
            this.primaryConfig = this.loadConfig();
            return this.primaryConfig;
        } catch (final ConfigurateException exception) {
            throw new UncheckedIOException("Failed to initialize %s, see above for further details".formatted(PRIMARY_CONFIG_FILE_NAME), exception);
        }
    }

    private ConfigurationLoader<CommentedConfigurationNode> createYamlLoader(final Path file) {
        return HoconConfigurationLoader.builder()
                .prettyPrinting(true)
                .defaultOptions(options -> options
                        .shouldCopyDefaults(true)
                        .serializers(builder -> builder.registerAll(ConfigurateComponentSerializer.configurate().serializers())))
                .path(file)
                .build();
    }

    private PrimaryConfig loadConfig() throws ConfigurateException {
        final Path file = this.dataDirectory.resolve(ConfigLoader.PRIMARY_CONFIG_FILE_NAME);
        final ConfigurationLoader<CommentedConfigurationNode> loader = this.createYamlLoader(file);

        final CommentedConfigurationNode node = loader.load();
        final PrimaryConfig config = node.get(PrimaryConfig.class);
        if (config == null) {
            throw new ConfigurateException(node, "Failed to deserialize " + PrimaryConfig.class.getName());
        }
        node.set(PrimaryConfig.class, config);
        loader.save(node);
        return config;
    }
}
