package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SingleFountain {
    Player player;
    List<Item> items = new ArrayList<>();
    ItemStack item;

    public SingleFountain(Player player, String name) {
        String[] args = name.split("[:]", 2);
        if (args.length < 1) return;

        this.player = player;
        this.item = new ItemStack(Material.getMaterial(args[0]), 1, Byte.parseByte(args[1]));
    }

    public void start() {
        if (this.item == null) return;

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (SingleFountain.this.items.size() >= 50) {
                    cancel();
                    return;
                }

                SingleFountain.this.items.add(SingleFountain.this.spawn(SingleFountain.this.player.getLocation().clone().add(0, 2, 0)));
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 1L);

        new BukkitRunnable() {
            public void run() {
                for (Item item : SingleFountain.this.items)
                    item.remove();

                task.cancel();
                cancel();
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 100L);
    }

    public Item spawn(Location location) {
        double x = ThreadLocalRandom.current().nextDouble(-0.25, 0.25);
        double y = ThreadLocalRandom.current().nextDouble(0.20, 0.60);
        double z = ThreadLocalRandom.current().nextDouble(-0.25, 0.25);

        Item item = location.getWorld().dropItem(location, this.item);
        item.setVelocity(new Vector(x, y, z));
        item.setPickupDelay(2147483647);
        return item;
    }
}
