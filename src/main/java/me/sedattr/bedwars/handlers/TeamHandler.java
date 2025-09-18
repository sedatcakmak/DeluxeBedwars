package me.sedattr.bedwars.handlers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TeamHandler {
    private Boolean bedStatus = true;

    private final String name;
    private final Integer maxPlayers;
    private final Location spawnLocation;
    private final Location bedLocation;
    private final Location shopLocation;
    private final Location upgradeLocation;
    private final Location generatorLocation;
    private final List<PlayerHandler> players = new ArrayList<>();
    private final Location firstLocation;
    private final Location secondLocation;
    private final UpgradesHandler upgrades;
    private List<BukkitTask> islandTasks = new ArrayList<>();
    private final HashMap<LivingEntity, BukkitTask> entities = new HashMap<>();

    public TeamHandler(String map, String name) throws NullPointerException {
        File file = new File(DeluxeBedwars.getInstance().getDataFolder() + "/arenas", map + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection teamConfiguration = configuration.getConfigurationSection("teams." + name);

        this.name = name;
        this.maxPlayers = Math.max(teamConfiguration.getInt("max_players"), 1);
        this.firstLocation = (Location) teamConfiguration.get("protection.first");
        this.secondLocation = (Location) teamConfiguration.get("protection.second");
        this.spawnLocation = (Location) teamConfiguration.get("spawn");
        this.bedLocation = (Location) teamConfiguration.get("bed");
        this.shopLocation = (Location) teamConfiguration.get("shop");
        this.upgradeLocation = (Location) teamConfiguration.get("upgrade");
        this.generatorLocation = (Location) teamConfiguration.get("generator");

        this.upgrades = new UpgradesHandler(this);
    }

    public Integer getMaxPlayers() {
        return this.maxPlayers;
    }

    public HashMap<LivingEntity, BukkitTask> getEntities() {
        return this.entities;
    }

    public void startEntityName(Player player, LivingEntity entity, ConfigurationSection section) {
        final int[] totalTime = {section.getInt("time")};
        double neededTime = section.getString("bar_type") != null && section.getString("bar_type").equalsIgnoreCase("bar") ? section.getDouble("health") / section.getInt("count") : 0;

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (totalTime[0] <= 0) {
                    cancel();
                    entity.remove();
                    return;
                }

                ConfigurationSection teamSection = Variables.config.getConfigurationSection("teams." + TeamHandler.this.name);
                String text = section.getString("name");

                String bar = getEntityBar(section, entity.getHealth(), neededTime);
                if (!bar.equals("")) text = text.replace("%bar%", bar);

                text = text
                        .replace("%name%", player.getName())
                        .replace("%display_name%", player.getDisplayName())
                        .replace("%time%", String.valueOf(totalTime[0]))
                        .replace("%team_short%", teamSection.getString("short"))
                        .replace("%team_name%", teamSection.getString("name"))
                        .replace("%team_color%", teamSection.getString("color"));
                entity.setCustomName(Utils.colorize(text));

                totalTime[0]--;
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 20L);

        this.entities.put(entity, task);
    }

    public String getEntityBar(ConfigurationSection section, double leftTime, double neededTime) {
        if (section.getString("bar_type") == null || !section.getString("bar_type").equalsIgnoreCase("bar")) return "";

        double time = leftTime + neededTime;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < section.getInt("count"); i++) {
            String status = section.getString("unavailable");
            if (time > neededTime)
                status = section.getString("available");

            bar.append(status);

            time-=neededTime;
        }

        return bar.toString();
    }

    public Location getGeneratorLocation() {
        return this.generatorLocation;
    }

    public List<BukkitTask> getIslandTasks() {
        return this.islandTasks;
    }

    public void setIslandTasks(List<BukkitTask> tasks) {
        this.islandTasks = tasks;
    }

    private boolean isBetween(double v1, double v2, double b) {
        return (b >= v1 && b <= v2) || (b <= v1 && b >= v2);
    }

    public boolean isInside(Location l1, Location l2, Location b) {
        if (l1 == null || l2 == null || b == null) return false;

        return l1.getWorld().equals(b.getWorld()) && l2.getWorld().equals(b.getWorld()) && isBetween(l1.getZ(), l2.getZ(), b.getZ()) && isBetween(l1.getX(), l2.getX(), b.getX()) && isBetween(l1.getY(), l2.getY(), b.getY());
    }

    public void send(String message) {
        for (PlayerHandler player : this.players)
            player.getPlayer().sendMessage(Utils.colorize(message));
    }

    public NPC spawnShopNPC() {
        EntityType shop = null;
        String type;
        String skin = null;

        if (this.getPlayers().size() > 0) {
            Random random = new Random();
            PlayerHandler playerHandler = this.getPlayers().get(random.nextInt(this.getPlayers().size()));
            type = new DataHandler(playerHandler.getPlayer()).getPlayerSelectedCosmetic("shop_npcs");
            ConfigurationSection section = Variables.cosmetics.getConfigurationSection("shop_npcs." + type);
            if (section != null) {
                shop = type != null && !type.equals("") ? EntityType.valueOf(section.getString("entity")) : EntityType.valueOf(Variables.cosmetics.getString("shop_npcs.default.entity"));
                if (section.getString("skin") != null && !section.getString("skin").equals("")) skin = section.getString("skin").replace("%player%", playerHandler.getPlayer().getName());
            }
        } else
            shop = EntityType.valueOf(Variables.cosmetics.getString("shop_npcs.default.entity"));

        if (shop == null) shop = EntityType.VILLAGER;

        LookClose close = new LookClose();
        close.lookClose(true);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(shop, "SHOP");
        if (shop == EntityType.PLAYER && skin != null)
            skin(npc, skin);
        npc.spawn(this.getShopLocation());
        npc.addTrait(close);

        return npc;
    }

    public NPC spawnUpgradeNPC() {
        EntityType shop = null;
        String type;
        String skin = null;

        if (this.getPlayers().size() > 0) {
            Random random = new Random();
            PlayerHandler playerHandler = this.getPlayers().get(random.nextInt(this.getPlayers().size()));
            type = new DataHandler(playerHandler.getPlayer()).getPlayerSelectedCosmetic("upgrade_npcs");
            ConfigurationSection section = Variables.cosmetics.getConfigurationSection("upgrade_npcs." + type);
            if (section != null) {
                shop = type != null && !type.equals("") ? EntityType.valueOf(section.getString("entity")) : EntityType.valueOf(Variables.cosmetics.getString("upgrade_npcs.default.entity"));
                if (section.getString("skin") != null && !section.getString("skin").equals("")) skin = section.getString("skin").replace("%player%", playerHandler.getPlayer().getName());
            }
        } else
            shop = EntityType.valueOf(Variables.cosmetics.getString("upgrade_npcs.default.entity"));

        if (shop == null) shop = EntityType.VILLAGER;

        LookClose close = new LookClose();
        close.lookClose(true);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(shop, "UPGRADES");
        if (shop == EntityType.PLAYER && skin != null)
            skin(npc, skin);
        npc.spawn(this.getUpgradeLocation());
        npc.addTrait(close);

        return npc;
    }

    public void bedMessages(Player player) {
        DataHandler data = new DataHandler(player);
        String text = data.getPlayerSelectedCosmetic("kill_messages");
        if (text != null && text.equalsIgnoreCase("random"))
            text = data.getPlayerUnlockedCosmetics("kill_messages").get(new Random().nextInt(data.getPlayerUnlockedCosmetics("kill_messages").size()));
        if (text == null) text = "default";

        data.addPlayerStat("broken_beds", 1);
        ConfigurationSection teamSection = Variables.config.getConfigurationSection("teams." + this.name);
        if (teamSection == null) return;

        ConfigurationSection section = Variables.cosmetics.getConfigurationSection("kill_messages." + text + ".bed_messages");
        if (section == null) return;

        List<String> bedBroke = section.getStringList("bed_broke");
        List<String> bedBroken = section.getStringList("bed_broken");
        List<String> bedDestroy = section.getStringList("bed_destroy");
        if (bedBroke != null && bedBroke.size()>0)
            for (String line : bedBroke) {
                player.sendMessage(Utils.colorize(line
                        .replace("%broken_beds%", String.valueOf(data.getPlayerStat("broken_beds")))
                        .replace("%team_short%", teamSection.getString("short"))
                        .replace("%team_name%", teamSection.getString("name"))
                        .replace("%team_color%", teamSection.getString("color"))));
            }

        if (bedBroken != null && bedBroken.size()>0)
            for (String line : bedBroken) {
                send(Utils.colorize(line
                        .replace("%broken_beds%", String.valueOf(data.getPlayerStat("broken_beds")))
                        .replace("%team_short%", teamSection.getString("short"))
                        .replace("%team_name%", teamSection.getString("name"))
                        .replace("%team_color%", teamSection.getString("color"))));
            }

        GameManager manager = DeluxeBedwars.getInstance().getGameManager();
        ArenaHandler arena = manager.getArena(player);
        if (arena == null) return;
        TeamHandler team = manager.getTeam(player);
        if (team == null) return;

        ConfigurationSection playerSection = Variables.config.getConfigurationSection("teams." + team.getName());
        if (playerSection == null) return;
        if (bedDestroy != null && bedDestroy.size()>0)
            for (String line : bedDestroy) {
                arena.send(Utils.colorize(line
                        .replace("%broken_beds%", String.valueOf(data.getPlayerStat("broken_beds")))
                        .replace("%player_short%", playerSection.getString("short"))
                        .replace("%player_color%", playerSection.getString("color"))
                        .replace("%player_name%", player.getName())
                        .replace("%team_short%", teamSection.getString("short"))
                        .replace("%team_name%", teamSection.getString("name"))
                        .replace("%team_color%", teamSection.getString("color"))));
            }
    }

    public void skin(NPC npc, String skinName) {
        if (skinName == null) return;

        npc.data().set(NPC.PLAYER_SKIN_UUID_METADATA, skinName);
    }

    public Location getFirstLocation() {
        return this.firstLocation;
    }

    public Location getSecondLocation() {
        return this.secondLocation;
    }

    public List<PlayerHandler> getPlayers() {
        return this.players;
    }

    public List<PlayerHandler> getAlivePlayers() {
        return this.players.stream().filter(PlayerHandler::isAlive).collect(Collectors.toList());
    }

    public void removePlayer(PlayerHandler player) {
        this.players.remove(player);
    }

    public String getName() {
        return this.name;
    }

    public void addPlayer(PlayerHandler player) {
        this.players.add(player);
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public Location getShopLocation() {
        return this.shopLocation;
    }

    public Location getUpgradeLocation() {
        return this.upgradeLocation;
    }

    public Location getBedLocation() {
        return this.bedLocation;
    }

    public Boolean getBedStatus() {
        return this.bedStatus;
    }

    public void setBedStatus(Boolean bool) {
        this.bedStatus = bool;
    }

    public UpgradesHandler getUpgrades() {
        return this.upgrades;
    }
}