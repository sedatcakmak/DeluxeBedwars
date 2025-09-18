package me.sedattr.bedwars.cosmetics;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class Firework {
    Location location;
    Random random = new Random();
    
    public Firework(Location location) {
        this.location = location;
    }

    private void firework(Location loc) {
        org.bukkit.entity.Firework firework = loc.getWorld().spawn(loc, org.bukkit.entity.Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        FireworkEffect fireworkEffect = FireworkEffect.builder()
                .withColor(Color.fromRGB(this.random.nextInt(255), this.random.nextInt(255), this.random.nextInt(255)))
                .withFade(Color.fromRGB(this.random.nextInt(255), this.random.nextInt(255), this.random.nextInt(255)))
                .with(FireworkEffect.Type.values()[this.random.nextInt(4)])
                .build();
        meta.addEffect(fireworkEffect);
        meta.setPower(0);
        firework.setFireworkMeta(meta);
    }

    public void start() {
        firework(this.location);
    }
}
