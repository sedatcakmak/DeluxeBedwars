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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MultiFountain {
    Player player;
    List<Item> items = new ArrayList<>();
    List<ItemStack> itemList = new ArrayList<>();

    public MultiFountain(Player player, List<String> list) {
        if (list == null || list.size() < 1) return;
        this.player = player;

        for (String name : list) {
            String[] args = name.split("[:]", 2);
            if (args.length < 1) return;

            this.itemList.add(new ItemStack(Material.getMaterial(args[0]), 1, Byte.parseByte(args[1])));
        }
    }

    public void start() {
        if (this.itemList == null || this.itemList.size() < 1) return;

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (MultiFountain.this.items.size() >= 50) {
                    cancel();
                    return;
                }

                MultiFountain.this.items.add(MultiFountain.this.spawn(MultiFountain.this.player.getLocation().clone().add(0, 2, 0)));
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 1L);

        new BukkitRunnable() {
            public void run() {
                for (Item item : MultiFountain.this.items)
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

        Item item = location.getWorld().dropItem(location, this.itemList.get(new Random().nextInt(this.itemList.size())));
        item.setVelocity(new Vector(x, y, z));
        item.setPickupDelay(Integer.MAX_VALUE);
        return item;
    }
}
