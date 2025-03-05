/*
 * LavaFishing
 *
 * Copyright (c) 2025. NamiUni
 *                     Contributors []
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.namiuni.lavafishing.fishing;

import com.github.namiuni.lavafishing.config.ConfigLoader;
import com.github.namiuni.lavafishing.event.LavaFishStateTransitionEvent;
import com.github.namiuni.lavafishing.config.PrimaryConfig;
import com.github.namiuni.lavafishing.event.LavaHookLootTableRollEvent;
import com.github.namiuni.lavafishing.event.LavaHookStateTransitionEvent;
import com.github.namiuni.lavafishing.util.LavaFishingUtil;
import com.github.namiuni.lavafishing.util.PDCUtil;
import io.papermc.paper.block.fluid.FluidData;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Fishing hook bobbing on lava. Replicates vanilla behavior.
 *
 * <p>As a Minecraft specification, fishing hook ignores velocity packets. Therefore, the hook is made to ride on a invisible entity, and velocity is set on the vehicle entity to simulate bobbing.</p>
 *
 * @see <a href="https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Object_Data#Fishing_Hook">Minecraft Wiki</a>
 */
@NullMarked
@ApiStatus.Experimental
@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class LavaHook {

    // Open Lava Blocks
    private static final Set<Material> VALID_LAVA_BLOCKS = EnumSet.of(Material.AIR, Material.LAVA, Material.LILY_PAD);

    // Constants (physics simulation, particle generation, etc.)
    private static final Vector BUOYANCY = new Vector(0.3, 0.2, 0.3);
    private static final Vector GRAVITY = new Vector(0.0, -0.03, 0.0);
    private static final double WATER_RESISTANCE = 0.92D;
    private static final double HORIZONTAL_DAMPING = 0.9;
    private static final double VERTICAL_ADJUSTMENT_FACTOR = 0.2;
    private static final double MIN_DIFF_THRESHOLD = 0.01;
    private static final double DIFF_ADJUSTMENT = 0.1;
    private static final double BITING_Y_MULTIPLIER = -0.1;

    // FishHook Properties
    private final FishHook paperHook;
    private @Nullable Entity bobbingVehicle = null;
    private final Player player;
    private final Supplier<Integer> minWaitTime;
    private final Supplier<Integer> maxWaitTime;
    private final Supplier<Integer> minLureTime;
    private final Supplier<Integer> maxLureTime;
    private final Supplier<Float> minLureAngle;
    private final Supplier<Float> maxLureAngle;
    private final Supplier<Boolean> applyLure;
    private final Supplier<Boolean> rainInfluenced;
    private final Supplier<Boolean> skyInfluenced;
    // TODO: 将来的に luck / lureSpeed のロジックを実装
    private final int luckBonus;
    private final int lureSpeed;

    // Plugin Settings
    private final boolean callBukkitEvent;
    private final Particle bubbleParticle;
    private final Particle fishingParticle;
    private final Particle splashParticle;
    private final org.bukkit.Sound floadSound;
    private final org.bukkit.Sound biteSound;
    private final LootTable lootTable;

    // LavaHook state
    private FishHook.HookState currentState;
    private boolean biting;
    private int outOfLavaTime;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean openLava = true;

    private LavaHook(final FishHook paperHook, ConfigLoader configLoader) {
        this.paperHook = Objects.requireNonNull(paperHook);
        this.player = (Player) Objects.requireNonNull(paperHook.getShooter());

        // Paper FishHook Properties
        this.minWaitTime = paperHook::getMinWaitTime;
        this.maxWaitTime = paperHook::getMaxWaitTime;
        this.minLureTime = paperHook::getMinLureTime;
        this.maxLureTime = paperHook::getMaxLureTime;
        this.minLureAngle = paperHook::getMinLureAngle;
        this.maxLureAngle = paperHook::getMaxLureAngle;
        this.applyLure = paperHook::getApplyLure;
        this.rainInfluenced = paperHook::isRainInfluenced;
        this.skyInfluenced = paperHook::isSkyInfluenced;
        final ItemStack fishingRod = player.getInventory().getItemInMainHand();
        this.luckBonus = fishingRod.getEnchantmentLevel(Enchantment.LUCK_OF_THE_SEA);
        this.lureSpeed = fishingRod.getEnchantmentLevel(Enchantment.LURE) * 100;

        // Plugin Settings
        final PrimaryConfig config = configLoader.primary();
        this.callBukkitEvent = config.callBukkitEvent();

        final Registry<Particle> particles = RegistryAccess.registryAccess().getRegistry(RegistryKey.PARTICLE_TYPE);
        this.bubbleParticle = particles.getOrThrow(config.lavaHookSettings().bubbleParticle());
        this.fishingParticle = particles.getOrThrow(config.lavaHookSettings().fishingParticle());
        this.splashParticle = particles.getOrThrow(config.lavaHookSettings().splashParticle());

        final Registry<org.bukkit.Sound> sounds = RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT);
        this.floadSound = sounds.getOrThrow(config.lavaHookSettings().floatSound());
        this.biteSound = sounds.getOrThrow(config.lavaHookSettings().biteSound());

        this.lootTable = Registry.LOOT_TABLES.getOrThrow(config.lavaHookSettings().lootTable()).getLootTable();

        // LavaHook state
        LavaFishingUtil.markLavaHook(this.paperHook);
        this.currentState = paperHook.getState();
    }

    static LavaHook create(final FishHook fishHook, final ConfigLoader configLoader) {
        return new LavaHook(fishHook, configLoader);
    }

    /**
     * Get the paper fish hook.
     *
     * @return the paepr fish hook
     * @since 1.0.0
     */
    public FishHook getPaperFishHook() {
        return this.paperHook;
    }

    /**
     * Get the lava hook owner.
     *
     * @return the hook owner
     * @since 1.0.0
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Get the hook vehicle.
     *
     * <p>Vehicle is null until it lands on the lava</p>
     *
     * @return the lava hook vehicle
     * @since 1.0.0
     */
    public @Nullable Entity getBobbingVehicle() {
        return this.bobbingVehicle;
    }

    /**
     * Get the hook state.
     *
     * @return the hook state.
     * @since 1.0.0
     */
    public FishHook.HookState getHookState() {
        return this.currentState;
    }

    boolean wind(final EquipmentSlot hand) {

        boolean overridePaperEvent = false;

        if (this.nibble > 0) {
            final LootContext lootContext = new LootContext
                    .Builder(this.paperHook.getLocation())
                    .lootedEntity(this.paperHook)
                    .killer(this.player)
                    .luck((float) (this.luckBonus + Objects.requireNonNull(this.player.getAttribute(Attribute.LUCK)).getValue()))
                    .build();
            final Collection<ItemStack> selectItems = this.lootTable.populateLoot(null, lootContext);
            new LavaHookLootTableRollEvent(this, selectItems).callEvent();

            for (final ItemStack itemStack : selectItems) {
                final Item itemEntity = this.paperHook.getWorld().createEntity(this.paperHook.getLocation(), Item.class);
                final double dx = this.player.getX() - this.paperHook.getX();
                final double dy = this.player.getY() - this.paperHook.getY();
                final double dz = this.player.getZ() - this.paperHook.getZ();
                final Vector velocity = new Vector(dx * 0.1, dy * 0.1 + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * 0.1);
                itemEntity.setVelocity(velocity);
                itemEntity.setItemStack(itemStack);

                int exp = ThreadLocalRandom.current().nextInt(6) + 1;
                if (this.callBukkitEvent) {
                    overridePaperEvent = true;
                    final PlayerFishEvent fishEvent = new PlayerFishEvent(this.player, itemEntity, this.paperHook, hand, PlayerFishEvent.State.CAUGHT_FISH); // WARNING: Bukkit events should not be called.
                    fishEvent.setExpToDrop(exp);
                    if (!fishEvent.callEvent()) {
                        itemEntity.remove();
                        continue;
                    }
                    exp = fishEvent.getExpToDrop();
                }

                itemEntity.spawnAt(this.paperHook.getLocation());

                if (0 < exp) {
                    final Location orbLocation = this.player.getLocation().add(0.0, 0.5, 0.5);
                    final int resultExp = exp;
                    this.player.getWorld().spawn(orbLocation, ExperienceOrb.class, orb -> orb.setExperience(resultExp));
                }

                if (LavaFishingUtil.isFishItem(itemStack)) {
                    this.player.incrementStatistic(Statistic.FISH_CAUGHT, 1);
                }
            }

            this.paperHook.remove();
            this.player.getInventory().getItem(hand).damage(1, this.player);
        }

        return overridePaperEvent;
    }

    boolean tick() {
        if (this.paperHook.isDead()) {
            return false;
        }

        return switch (this.paperHook.getState()) {
            case BOBBING -> {
                if (this.bobbingVehicle != null) {
                    this.bobbingVehicle.remove();
                }
                yield false;
            }
            case HOOKED_ENTITY -> {
                new LavaHookStateTransitionEvent(this, FishHook.HookState.HOOKED_ENTITY).callEvent();
                this.currentState = FishHook.HookState.HOOKED_ENTITY;
                yield true;
            }
            case UNHOOKED -> {
                final Location hookLocation = this.paperHook.getLocation();
                final FluidData fluidData = this.paperHook.getWorld().getFluidData(hookLocation);

                switch (this.currentState) {
                    case UNHOOKED -> handleUnhookedState(fluidData);
                    case BOBBING -> handleBobbingState(fluidData, this::fishing);
                    case HOOKED_ENTITY -> throw new IllegalStateException("Unexpected HOOKED_ENTITY state in stateTransition.");
                }
                yield true;
            }
        };
    }

    private void handleUnhookedState(final FluidData fluidData) {
        if (!LavaFishingUtil.isLavaFluid(fluidData)) {
            return;
        }
        final Sound sound = Sound.sound(
                this.floadSound,
                Sound.Source.NEUTRAL,
                1.0f,
                1.0f + (ThreadLocalRandom.current().nextFloat() - ThreadLocalRandom.current().nextFloat()) * 0.4f
        );
        this.paperHook.getWorld().playSound(sound, this.paperHook.getX(), this.paperHook.getY(), this.paperHook.getZ());
        this.paperHook.setVelocity(this.paperHook.getVelocity().multiply(BUOYANCY));
        this.rideBobbingVehicleIfNeeded();
        new LavaHookStateTransitionEvent(this, FishHook.HookState.BOBBING).callEvent();
        this.currentState = FishHook.HookState.BOBBING;
    }

    private void handleBobbingState(final FluidData fluidData, final Runnable fishing) {
        if (this.paperHook.isOnGround()) {
            new LavaHookStateTransitionEvent(this, FishHook.HookState.UNHOOKED).callEvent();
            this.currentState = FishHook.HookState.UNHOOKED;
            return;
        }

        this.rideBobbingVehicleIfNeeded();

        final Location hookLocation = this.paperHook.getLocation();
        final Vector velocity = Objects.requireNonNull(this.bobbingVehicle).getVelocity();
        final float lavaHeight = fluidData.computeHeight(hookLocation);

        double diff = hookLocation.getY() + velocity.getY() - hookLocation.getBlockY() - lavaHeight;
        if (Math.abs(diff) < MIN_DIFF_THRESHOLD) {
            diff += Math.signum(diff) * DIFF_ADJUSTMENT;
        }
        velocity.setX(velocity.getX() * HORIZONTAL_DAMPING);
        velocity.setY(velocity.getY() - diff * ThreadLocalRandom.current().nextFloat() * VERTICAL_ADJUSTMENT_FACTOR);
        velocity.setZ(velocity.getZ() * HORIZONTAL_DAMPING);

        this.updateOpenLavaState();

        if (0.0f < lavaHeight) {
            this.outOfLavaTime = Math.max(0, this.outOfLavaTime -1);
            if (this.biting) {
                velocity.setX(0.0f);
                velocity.setY(BITING_Y_MULTIPLIER * ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextFloat());
                velocity.setZ(0.0f);
            }
            fishing.run();
        } else {
            this.outOfLavaTime = Math.min(10, this.outOfLavaTime + 1);
            velocity.add(GRAVITY);
        }

        velocity.multiply(WATER_RESISTANCE);
        this.bobbingVehicle.setVelocity(velocity);
    }

    private void fishing() {
        final Location hookLocation = this.paperHook.getLocation();
        int weatherModifier = this.calculateWeatherModifier(hookLocation);
        if (0 < this.nibble) {
            this.nibble--;
            if (this.nibble <= 0) {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
                this.biting = false;
                if (this.callBukkitEvent) {
                    new PlayerFishEvent(this.player, null, this.paperHook, PlayerFishEvent.State.FAILED_ATTEMPT).callEvent(); // WARNING: Bukkit events should not be called.
                    new LavaFishStateTransitionEvent(this, LavaFishStateTransitionEvent.FishState.ESCAPE).callEvent();
                }
            }
        } else if (0 < this.timeUntilHooked) {
            this.timeUntilHooked -= weatherModifier;
            if (0 < this.timeUntilHooked) {
                this.updateFishMovement();
                this.spawnLuredParticles();
            } else {
                if (this.callBukkitEvent && new PlayerFishEvent(this.player, null, this.paperHook, PlayerFishEvent.State.BITE).callEvent() || // WARNING: Bukkit events should not be called.
                        new LavaFishStateTransitionEvent(this, LavaFishStateTransitionEvent.FishState.BITE).callEvent()
                ) {
                    this.playBiteSoundAndParticles();
                    this.nibble = ThreadLocalRandom.current().nextInt(20, 40);
                    this.biting = true;
                }
            }
        } else if (0 < this.timeUntilLured) {
            this.timeUntilLured -= weatherModifier;
            this.spawnSplashParticlesRandomly();
            if (this.timeUntilLured <= 0) {
                this.fishAngle = ThreadLocalRandom.current().nextFloat(this.minLureAngle.get(), this.maxLureAngle.get());
                this.timeUntilHooked = ThreadLocalRandom.current().nextInt(this.minLureTime.get(), this.maxLureTime.get());
                if (this.callBukkitEvent && !new PlayerFishEvent(this.player, null, this.paperHook, PlayerFishEvent.State.LURED).callEvent() || // WARNING: Bukkit events should not be called.
                        !new LavaFishStateTransitionEvent(this, LavaFishStateTransitionEvent.FishState.LURED).callEvent()) {
                    this.timeUntilHooked = 0;
                }
            }
        } else {
            this.resetTimeUntilLured();
        }
    }

    private int calculateWeatherModifier(final Location hookLocation) {
        int modifier = 1;
        if (this.rainInfluenced.get() && this.paperHook.isInRain()) {
            modifier++;
        }
        final Block blockAbove = hookLocation.getBlock().getRelative(0, 1, 0);
        if (this.skyInfluenced.get() && ThreadLocalRandom.current().nextFloat() < 0.5f && 0 < blockAbove.getLightFromSky()) {
            modifier--;
        }
        return modifier;
    }

    private void updateFishMovement() {
        this.fishAngle += 9.188f * (ThreadLocalRandom.current().nextFloat() - ThreadLocalRandom.current().nextFloat());
    }

    private void spawnLuredParticles() {
        final World world = this.paperHook.getWorld();
        final double angleRad = Math.toRadians(this.fishAngle);
        final double sin = Math.sin(angleRad);
        final double cos = Math.cos(angleRad);
        final double x = this.paperHook.getX() + sin * this.timeUntilHooked * 0.1;
        final double y = Math.floor(this.paperHook.getY()) + 1.0;
        final double z = this.paperHook.getZ() + cos * this.timeUntilHooked * 0.1;
        final Block block = world.getBlockAt((int) Math.floor(x), (int) Math.floor(y - 1.0), (int) Math.floor(z));

        if (block.getType() == Material.LAVA) {
            if (ThreadLocalRandom.current().nextFloat() < 0.15f) {
                world.spawnParticle(this.bubbleParticle, x, y - 0.1, z, 1, sin, 0.1, cos, 0.0);
            }
            final double offsetX = sin * 0.04;
            final double offsetZ = cos * 0.04;
            world.spawnParticle(this.fishingParticle, x, y, z, 0, offsetZ, 0.01, -offsetX, 1.0);
            world.spawnParticle(this.fishingParticle, x, y, z, 0, -offsetZ, 0.01, offsetX, 1.0);
        }
    }

    private void spawnSplashParticlesRandomly() {
        final World world = this.paperHook.getWorld();
        float threshold = 0.15f;
        if (this.timeUntilLured < 20) {
            threshold += (20 - this.timeUntilLured) * 0.05f;
        } else if (this.timeUntilLured < 40) {
            threshold += (40 - this.timeUntilLured) * 0.02f;
        } else if (this.timeUntilLured < 60) {
            threshold += (60 - this.timeUntilLured) * 0.01f;
        }
        if (ThreadLocalRandom.current().nextFloat() < threshold) {
            final float randomAngle = ThreadLocalRandom.current().nextFloat(0.0f, 360.0f);
            final float randomDistance = ThreadLocalRandom.current().nextFloat(25.0f, 60.0f);
            final double rad = Math.toRadians(randomAngle);
            final double sin = Math.sin(rad);
            final double cos = Math.cos(rad);
            final double x = this.paperHook.getX() + sin * randomDistance * 0.1;
            final double y = Math.floor(this.paperHook.getY()) + 1.0;
            final double z = this.paperHook.getZ() + cos * randomDistance * 0.1;
            final Block block = world.getBlockAt((int) Math.floor(x), (int) Math.floor(y - 1.0), (int) Math.floor(z));
            if (block.getType() == Material.LAVA) {
                final int count = 2 + ThreadLocalRandom.current().nextInt(2);
                world.spawnParticle(this.splashParticle, x, y, z, count, 0.1f, 0.0, 0.1f, 0.0);
            }
        }
    }

    private void playBiteSoundAndParticles() {
        final Sound splashSound = Sound.sound(
                this.biteSound,
                Sound.Source.NEUTRAL,
                0.25f,
                0.7f + (ThreadLocalRandom.current().nextFloat() - ThreadLocalRandom.current().nextFloat()) * 0.4f
        );
        this.paperHook.getWorld().playSound(splashSound, this.paperHook.getX(), this.paperHook.getY(), this.paperHook.getZ());

        final double yOffset = this.paperHook.getY() + 0.5;
        final int particleCount = (int) (1.0F + this.paperHook.getWidth() * 20.0F);
        this.paperHook.getWorld().spawnParticle(this.bubbleParticle, this.paperHook.getX(), yOffset, this.paperHook.getZ(), particleCount, this.paperHook.getWidth(), 0.0, this.paperHook.getWidth(), 0.2F);
        this.paperHook.getWorld().spawnParticle(this.fishingParticle, this.paperHook.getX(), yOffset, this.paperHook.getZ(), particleCount, this.paperHook.getWidth(), 0.0, this.paperHook.getWidth(), 0.2F);
    }

    private void resetTimeUntilLured() {
        this.timeUntilLured = ThreadLocalRandom.current().nextInt(this.minWaitTime.get(), this.maxWaitTime.get());
        if (this.applyLure.get()) {
            this.timeUntilLured -= (this.lureSpeed >= this.maxWaitTime.get() ? this.timeUntilLured - 1 : this.lureSpeed);
        }
    }

    private void updateOpenLavaState() {
        this.openLava = (this.nibble <= 0 && this.timeUntilHooked <= 0)
                || (this.openLava && this.outOfLavaTime < 10 && this.calculateOpenLava());
        PDCUtil.setInOpenLava(this.paperHook.getPersistentDataContainer(), this.openLava);
    }

    public boolean calculateOpenLava() {
        final Block locationBlock = this.paperHook.getLocation().getBlock();
        boolean insideLava = false;
        boolean aboveLava = false;

        for (int y = -1; y <= 2; y++) {
            Material previous = null;
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    final Block relativeBlock = locationBlock.getRelative(x, y, z);
                    final Material next = relativeBlock.getType();

                    // If an invalid block is found, return false.
                    if (!VALID_LAVA_BLOCKS.contains(next) || (aboveLava && next == Material.LAVA)) {
                        return false;
                    }

                    // If different from the previous block, return false.
                    if (previous != null && previous != next) {
                        return false;
                    }

                    previous = next;
                }
            }

            final boolean isLavaLayer = previous == Material.LAVA;
            if (insideLava && !isLavaLayer) {
                aboveLava = true;
            }

            insideLava = isLavaLayer;
        }

        return true;
    }

    private void rideBobbingVehicleIfNeeded() {
        final Entity vehicle = this.paperHook.getVehicle();

        if (this.bobbingVehicle == null || vehicle == null) {
            if (vehicle != null && !LavaFishingUtil.isLavaHooksVehicle(vehicle)) {
                final String vehicleInfo = vehicle.getAsString() != null
                        ? vehicle.getAsString()
                        : vehicle.getType().key().asString();
                this.player.sendRichMessage("<#F6AA00>[LavaFishing] Illegal state of 'fishing_bobber' detected. Lava fishing cannot continue. Please report to the administrator.</#F6AA00>");
                throw new IllegalStateException("The FishHook already rides on an entity other than this plugin entity: %s".formatted(vehicleInfo));
            }

            this.bobbingVehicle = this.paperHook.getWorld().spawn(this.paperHook.getLocation().add(0, -0.25, 0), Snowball.class, bobbingVehicle -> {
                bobbingVehicle.setSilent(true);
                bobbingVehicle.setItem(ItemStack.of(Material.STONE));
                bobbingVehicle.setInvisible(true); // hide shadow
                bobbingVehicle.setPersistent(false); // NoPersistence
                bobbingVehicle.setVelocity(this.paperHook.getVelocity());
                bobbingVehicle.setInvulnerable(true);
                bobbingVehicle.addPassenger(this.paperHook); //
                bobbingVehicle.setGravity(false); // simulate with wrapped hooks
                bobbingVehicle.setNoPhysics(true);
                LavaFishingUtil.markLavaHooksVehicle(bobbingVehicle);
            });
        }
    }
}
