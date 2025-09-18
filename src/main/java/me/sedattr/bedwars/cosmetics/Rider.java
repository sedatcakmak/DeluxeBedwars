package me.sedattr.bedwars.cosmetics;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.helpers.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Rider {
    Player player;
    LivingEntity entity;
    EntityType entityType;
    String name;

    public Rider(Player player, String type, String name) {
        this.player = player;
        this.entityType = EntityType.valueOf(type);
        this.name = name;
    }

    public void start() {
        if (this.entityType == null) return;

        this.entity = (LivingEntity) this.player.getWorld().spawnEntity(this.player.getLocation(), this.entityType);
        this.entity.setPassenger(this.player);
        this.entity.setNoDamageTicks(Integer.MAX_VALUE);
        this.entity.setCustomName(Utils.colorize(this.name.replace("%player%", this.player.getName()).replace("%displayname%", this.player.getDisplayName())));
        this.entity.setMetadata("BedwarsCOSMETIC", new FixedMetadataValue(DeluxeBedwars.getInstance(), this.player));

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Rider.this.entity.setVelocity(Rider.this.player.getLocation().getDirection().multiply(0.7D));
                Rider.this.entity.getLocation().setDirection(Rider.this.player.getLocation().getDirection());
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 1L);

        new BukkitRunnable() {
            public void run() {
                Rider.this.entity.remove();
                task.cancel();
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 250L);
    }
}
