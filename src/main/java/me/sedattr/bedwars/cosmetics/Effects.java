package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.nms.ParticleEffect;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class Effects {
    Location location;
    ConfigurationSection section;

    public Effects(Location location, ConfigurationSection section) {
        this.location = location;
        this.section = section;
    }

    public void start() {
        if (this.section == null)
            return;

        final int[] time = {0};
        if (this.section.getString("style").toLowerCase().contains("single")) {
            this.location.getWorld().playEffect(this.location, Effect.valueOf(Effects.this.section.getString("effect")), 1);
        } else if (this.section.getString("style").toLowerCase().contains("random")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (time[0] >= Effects.this.section.getInt("count")) {
                        cancel();
                        return;
                    }

                    double x = ThreadLocalRandom.current().nextDouble(-2.50, 2.50);
                    double y = ThreadLocalRandom.current().nextDouble(-1, 2.50);
                    double z = ThreadLocalRandom.current().nextDouble(-2.50, 2.50);
                    Location loc = Effects.this.location.clone();
                    loc.add(x, y, z);

                    ParticleEffect.valueOf(Effects.this.section.getString("effect")).display(0.0F, 0.0F, 0.0F, 0.0F, 1, loc, 100.0D);
                    time[0]++;
                }
            }.runTaskTimer(DeluxeBedwars.getInstance(), 0, this.section.getInt("time"));
        } else if (this.section.getString("style").toLowerCase().contains("multi")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (time[0] >= Effects.this.section.getInt("count")) {
                        cancel();
                        return;
                    }

                    Effects.this.location.getWorld().playEffect(Effects.this.location, Effect.valueOf(Effects.this.section.getString("effect")), 1);
                    time[0]++;
                }
            }.runTaskTimer(DeluxeBedwars.getInstance(), 0, this.section.getInt("time"));
        }
    }
}