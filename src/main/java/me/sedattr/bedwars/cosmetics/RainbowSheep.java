package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;

public class RainbowSheep {
    Location location;
    Sheep sheep;
    List<DyeColor> colors = new ArrayList<>(Arrays.asList(DyeColor.MAGENTA,
            DyeColor.BLACK, DyeColor.BLUE, DyeColor.BROWN, DyeColor.CYAN,
            DyeColor.GRAY, DyeColor.GREEN, DyeColor.LIGHT_BLUE, DyeColor.LIME, DyeColor.ORANGE,
            DyeColor.PINK, DyeColor.PURPLE, DyeColor.RED, DyeColor.SILVER, DyeColor.WHITE,
            DyeColor.YELLOW));

    public RainbowSheep(Location location) {
        this.location = location;
    }

    public void start() {
        this.sheep = RainbowSheep.this.location.getWorld().spawn(RainbowSheep.this.location, Sheep.class);
        this.sheep.setColor(DyeColor.BLACK);
        this.sheep.setMetadata("BedwarsCOSMETIC", new FixedMetadataValue(DeluxeBedwars.getInstance(), this.location));
        this.sheep.setNoDamageTicks(Integer.MAX_VALUE);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                int random = new Random().nextInt(RainbowSheep.this.colors.size()-1);
                RainbowSheep.this.sheep.setHealth(RainbowSheep.this.sheep.getMaxHealth());

                RainbowSheep.this.sheep.setColor(RainbowSheep.this.colors.get(random));
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            public void run() {
                task.cancel();
                RainbowSheep.this.sheep.remove();
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 250L);
    }
}
