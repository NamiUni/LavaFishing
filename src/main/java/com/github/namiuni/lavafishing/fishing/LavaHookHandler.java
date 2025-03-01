package com.github.namiuni.lavafishing.fishing;

import com.github.namiuni.lavafishing.config.ConfigLoader;
import com.github.namiuni.lavafishing.util.LavaFishingUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@NullMarked
@ApiStatus.Internal
public final class LavaHookHandler {

    private final ComponentLogger logger;
    private final ConfigLoader configLoader;
    private final Consumer<BukkitRunnable> scheduler;

    private final Map<UUID, LavaHook> lavaFishingUsers;

    public LavaHookHandler(
            final ComponentLogger logger,
            final ConfigLoader configLoader,
            final Consumer<BukkitRunnable> scheduler
    ) {
        this.logger = logger;
        this.configLoader = configLoader;
        this.scheduler = scheduler;

        this.lavaFishingUsers = new ConcurrentHashMap<>();
    }

    public void cast(final FishHook fishHook) {
        final LavaHook lavaHook = LavaHook.create(fishHook, this.configLoader);
        this.lavaFishingUsers.put(lavaHook.getPlayer().getUniqueId(), lavaHook);

        try {
            this.scheduler.accept(new LavaHookTask(lavaHook));
        } catch (final IllegalStateException exception) {
            this.logger.warn("fishing_bobber failed to bob on the lava.", exception);
        }
    }

    public boolean wind(final Player player, final EquipmentSlot hand) {
        if (this.lavaFishingUsers.containsKey(player.getUniqueId())) {
            final LavaHook lavaHook = this.lavaFishingUsers.get(player.getUniqueId());
            return lavaHook.wind(hand); // Return true when PlayerFishEvent is called.
        }

        return false;
    }

    public void cleanUp(final FishHook fishHook) {
        final Entity vehicle = fishHook.getVehicle();
        if (vehicle != null && LavaFishingUtil.isLavaHooksVehicle(vehicle)) {
            vehicle.remove();
        }

        final UUID ownerUUID = fishHook.getOwnerUniqueId();
        if (ownerUUID != null) {
            this.lavaFishingUsers.remove(ownerUUID);
        } else {
            this.logger.warn("fishing_bobber owner not found.");
        }
    }

    public void hook(final Entity vehicle, final Entity target) {
        final Optional<FishHook> fishHook = vehicle.getPassengers().stream()
                .filter(FishHook.class::isInstance)
                .map(FishHook.class::cast)
                .findFirst();

        if (fishHook.isEmpty()) {
            this.logger.warn("The hook does not exist. entity hook canceled.");
            return;
        }

        if (!LavaFishingUtil.isLavaHook(fishHook.get())) {
            this.logger.warn("Unknown Hook. entity hook canceled.");
        }

        if (target.isDead()) {
            this.logger.warn("Hook target has been removed.");
        }

        fishHook.get().setHookedEntity(target);
    }

    public void removeAllVehicles() {
        Bukkit.getWorlds().stream()
                .flatMap(world -> world.getEntities().stream())
                .filter(LavaFishingUtil::isLavaHooksVehicle)
                .forEach(Entity::remove);
    }
}
