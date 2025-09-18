package me.sedattr.bedwars.helpers;

import com.hakan.scoreboard.scoreboard.ScoreBoard;
import me.sedattr.bedwars.handlers.ArenaHandler;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Variables {
    public static ConfigurationSection config;
    public static ConfigurationSection messages;
    public static YamlConfiguration items;
    public static YamlConfiguration menus;
    public static YamlConfiguration data;
    public static ConfigurationSection cosmetics;
    public static String date;
    public static String time;
    public static Location lobby;

    public static HashMap<UUID, String> menuID = new HashMap<>();
    public static HashMap<ArenaHandler, List<BukkitTask>> tasks = new HashMap<>();
    public static HashMap<ArenaHandler, List<BukkitTask>> otherTasks = new HashMap<>();
    public static HashMap<ArenaHandler, HashMap<Location, ArmorStand>> armorStands = new HashMap<>();
    public static HashMap<ArenaHandler, HashMap<Location, List<ArmorStand>>> textStands = new HashMap<>();
    public static HashMap<Player, Projectile> bridgeEggs = new HashMap<>();
    public static List<Player> cooldowns = new ArrayList<>();
    public static Boolean legacy = Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12");
    public static HashMap<Player, ScoreBoard> boards = new HashMap<>();
    public static List<Player> teleportedPlayers = new ArrayList<>();
    public static List<Player> hiddenPlayers = new ArrayList<>();

    public static HashMap<String, DyeColor> dyeColors = new HashMap<String, DyeColor>() {{
        put("red", DyeColor.RED);
        put("darkgreen", DyeColor.GREEN);
        put("blue", DyeColor.BLUE);
        put("yellow", DyeColor.YELLOW);
        put("aqua", DyeColor.CYAN);
        put("black", DyeColor.BLACK);
        put("gray", DyeColor.GRAY);
        put("green", DyeColor.LIME);
        put("white", DyeColor.WHITE);
        put("pink", DyeColor.PINK);
    }};

    public static HashMap<String, Color> colors = new HashMap<String, Color>() {{
        put("red", Color.RED);
        put("darkgreen", Color.GREEN);
        put("blue", Color.NAVY);
        put("yellow", Color.YELLOW);
        put("aqua", Color.AQUA);
        put("black", Color.BLACK);
        put("gray", Color.GRAY);
        put("green", Color.LIME);
        put("white", Color.WHITE);
        put("pink", Color.FUCHSIA);
    }};

    public static HashMap<String, Integer> blockDatas = new HashMap<String, Integer>() {{
        put("red", 14);
        put("darkgreen", 13);
        put("blue", 11);
        put("yellow", 4);
        put("aqua", 9);
        put("black", 15);
        put("gray", 7);
        put("green", 5);
        put("white", 0);
        put("pink", 6);
    }};
}
