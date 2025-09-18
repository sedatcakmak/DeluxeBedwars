package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DayNight {
    long oldTime;
    World world;

    public DayNight(World world) {
        this.world = world;
    }

    public void start() {
        this.oldTime = this.world.getTime();

        BukkitTask task = (new BukkitRunnable() {
            public void run() {
                DayNight.this.world.setTime(DayNight.this.world.getTime() + 500);
            }
        }).runTaskTimer(DeluxeBedwars.getInstance(), 0L, 1);

        new BukkitRunnable() {
            public void run() {
                task.cancel();

                DayNight.this.world.setTime(DayNight.this.oldTime);
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 250L);
    }
}
