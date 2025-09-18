package me.sedattr.bedwars.events.entity;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityDeathListener implements Listener {
    private final DeluxeBedwars plugin;

    public EntityDeathListener(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(org.bukkit.event.entity.EntityDeathEvent e) {
        if (e.getEntity() == null) return;

        if (e.getEntity().hasMetadata("BedwarsTEAM")) {
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }
}
