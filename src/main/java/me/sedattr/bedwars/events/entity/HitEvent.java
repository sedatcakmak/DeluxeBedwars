package me.sedattr.bedwars.events.entity;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class HitEvent implements Listener {
    private final DeluxeBedwars plugin;

    public HitEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void launchSkull(ProjectileHitEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Projectile entity = e.getEntity();

                if (entity == null)
                    return;
                if (entity.getShooter() == null)
                    return;
                if (!(entity.getShooter() instanceof Player))
                    return;
                if (!entity.hasMetadata("BedwarsTHROWABLE") || !entity.hasMetadata("BedwarsTEAM"))
                    return;

                TeamHandler team = (TeamHandler) entity.getMetadata("BedwarsTEAM").get(0).value();
                if (team == null) return;

                ConfigurationSection section = Variables.items.getConfigurationSection("items." + entity.getMetadata("BedwarsTHROWABLE").get(0).asString() + ".settings");
                LivingEntity livingEntity = (LivingEntity) e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation().add(0, 1, 0), EntityType.valueOf(section.getString("entity")));
                livingEntity.setCustomNameVisible(true);
                livingEntity.setMetadata("BedwarsTEAM", new FixedMetadataValue(DeluxeBedwars.getInstance(), team));
                livingEntity.setMaxHealth(section.getDouble("health"));
                livingEntity.setHealth(section.getDouble("health"));
                livingEntity.setCustomNameVisible(true);

                team.startEntityName((Player) e.getEntity().getShooter(), livingEntity, section);
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 5L);
    }
}
