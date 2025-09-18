package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Bats {
    List<Entity> bats = new ArrayList<>();
    Location location;

    public Bats(Location location) {
        this.location = location;
    }

    public void start() {
        Location location = this.location.clone();
        location.add(0, 2, 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bats.this.bats.size() >= 10) {
                    cancel();
                    return;
                }

                Bat bat = (Bat) Bats.this.location.getWorld().spawnEntity(location, EntityType.BAT);
                bat.setTicksLived(200);
                bat.setNoDamageTicks(2147483647);
                Bats.this.bats.add(bat);
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0, 5L);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity bat : Bats.this.bats)
                    bat.getWorld().playEffect(bat.getLocation(), Effect.SMOKE, -100);
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 15L, 3L);

        new BukkitRunnable() {
            @Override
            public void run() {
                task.cancel();
                cancel();

                Bats.this.bats.forEach(Entity::remove);
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 200L);
    }
}
