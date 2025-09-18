package me.sedattr.bedwars.cosmetics;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public class DeathCry {
    Location location;
    ConfigurationSection section;

    public DeathCry(Location location, ConfigurationSection section) {
        this.location = location;
        this.section = section;
    }

    public void start() {
        if (this.section == null)
            return;
        if (this.section.getString("sound") == null)
            return;
        if (this.section.getString("sound").equalsIgnoreCase("none"))
            return;

        Sound sound = Sound.valueOf(this.section.getString("sound"));
        this.location.getWorld().playSound(this.location, sound, Math.max(this.section.getInt("volume"), 1), Math.max(this.section.getInt("pitch"), 1));
    }
}