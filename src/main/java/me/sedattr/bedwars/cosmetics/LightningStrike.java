package me.sedattr.bedwars.cosmetics;

import org.bukkit.Location;

public class LightningStrike {
    Location location;

    public LightningStrike(Location location) {
        this.location = location;
    }

    public void start() {
        this.location.getWorld().strikeLightningEffect(this.location);
    }
}