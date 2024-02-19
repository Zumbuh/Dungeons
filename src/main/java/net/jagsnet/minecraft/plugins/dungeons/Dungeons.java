package net.jagsnet.minecraft.plugins.dungeons;

import net.jagsnet.minecraft.plugins.dungeons.commands.Main;
import net.jagsnet.minecraft.plugins.dungeons.listeners.Events;
import net.jagsnet.minecraft.plugins.dungeons.listeners.LitematicTest;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Dungeons extends JavaPlugin {
    private static Dungeons instance;

    public static boolean generating = false;
    public static boolean isGenerating() { return generating; }
    public static void setGenerating(boolean b) { generating = b; }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        this.getCommand("dungeons").setExecutor(new Main());
        getServer().getPluginManager().registerEvents(new LitematicTest(), this);

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(Dungeons.getInstance(), new Runnable() {
            @Override
            public void run() {
                Events.event();
            }
        }, 0L, 100L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Dungeons getInstance() {
        return instance;
    }
}
