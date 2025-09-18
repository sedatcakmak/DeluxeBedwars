package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public class Rain {
    Player player;
    Material material;

    public Rain(Player player, String type) {
        this.player = player;
        this.material = Material.getMaterial(type);
    }

    private void block(Location loc) {
        FallingBlock block = loc.getWorld().spawnFallingBlock(loc, this.material, (byte) 0);
        block.setMetadata("BedwarsCOSMETIC", new FixedMetadataValue(DeluxeBedwars.getInstance(), this.player));
    }

    public void start() {
        if (this.material == null) return;

        BukkitTask task = (new BukkitRunnable() {
            public void run() {
                double randomX = ThreadLocalRandom.current().nextDouble(Rain.this.player.getLocation().getX() - 10, Rain.this.player.getLocation().getX() + 10);
                double randomY = ThreadLocalRandom.current().nextDouble(Rain.this.player.getLocation().getY() + 10, Rain.this.player.getLocation().getY() + 25);
                double randomZ = ThreadLocalRandom.current().nextDouble(Rain.this.player.getLocation().getZ() - 10, Rain.this.player.getLocation().getZ() + 10);

                Location loc = new Location(Rain.this.player.getWorld(), randomX, randomY, randomZ);
                block(loc);
            }
        }).runTaskTimer(DeluxeBedwars.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            public void run() {
                task.cancel();
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 250L);
    }
}
