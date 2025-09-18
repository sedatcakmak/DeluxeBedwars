package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;

public class TNT {
    Location location;

    public TNT(Location location) {
        this.location = location;
    }

    public void start() {
        Location location = this.location.clone();

        TNTPrimed primed = TNT.this.location.getWorld().spawn(location, TNTPrimed.class);
        primed.setFuseTicks(25);
        new BukkitRunnable() {
            public void run() {
                primed.remove();
                TNT.this.location.getWorld().playEffect(location, Effect.EXPLOSION_LARGE, 1);
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 20L);
    }
}
