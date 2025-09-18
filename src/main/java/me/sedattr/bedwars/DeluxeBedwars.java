package me.sedattr.bedwars;

import com.hakan.invapi.InventoryAPI;
import com.hakan.scoreboard.api.ScoreboardAPI;
import me.sedattr.bedwars.commands.*;
import me.sedattr.bedwars.events.entity.*;
import me.sedattr.bedwars.events.player.*;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.FileHandler;
import me.sedattr.bedwars.handlers.GameManager;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.nms.VersionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class DeluxeBedwars extends JavaPlugin {
    private VersionManager versionManager;
    private GameManager gameManager;
    private static DeluxeBedwars instance;

    public static DeluxeBedwars getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Variables.config = getConfig();
        Variables.messages = getConfig().getConfigurationSection("messages");

        if (getServer().getPluginManager().getPlugin("Citizens") == null || !getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        versionManager = new VersionManager();
        versionManager.setupNMS();

        gameManager = new GameManager();

        ScoreboardAPI.setup(this);
        InventoryAPI.setup(this);
        new FileHandler("data.yml");
        new FileHandler("cosmetics.yml");
        new FileHandler("items.yml");
        new FileHandler("menus.yml");

        registerAllEvents();
        getCommand("bedwars").setExecutor(new BedwarsCommand(this));
        getCommand("bedwarssetup").setExecutor(new BedwarsSetupCommand(this));
        getCommand("bedwarsparty").setExecutor(new BedwarsPartyCommand(this));
        getCommand("bedwarsteam").setExecutor(new BedwarsTeamCommand(this));
        getCommand("bedwarsadmin").setExecutor(new BedwarsAdminCommand(this));
        Bukkit.getConsoleSender().sendMessage("plugin started");

        new BukkitRunnable() {
            @Override
            public void run() {
                ConfigurationSection section = Variables.config.getConfigurationSection("lobby.location");
                if (section != null && Bukkit.getWorld(section.getString("world")) != null)
                    Variables.lobby = new Location(Bukkit.getWorld(section.getString("world")), section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), section.getInt("yaw"), section.getInt("pitch"));

                File dataFolder = new File(getDataFolder() + "/arenas");
                File[] files = dataFolder.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (!file.getName().contains(".yml")) continue;

                        new ArenaHandler(file.getName().replace(".yml", ""));
                    }
                }
            }
        }.runTaskLaterAsynchronously(this, 200);
    }

    public void onDisable() {
        for (ArenaHandler arena : gameManager.getArenas()) {
            if (arena == null)
                continue;

            arena.reloadArena();
            arena.teleportPlayers(true);
        }
    }

    public void registerAllEvents() {
        Bukkit.getPluginManager().registerEvents(new ChangeBlockEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ExplodeEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new LaunchEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new TargetEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new BedEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new BreakPlaceEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageByEntityEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new DropEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new FoodLevelEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new InteractAtEntityEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new InteractEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinLeaveEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new MoveEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new PickupEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new TeleportEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new ConsumeEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new HitEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldChangeEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new RespawnEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new CraftEvent(this), this);
    }
}
