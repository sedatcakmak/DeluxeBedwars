package me.sedattr.bedwars.handlers;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TeamSetupHandler {
    private final ArenaSetupHandler setup;
    private final Player player;
    private final String name;

    private Location upgradeLocation;
    private Location shopLocation;
    private Location spawnLocation;
    private Location bedLocation;
    private Location generator;
    private Location minLocation;
    private Location maxLocation;
    private Integer maxPlayers = 0;

    public TeamSetupHandler(ArenaSetupHandler setup, TeamHandler handler) {
        this.setup = setup;
        this.player = setup.getPlayer();
        this.name = handler.getName();

        this.bedLocation = handler.getBedLocation();
        this.generator = handler.getGeneratorLocation();
        this.shopLocation = handler.getShopLocation();
        this.upgradeLocation = handler.getUpgradeLocation();
        this.spawnLocation = handler.getSpawnLocation();
        this.minLocation = handler.getFirstLocation();
        this.maxLocation = handler.getSecondLocation();
        this.maxPlayers = handler.getMaxPlayers();

        setup.addTeam(this);
    }

    public TeamSetupHandler(ArenaSetupHandler setup, Player player, String name) {
        this.name = name;
        this.player = player;
        this.setup = setup;

        setup.addTeam(this);
    }

    public Integer getMaxPlayers() {
        return this.maxPlayers;
    }

    public Location getUpgradeLocation() {
        return this.upgradeLocation;
    }

    public Location getBedLocation() {
        return this.bedLocation;
    }

    public Location getShopLocation() {
        return this.shopLocation;
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public Location getGenerator() {
        return this.generator;
    }

    public Location getMinLocation() {
        return this.minLocation;
    }

    public Location getMaxLocation() {
        return this.maxLocation;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setGenerator(Location location) {
        this.generator = location;
    }

    public void setMaxPlayers(Integer count) {
        this.maxPlayers = count;
    }

    public void setLocation(Location location, String type) {
        switch (type.toLowerCase()) {
            case "spawn":
                this.spawnLocation = location;
                break;
            case "upgrade":
                this.upgradeLocation = location;
                break;
            case "shop":
                this.shopLocation = location;
                break;
            case "bed":
                this.bedLocation = location;
                break;
            default:
        }
    }

    public void setProtection(Location location, String type) {
        switch (type.toLowerCase()) {
            case "min":
                this.minLocation = location;
                break;
            case "max":
                this.maxLocation = location;
                break;
            default:
        }
    }

    public String getName() {
        return this.name;
    }

    public List<String> missing() {
        List<String> missing = new ArrayList<>();
        if (this.upgradeLocation == null) missing.add("upgrade_location");
        if (this.shopLocation == null) missing.add("shop_location");
        if (this.bedLocation == null) missing.add("bed_location");
        if (this.spawnLocation == null) missing.add("spawn_location");
        if (this.generator == null) missing.add("generator_location");
        if (this.minLocation == null) missing.add("min_protection");
        if (this.maxLocation == null) missing.add("max_protection");
        if (this.maxPlayers <= 0) missing.add("max_players");
        if (missing.size() > 0) return missing;

        return null;
    }

    public List<String> save() throws NullPointerException {
        File file = new File(DeluxeBedwars.getInstance().getDataFolder() + "/arenas", this.setup.getName() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        List<String> missing = missing();
        if (missing != null) return missing;

        configuration.set("teams." + this.name + ".generator", this.generator);
        configuration.set("teams." + this.name + ".spawn", this.spawnLocation);
        configuration.set("teams." + this.name + ".bed", this.bedLocation);
        configuration.set("teams." + this.name + ".shop", this.shopLocation);
        configuration.set("teams." + this.name + ".upgrade", this.upgradeLocation);
        configuration.set("teams." + this.name + ".protection.first", this.minLocation);
        configuration.set("teams." + this.name + ".protection.second", this.maxLocation);
        configuration.set("teams." + this.name + ".max_players", this.maxPlayers);

        try {
            configuration.save(file);
        } catch (IOException ignored) {
        }

        this.setup.setTeam(null);
        return null;
    }
}
