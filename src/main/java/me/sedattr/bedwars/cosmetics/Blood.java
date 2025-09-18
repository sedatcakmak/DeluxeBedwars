package me.sedattr.bedwars.cosmetics;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

public class Blood {
    Location location;

    public Blood(Location location) {
        this.location = location;
    }

    public void start() {
        this.location.getWorld().playEffect(this.location.clone().add(0.0D, 0.9D, 0.0D), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        this.location.getWorld().playEffect(this.location.clone().add(0.0D, 0.5D, 0.0D), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        this.location.getWorld().playEffect(this.location.clone().add(0.0D, 0.1D, 0.0D), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
    }
}
