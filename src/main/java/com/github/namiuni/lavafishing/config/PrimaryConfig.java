package com.github.namiuni.lavafishing.config;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@NullMarked
@ApiStatus.Internal
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class PrimaryConfig {

    @Comment("""
            Enable PlayerFishEvent calls in this plugin. https://jd.papermc.io/paper/1.21.4/org/bukkit/event/player/PlayerFishEvent.html
            Enabling this option may improve compatibility with other fishing plugins.
            WARNING: WARNING: Do not report any issues caused by enabling this option to developers of other plugins.
                     Only enable this option if you understand its effects.
            """)
    private boolean callBukkitEvent = false;

    @Comment("Lava Hook Settings")
    private LavaHookSettings lavaHookSettings = new LavaHookSettings(
            Key.key("minecraft:smoke"),
            Key.key("minecraft:dripping_lava"),
            Key.key("minecraft:dripping_lava"),
            Key.key("minecraft:item.bucket.fill_lava"),
            Key.key("minecraft:entity.fishing_bobber.splash"),
            Key.key("minecraft:gameplay/fishing")
    );

    public boolean callBukkitEvent() {
        return this.callBukkitEvent;
    }

    public LavaHookSettings lavaHookSettings() {
        return this.lavaHookSettings;
    }

    @ConfigSerializable
    public record LavaHookSettings(
            @Comment("Particle effect for bobbing hooks.") Key bubbleParticle,
            @Comment("Particle effect for lured fish.") Key fishingParticle,
            @Comment("Particle effect for splashing hooks.") Key splashParticle,
            @Comment("Sound effect for a floating hook.") Key floatSound,
            @Comment("Sound effect for a fish biting.") Key biteSound,
            @Comment("Loot table used for randomly selecting fish during lava fishing.") Key lootTable
    ) {}
}
