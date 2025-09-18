package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColdSnap {
    HashMap<Location, String> changedBlocks = new HashMap<>();
    Player player;

    public ColdSnap(Player player) {
        this.player = player;
    }

    public void start() {
        BukkitTask task = (new BukkitRunnable() {
            public void run() {
                for (Block block : getNearbyBlocks(ColdSnap.this.player.getLocation())) {
                    ColdSnap.this.changedBlocks.put(block.getLocation(), block.getTypeId() + ":" + block.getType() + ":" + block.getData());
                    block.setType(Material.PACKED_ICE);
                }
            }
        }).runTaskTimer(DeluxeBedwars.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            public void run() {
                task.cancel();

                ColdSnap.this.changedBlocks.forEach((key, value) -> {
                    String[] args = value.split("[:]", 3);

                    key.getBlock().setTypeId(Integer.parseInt(args[0]));
                    key.getBlock().setType(Material.valueOf(args[1]));
                    key.getBlock().setData(Byte.parseByte(args[2]));
                });
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 250L);
    }

    private List<Block> getNearbyBlocks(Location location) {
        List<Block> blocks = new ArrayList<>();
        for (int x = location.getBlockX() - 3; x <= location.getBlockX() + 3; x++) {
            for (int y = location.getBlockY() - 3; y <= location.getBlockY() + 3; y++) {
                for (int z = location.getBlockZ() - 3; z <= location.getBlockZ() + 3; z++) {
                    Block block = location.getWorld().getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR && block.getType() != Material.ICE && block.getType() != Material.PACKED_ICE)
                        blocks.add(block);
                }
            }
        }
        return blocks;
    }
}
