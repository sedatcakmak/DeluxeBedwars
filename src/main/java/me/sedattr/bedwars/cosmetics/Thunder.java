package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public class Thunder {
    Location location;

    public Thunder(Location location) {
        this.location = location;
    }

    public void start() {
        BukkitTask task = (new BukkitRunnable() {
            public void run() {
                double randomX = ThreadLocalRandom.current().nextDouble(Thunder.this.location.getX() - 25, Thunder.this.location.getX() + 25);
                double randomY = ThreadLocalRandom.current().nextDouble(Thunder.this.location.getY(), Thunder.this.location.getY() + 25);
                double randomZ = ThreadLocalRandom.current().nextDouble(Thunder.this.location.getZ() - 25, Thunder.this.location.getZ() + 25);

                Location loc = new Location(Thunder.this.location.getWorld(), randomX, randomY, randomZ);
                loc.getWorld().strikeLightningEffect(loc);
            }
        }).runTaskTimer(DeluxeBedwars.getInstance(), 0L, 10L);

        new BukkitRunnable() {
            public void run() {
                task.cancel();
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 250L);
    }
}