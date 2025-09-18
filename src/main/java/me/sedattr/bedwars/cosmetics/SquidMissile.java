package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.nms.ParticleEffect;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Squid;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class SquidMissile {
    Location location;
    int time = 0;

    public SquidMissile(Location location) {
        this.location = location;
    }

    public void start() {
        Location location = this.location.clone().add(0, 2, 0);
        Squid squid = this.location.getWorld().spawn(location, Squid.class);
        squid.setNoDamageTicks(Integer.MAX_VALUE);
        squid.setMetadata("BedwarsCOSMETIC", new FixedMetadataValue(DeluxeBedwars.getInstance(), "BedwarsCOSMETIC"));

        new BukkitRunnable() {
            public void run() {
                SquidMissile.this.time++;
                ParticleEffect.FLAME.display(0.0F, 0.0F, 0.0F, 0.0F, 1, location.add(0, -2, 0), 100.0D);
                if (SquidMissile.this.time > 25) {
                    squid.getWorld().playEffect(squid.getLocation(), Effect.EXPLOSION_LARGE, 1);

                    squid.remove();
                    cancel();
                }

                location.add(0, 2+(0.15*SquidMissile.this.time), 0);
                squid.teleport(location);

            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 1L);
    }
}
