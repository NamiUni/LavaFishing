package com.github.namiuni.lavafishing.config;

import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.loot.LootTables;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Set;

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

    @Comment("Worlds that allow lava fishing.")
    private Set<Key> allowWorlds = Set.of(
            NamespacedKey.minecraft("overworld"),
            NamespacedKey.minecraft("the_nether")
    );

    @Comment("Lava Hook Settings.")
    private LavaHookSettings lavaHookSettings = new LavaHookSettings(
            Particle.SMOKE.key(),
            Particle.DRIPPING_LAVA.key(),
            Particle.DRIPPING_LAVA.key(),
            SoundEventKeys.ITEM_BUCKET_FILL,
            SoundEventKeys.ENTITY_FISHING_BOBBER_SPLASH,
            LootTables.FISHING.key()
    );

    public boolean callBukkitEvent() {
        return this.callBukkitEvent;
    }

    public Set<Key> allowWorlds() {
        return this.allowWorlds;
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
