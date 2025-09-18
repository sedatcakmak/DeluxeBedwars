package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.nms.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BurningShoes {
    Player player;

    public BurningShoes(Player player) {
        this.player = player;
    }

    public void start() {
        final Boolean[] foot = {true};
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Location l = BurningShoes.this.player.getLocation();
                l.setY(Math.floor(l.getY()));
                if (!l.clone().subtract(0.0D, 1.0D, 0.0D).getBlock().isEmpty()) {
                    double x = Math.cos(Math.toRadians(BurningShoes.this.player.getLocation().getYaw())) * 0.25D;
                    double y = Math.sin(Math.toRadians(BurningShoes.this.player.getLocation().getYaw())) * 0.25D;
                    if (foot[0])
                        l.add(x, 0.025D, y);
                    else
                        l.subtract(x, -0.025D, y);

                    ParticleEffect.FLAME.display(0.0F, 0.0F, 0.0F, 0.0F, 2, l, 100.0D);
                    foot[0] = !foot[0];
                }
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                task.cancel();
                cancel();
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 200L);
    }
}
