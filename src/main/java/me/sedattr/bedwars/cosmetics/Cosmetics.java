package me.sedattr.bedwars.cosmetics;
import me.sedattr.bedwars.handlers.DataHandler;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class Cosmetics {
    Location killerLocation;
    Location victimLocation;
    String effect;
    Player cosmeticOwner;
    Boolean killer;
    String type;

    public Cosmetics(Player cosmeticOwner, Location killerLocation, Location victimLocation, Boolean killer, String type) {
        this.killerLocation = killerLocation;
        this.victimLocation = victimLocation;
        this.cosmeticOwner = cosmeticOwner;
        this.killer = killer;
        this.effect = new DataHandler(cosmeticOwner).getPlayerSelectedCosmetic(type);
        this.type = type;
    }

    public void start() {
        if (this.effect == null) return;

        if (this.effect.equalsIgnoreCase("random")) {
            System.out.println("sorun");
        }

        if (this.type.equalsIgnoreCase("death_cries")) {
            new DeathCry(this.killer ? this.killerLocation : this.victimLocation, Variables.cosmetics.getConfigurationSection(this.type + "." + this.effect)).start();
            return;
        }

        switch (this.effect.toLowerCase()) {
            case "fireworks":
                new Fireworks(this.cosmeticOwner).start();
                break;
            case "rainbow_sheep":
            case "rainbowsheep":
                new RainbowSheep(this.killer ? this.killerLocation : this.victimLocation).start();
                break;
            case "thunder":
                new Thunder(this.killer ? this.killerLocation : this.victimLocation).start();
                break;
            case "day_night":
            case "daynight":
                new DayNight(this.cosmeticOwner.getWorld()).start();
                break;
            case "cold_snap":
            case "coldsnap":
                new ColdSnap(this.cosmeticOwner).start();
                break;
            case "blood":
                new Blood(this.killer ? this.killerLocation : this.victimLocation).start();
                break;
            case "firework":
                new Firework(this.killer ? this.killerLocation : this.victimLocation).start();
                break;
            case "squid_missile":
            case "squidmissile":
                new SquidMissile(this.killer ? this.killerLocation : this.victimLocation).start();
                break;
            case "tnt":
                new TNT(this.killer ? this.killerLocation : this.victimLocation).start();
                break;
            case "lightning_strike":
            case "lightningstrike":
                new LightningStrike(this.killer ? this.killerLocation : this.victimLocation).start();
                break;
            case "bats":
                new Bats(this.killer ? this.killerLocation : this.victimLocation).start();
                break;
            case "burning_shoes":
            case "burningshoes":
                new BurningShoes(this.cosmeticOwner).start();
                break;
            default:
                ConfigurationSection section = new DataHandler(this.cosmeticOwner).getCosmetic(this.type, this.effect);
                if (section != null && section.getString("type") != null && !section.getString("type").equals("")) {
                    if (section.getString("type").contains("ride"))
                        new Rider(this.cosmeticOwner, section.getString("entity"), section.getString("name")).start();
                    else if (section.getString("type").contains("rain"))
                        new Rain(this.cosmeticOwner, section.getString("material")).start();
                    else if (section.getString("type").contains("fountain")) {
                        if (section.getString("material") != null)
                            new SingleFountain(this.cosmeticOwner, section.getString("material")).start();
                        else if (section.getStringList("materials") != null)
                            new MultiFountain(this.cosmeticOwner, section.getStringList("materials")).start();
                    } else if (section.getString("type").contains("entities")) {
                        if (section.getString("entity") != null && section.getInt("count") > 0)
                            new Bugs(this.killer ? this.killerLocation : this.victimLocation, section.getInt("count"), section.getString("entity")).start();
                    } else if (section.getString("type").contains("effect")) {
                        new Effects(this.killer ? this.killerLocation : this.victimLocation, section).start();
                    }
                }
                break;
        }
    }
}