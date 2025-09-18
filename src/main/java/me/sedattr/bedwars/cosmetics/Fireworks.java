package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Fireworks {
    Random random = new Random();
    Player player;
    
    public Fireworks(Player player) {
        this.player = player;
    }

    private void firework(Location loc) {
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        FireworkEffect.Type type = FireworkEffect.Type.values()[this.random.nextInt(4)];
        Color color1 = Color.fromBGR(this.random.nextInt(255), this.random.nextInt(255), this.random.nextInt(255));
        Color color2 = Color.fromBGR(this.random.nextInt(255), this.random.nextInt(255), this.random.nextInt(255));

        FireworkEffect fireworkEffect = FireworkEffect.builder().withColor(color1).withFade(color2).with(type).build();
        fireworkMeta.addEffect(fireworkEffect);
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
    }

    public void start() {
        BukkitTask task = (new BukkitRunnable() {
            public void run() {
                double randomX = ThreadLocalRandom.current().nextDouble(Fireworks.this.player.getLocation().getX() - 3, Fireworks.this.player.getLocation().getX() + 3);
                double randomY = ThreadLocalRandom.current().nextDouble(Fireworks.this.player.getLocation().getY() - 1, Fireworks.this.player.getLocation().getY() + 1);
                double randomZ = ThreadLocalRandom.current().nextDouble(Fireworks.this.player.getLocation().getZ() - 3, Fireworks.this.player.getLocation().getZ() + 3);

                Location loc = new Location(Fireworks.this.player.getWorld(), randomX, randomY, randomZ);
                firework(loc);
            }
        }).runTaskTimer(DeluxeBedwars.getInstance(), 0L, 10L);

        Bukkit.getScheduler().runTaskLater(DeluxeBedwars.getInstance(), task::cancel, 250L);
    }
}
