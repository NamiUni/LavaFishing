package com.github.namiuni.lavafishing;

import com.github.namiuni.lavafishing.config.ConfigLoader;
import com.github.namiuni.lavafishing.fishing.LavaHookListener;
import com.github.namiuni.lavafishing.fishing.LavaHookHandler;
import com.github.namiuni.lavafishing.util.PluginProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
public final class LavaFishing extends JavaPlugin {

    private final LavaHookHandler lavaHookHandler;
    private final LavaHookListener lavaHookListener;

    LavaFishing(final ConfigLoader configLoader) {
        this.lavaHookHandler = new LavaHookHandler(this.getComponentLogger(), configLoader, this::runAtFixedRate);
        this.lavaHookListener = new LavaHookListener(configLoader, this.lavaHookHandler);
    }

    @Override
    public void onLoad() {
        PluginProvider.register(this);
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this.lavaHookListener, this);
    }

    @Override
    public void onDisable() {
        this.lavaHookHandler.removeAllVehicles();
    }

    private void runAtFixedRate(final BukkitRunnable task) {
        task.runTaskTimer(this, 0L, 1L);
    }
}
