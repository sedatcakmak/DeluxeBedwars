package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Bugs {
    Location location;
    EntityType type;
    Integer max;
    List<Entity> entities = new ArrayList<>();

    public Bugs(Location location, int max, String type) {
        this.location = location;
        this.max = max;
        this.type = EntityType.valueOf(type);
    }
    public void start() {
        if (this.type == null)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (entities.size() >= max) {
                    cancel();
                    return;
                }

                double x = ThreadLocalRandom.current().nextDouble(-2, 2);
                double z = ThreadLocalRandom.current().nextDouble(-2, 2);
                Location newLocation = Bugs.this.location.clone().add(x, 0, z);

                Entity entity = Bugs.this.location.getWorld().spawnEntity(newLocation, Bugs.this.type);
                entity.setMetadata("BedwarsCOSMETIC", new FixedMetadataValue(DeluxeBedwars.getInstance(), Bugs.this.location));
                entities.add(entity);
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            public void run() {
                Bugs.this.entities.forEach(Entity::remove);
                cancel();
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 150L);
    }
}
