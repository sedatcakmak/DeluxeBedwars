package me.sedattr.bedwars.handlers;

import lombok.Getter;
import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArenaSetupHandler {
    @Getter private final Player player;
    @Getter private final String name;
    @Getter private final World world;

    @Getter private final List<TeamSetupHandler> teams = new ArrayList<>();
    @Getter private TeamSetupHandler team = null;
    @Getter private List<Location> diamondGenerators = new ArrayList<>();
    @Getter private List<Location> emeraldGenerators = new ArrayList<>();
    @Getter private String type;
    @Getter private String mode;
    @Getter private Location waitingLocation;
    @Getter private Location minLocation;
    @Getter private Location maxLocation;
    @Getter private Location spectateLocation;
    @Getter private Integer maxPlayers = 0;
    @Getter private Integer minPlayers = 0;
    @Getter private Boolean finished = false;

    public ArenaSetupHandler(Player player, ArenaHandler handler) {
        this.player = player;
        this.world = handler.getWorld();
        this.name = handler.getName();

        this.diamondGenerators = handler.getDiamondGenerators();
        this.emeraldGenerators = handler.getEmeraldGenerators();
        this.type = handler.getType();
        this.mode = handler.getMode();
        this.maxLocation = handler.getMaxLocation();
        this.minLocation = handler.getMinLocation();
        this.waitingLocation = handler.getWaitingLocation();
        this.spectateLocation = handler.getSpectateLocation();
        this.minPlayers = handler.getMinPlayers();
        this.maxPlayers = handler.getMaxPlayers();

        List<TeamHandler> teams = handler.getTeams();
        if (teams != null && !teams.isEmpty())
            teams.forEach(team -> new TeamSetupHandler(this, team));

        DeluxeBedwars.getInstance().getGameManager().addSetup(this);
    }

    public ArenaSetupHandler(World world, Player player, String name) {
        this.world = world;
        this.name = name;
        this.player = player;

        DeluxeBedwars.getInstance().getGameManager().addSetup(this);
    }

    public void setTeam(TeamSetupHandler team) {
        this.team = team;
    }

    public void addTeam(TeamSetupHandler team) {
        this.teams.add(team);

        if (this.team == null)
            this.team = team;
    }

    public Boolean setType(String type) {
        if (!Variables.messages.getConfigurationSection("types").getKeys(false).contains(type))
            return false;

        this.type = type;
        return true;
    }

    public void setMode(String mode) {
        if (!Variables.messages.getConfigurationSection("modes").getKeys(false).contains(mode))
            return;

        this.mode = mode;
    }

    public void addGenerator(Location location, String type) {
        switch (type.toLowerCase()) {
            case "diamond":
                this.diamondGenerators.add(location);
                break;
            case "emerald":
                this.emeraldGenerators.add(location);
                break;
            default:
        }

    }

    public void setPlayers(int count, String type) {
        switch (type.toLowerCase()) {
            case "max":
                this.maxPlayers = count;
                break;
            case "min":
                this.minPlayers = count;
                break;
            default:
        }

    }

    public void setLocation(Location location, String type) {
        switch (type.toLowerCase()) {
            case "waiting":
                this.waitingLocation = location;
                break;
            case "spectate":
                this.spectateLocation = location;
                break;
            case "min":
                this.minLocation = location;
                break;
            case "max":
                this.maxLocation = location;
                break;
            default:
        }

    }

    public List<String> missing() {
        List<String> missing = new ArrayList<>();
        if (this.diamondGenerators.size() <= 0) missing.add("diamond_generators");
        if (this.emeraldGenerators.size() <= 0) missing.add("emerald_generators");
        if (this.type == null) missing.add("type");
        if (this.mode == null) missing.add("mode");
        if (this.waitingLocation == null) missing.add("waiting_location");
        if (this.spectateLocation == null) missing.add("spectate_location");
        if (this.maxLocation == null) missing.add("max_location");
        if (this.minLocation == null) missing.add("min_location");
        if (this.minPlayers <= 0) missing.add("min_players");
        if (this.maxPlayers <= 0) missing.add("max_players");
        if (missing.size() > 0) return missing;

        return null;
    }

    public List<String> save() throws NullPointerException {
        File file = new File(DeluxeBedwars.getInstance().getDataFolder() + "/arenas", this.name + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        List<String> missing = missing();
        if (missing != null) return missing;

        configuration.set("enabled", true);
        configuration.set("generators.diamond", this.diamondGenerators);
        configuration.set("generators.emerald", this.emeraldGenerators);
        configuration.set("settings.type", this.type);
        configuration.set("settings.mode", this.mode);
        configuration.set("settings.waiting", this.waitingLocation);
        configuration.set("settings.spectate", this.spectateLocation);
        configuration.set("settings.min", this.minLocation);
        configuration.set("settings.max", this.maxLocation);
        configuration.set("settings.min_players", this.minPlayers);
        configuration.set("settings.max_players", this.maxPlayers);
        configuration.set("settings.world", this.world.getName());

        try {
            configuration.save(file);
        } catch (IOException ignored) {
        }

        this.finished = true;

        new ArenaHandler(this.name);
        return null;
    }
}
