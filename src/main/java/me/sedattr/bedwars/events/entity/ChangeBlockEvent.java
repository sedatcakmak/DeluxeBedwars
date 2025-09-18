package me.sedattr.bedwars.events.entity;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ChangeBlockEvent implements Listener {
    private final DeluxeBedwars plugin;

    public ChangeBlockEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void anvilDamage(EntityChangeBlockEvent e) {
        if (e.getEntity().hasMetadata("BedwarsCOSMETIC")) {
            e.getBlock().getDrops().clear();

            new BukkitRunnable() {
                public void run() {
                    e.getEntity().remove();
                    e.getBlock().getLocation().getWorld().getBlockAt(e.getEntity().getLocation()).setType(Material.AIR);
                    e.getBlock().setType(Material.AIR);
                    e.getBlock().breakNaturally();
                    e.getBlock().getDrops().clear();
                }
            }.runTaskLater(DeluxeBedwars.getInstance(), 100L);
        }
    }
}
