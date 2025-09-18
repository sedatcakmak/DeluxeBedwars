package me.sedattr.bedwars.handlers;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.cosmetics.Cosmetics;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Events;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.Countdown;
import me.sedattr.bedwars.managers.Items;
import me.sedattr.bedwars.managers.PlayerManager;
import me.sedattr.bedwars.managers.Scoreboards;
import me.sedattr.bedwars.nbtapi.NBTItem;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class ArenaHandler {
    @Getter private final List<String> brokenBlocks = new ArrayList<>();
    @Getter private final List<Block> placedBlocks = new ArrayList<>();
    @Getter private final Map<Player, Integer> kills = Maps.newHashMap();
    @Getter private final List<Player> hiddenPlayers = new ArrayList<>();
    @Getter private String name;
    @Getter private Integer maxPlayers;
    @Getter private Integer minPlayers;
    @Getter private String type;
    @Getter private String mode;
    @Getter private final ArrayList<TeamHandler> teams = new ArrayList<>();
    @Getter private Enums.ArenaState state = Enums.ArenaState.WAITING;
    @Getter private final List<PlayerHandler> players = new ArrayList<>();
    @Getter private final HashMap<Player, Location> oldLocations = new HashMap<>();
    @Getter private final HashMap<Player, String> oldTablist = new HashMap<>();
    @Getter private Objective health;
    @Getter private YamlConfiguration configuration;
    @Getter private Location spectateLocation;
    @Getter private Location maxLocation;
    @Getter private Location minLocation;
    @Getter private World world;
    @Getter private final List<Location> diamondGenerators = new ArrayList<>();
    @Getter private final List<Location> emeraldGenerators = new ArrayList<>();
    @Getter private final List<NPC> NPCs = new ArrayList<>();
    @Getter private final List<EnderDragon> dragons = new ArrayList<>();
    @Getter private Boolean enabled = true;
    @Getter private Events nextEvent;
    @Getter private Location waitingLocation;

    public ArenaHandler(String name) {
        File file = new File(DeluxeBedwars.getInstance().getDataFolder() + "/arenas", name + ".yml");
        if (file == null) return;

        this.configuration = YamlConfiguration.loadConfiguration(file);
        this.maxPlayers = this.configuration.getInt("settings.max_players");
        this.minPlayers = this.configuration.getInt("settings.min_players");
        this.type = this.configuration.getString("settings.type");
        this.mode = this.configuration.getString("settings.mode");
        this.name = name;
        this.enabled = this.configuration.getBoolean("enabled");
        this.world = Bukkit.getWorld(this.configuration.getString("settings.world"));
        this.nextEvent = new Events(this);

        ConfigurationSection teamConfiguration = this.configuration.getConfigurationSection("teams");
        if (teamConfiguration == null) {
            Bukkit.getConsoleSender().sendMessage("§8[§bDeluxeBedwars§8] §cArena named " + this.name + " haven't any team!");
            return;
        }

        for (String entry : teamConfiguration.getKeys(false)) {
            try {
                this.teams.add(new TeamHandler(name, entry));
            } catch (NullPointerException e) {
                Bukkit.getConsoleSender().sendMessage("§8[§bDeluxeBedwars§8] §cThere is a problem with setting team " + entry + " for arena " + this.name + ":");
                e.printStackTrace();
            }
        }

        for (Object loc : this.configuration.getList("generators.diamond"))
            this.diamondGenerators.add((Location) loc);

        for (Object loc : this.configuration.getList("generators.emerald"))
            this.emeraldGenerators.add((Location) loc);

        this.waitingLocation = (Location) this.configuration.get("settings.waiting");
        this.spectateLocation = (Location) this.configuration.get("settings.spectate");
        this.maxLocation = (Location) this.configuration.get("settings.max");
        this.minLocation = (Location) this.configuration.get("settings.min");

        DeluxeBedwars.getInstance().getGameManager().addArena(this);
    }

    public void addPlacedBlock(Block block) {
        this.placedBlocks.add(block);
    }

    public void addBrokenBlock(String block) {
        this.brokenBlocks.add(block);
    }

    public void addHiddenPlayer(Player player) {
        this.hiddenPlayers.add(player);
    }

    public void removeHiddenPlayer(Player player) {
        this.hiddenPlayers.remove(player);
    }

    public void setState(Enums.ArenaState state) {
        this.state = state;
    }

    public void leave(Player p) {
        TeamHandler team = DeluxeBedwars.getInstance().getGameManager().getTeam(p);
        PlayerHandler handler = getPlayer(p);

        PartyHandler party = DeluxeBedwars.getInstance().getGameManager().getPartyOwner(p);
        if (party != null)
            party.leave();

        if (team != null) team.removePlayer(handler);
        DeluxeBedwars.getInstance().getGameManager().removePlayer(p);
        removePlayer(handler);

        p.sendMessage(Utils.sendMessage(null, "left_player").replace("%name%", this.name));
        PlayerManager manager = new PlayerManager(p);
        manager.resetPlayer(false);
        manager.giveLobbyItems();

        checkCount();
        checkWinner();
        new Scoreboards(this).removeBoard(p);
    }

    public void removePlayer(PlayerHandler playerHandler) {
        if (playerHandler == null) return;
        Player p = playerHandler.getPlayer();

        this.players.remove(playerHandler);
        DeluxeBedwars.getInstance().getGameManager().removePlayer(playerHandler.getPlayer());
        p.setGameMode(GameMode.SURVIVAL);
        p.setFlying(false);
        p.setAllowFlight(false);
        teleportBack(p, "leave");

        if (playerHandler.isAlive()) send(Variables.messages.getString("left_all")
                .replace("%display_name%", playerHandler.getPlayer().getDisplayName())
                .replace("%name%", playerHandler.getPlayer().getName())
                .replace("%players%", String.valueOf(this.players.size()))
                .replace("%max_players%", String.valueOf(this.maxPlayers)));

        checkCount();
    }

    public void checkCount() {
        if (!this.state.equals(Enums.ArenaState.WAITING) && !this.state.equals(Enums.ArenaState.STARTING)) return;
        if (this.players.size() < this.minPlayers) new Scoreboards(this).waitingScoreboard(0);

        boolean old = Countdown.startTasks.containsKey(this);
        if (this.players.size() >= this.minPlayers && !old)
            new Countdown.StartCountdown(this).runTaskTimer(DeluxeBedwars.getInstance(), 0, 20);
        else if (this.players.size() < this.minPlayers && old) {
            Countdown.startTasks.get(this).end();
            setState(Enums.ArenaState.WAITING);
        }
    }

    public PlayerHandler getPlayer(Player player) {
        for (PlayerHandler p : this.players) {
            if (p == null) continue;
            if (p.getPlayer().getName().equals("")) continue;

            if (p.getPlayer().getName().equalsIgnoreCase(player.getName())) return p;
        }

        return null;
    }

    public void join(Player player) {
        if (!this.enabled) {
            Utils.sendMessage(player, "arena_disabled");
            return;
        }

        if (!getState().equals(Enums.ArenaState.WAITING) && !getState().equals(Enums.ArenaState.STARTING)) {
            Utils.sendMessage(player, "arena_started");
            return;
        }

        if (this.players.size() >= getMaxPlayers()) {
            Utils.sendMessage(player, "arena_full");
            return;
        }

        if (DeluxeBedwars.getInstance().getGameManager().getArena(player) != null) {
            Utils.sendMessage(player, "already_playing");
            return;
        }

        PartyHandler party = DeluxeBedwars.getInstance().getGameManager().getParty(player);
        if (party != null) {
            if (party.getOwner() != player) {
                Utils.sendMessage(player, "not_owner");
                return;
            }

            for (Player p : party.getPlayers())
                addPlayer(p);
            return;
        }

        if (getPlayer(player) == null)
            addPlayer(player);
    }

    public void addPlayer(Player player) {
        if (Variables.hiddenPlayers.contains(player)) {
            for (PlayerHandler playerHandler : this.players) {
                playerHandler.getPlayer().showPlayer(player);
                player.showPlayer(playerHandler.getPlayer());
            }

            Variables.hiddenPlayers.remove(player);
        }

        new PlayerManager(player).resetPlayer(false);
        DataHandler data = new DataHandler(player);
        for (String entry : Variables.cosmetics.getKeys(false)) {
            if (!data.getRandom(entry)) continue;

            List<String> cosmetics = data.getPlayerUnlockedCosmetics(entry);
            cosmetics.add("default");

            if (cosmetics.size() > 0)
                data.setPlayerSelectedCosmetic(entry, cosmetics.get(new Random().nextInt(cosmetics.size())));
        }

        ConfigurationSection itemList = Variables.items.getConfigurationSection("game_items");
        if (itemList != null && itemList.getKeys(false).size() > 0) for (String entry : itemList.getKeys(false)) {
            ConfigurationSection section = itemList.getConfigurationSection(entry);
            ItemStack item = new Items(player, this).createItem(section, null);

            NBTItem nbti = new NBTItem(item);
            nbti.setString("BedwarsINVENTORYITEM", "game_items." + entry);
            player.getInventory().setItem(section.getInt("slot")-1, nbti.getItem());
        }

        DeluxeBedwars.getInstance().getGameManager().addPlayer(player, this, null);
        this.players.add(new PlayerHandler(player, this));
        player.setGameMode(GameMode.ADVENTURE);

        this.oldLocations.put(player, player.getLocation());
        player.teleport(this.waitingLocation);

        send(Variables.messages.getString("joined_all")
                .replace("%display_name%", player.getDisplayName())
                .replace("%name%", player.getName())
                .replace("%players%", String.valueOf(this.players.size()))
                .replace("%max_players%", String.valueOf(this.maxPlayers)));
        checkCount();
    }

    public void endGame() {
        if (this.state.equals(Enums.ArenaState.WAITING) || this.state.equals(Enums.ArenaState.STARTING) || this.state.equals(Enums.ArenaState.ENDED)) return;
        send(Utils.sendMessage(null, "nobody_won"));

        this.state = Enums.ArenaState.ENDED;
        teleportPlayers(false);
        Bukkit.getScheduler().runTaskLater(DeluxeBedwars.getInstance(), this::reloadArena, 350L);
    }

    public void teleportBack(Player player, String type) {
        boolean teleportToLobby = Variables.config.getBoolean("lobby.teleport_to_lobby." + type);

        if (!teleportToLobby || Variables.lobby == null) {
            Location oldLocation = this.oldLocations.get(player);
            if (oldLocation != null)
                player.teleport(oldLocation);
        } else
            player.teleport(Variables.lobby);
    }

    public void addDragon(EnderDragon dragon) {
        this.dragons.add(dragon);
    }

    public void reloadArena() {
        if (Variables.armorStands.containsKey(this)) Variables.armorStands.remove(this).forEach((key, value) -> value.remove());
        if (Variables.tasks.containsKey(this)) Variables.tasks.remove(this).forEach(BukkitTask::cancel);
        for (TeamHandler teamHandler : this.getTeams()) {
            if (teamHandler.getIslandTasks().size() > 0) teamHandler.getIslandTasks().forEach(BukkitTask::cancel);
            if (teamHandler.getEntities().size() > 0) teamHandler.getEntities().forEach((key, value) -> {
                key.remove();
                value.cancel();
            });
        }

        if (Variables.otherTasks.containsKey(this)) Variables.otherTasks.remove(this).forEach(BukkitTask::cancel);
        if (Variables.textStands.containsKey(this)) Variables.textStands.remove(this).forEach((key, value) -> value.forEach(Entity::remove));
        this.NPCs.forEach(NPC::despawn);

        DeluxeBedwars.getInstance().getGameManager().getArenas().remove(this);
        if (this.nextEvent != null) {
            BukkitTask task = this.nextEvent.getTask();
            if (task != null) task.cancel();
        }
        if (Countdown.arenaTasks.containsKey(this)) Countdown.arenaTasks.remove(this).end();

        deleteItems();
        for (String entry : this.brokenBlocks) {
            String[] args = entry.split("[:]", 6);
            if (args.length < 6) continue;

            Block oldBlock = this.world.getBlockAt(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            Chunk chunk = oldBlock.getChunk();
            if (!chunk.isLoaded())
                chunk.load();

            if (Material.getMaterial(args[5]) != null)
                oldBlock.setType(Material.getMaterial(args[5]), false);
            if (Integer.parseInt(args[3]) > 0)
                oldBlock.setTypeId(Integer.parseInt(args[3]));
            if (Integer.parseInt(args[4]) > 0)
                oldBlock.setData(Byte.parseByte(args[4]), false);
        }

        for (Block block : this.placedBlocks) {
            Chunk chunk = block.getChunk();
            if (!chunk.isLoaded())
                chunk.load();

            if (block != null && !block.getType().equals(Material.AIR))
                block.setType(Material.AIR);
        }

        if (DeluxeBedwars.getInstance().isEnabled())
            new ArenaHandler(this.name);
    }

    public Integer getKill(Player p) {
        return this.kills.getOrDefault(p, 0);
    }

    public void addKill(Player p, Integer count) {
        this.kills.put(p, getKill(p)+count);
    }

    public Map<Player, Integer> sortKills() {
        return this.kills.entrySet().stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public void teleportPlayers(Boolean unload) {
        this.dragons.forEach(EnderDragon::remove);
        if (this.health != null && this.health.isModifiable())
            this.health.unregister();

        for (PlayerHandler playerHandler : this.players) {
            Player p = playerHandler.getPlayer();
            if (!p.getWorld().equals(this.world)) continue;

            ArenaHandler oldArena = DeluxeBedwars.getInstance().getGameManager().getArena(p);
            if (oldArena != null && !oldArena.getName().equalsIgnoreCase(this.name)) continue;

            new PlayerManager(p).resetPlayer(false);
            if (this.oldTablist.containsKey(p))
                p.setPlayerListName(this.oldTablist.get(p));

            PlayerManager playerManager = new PlayerManager(p);
            if (!unload && playerHandler.isAlive()) new Cosmetics(p, p.getLocation(), p.getLocation(), true, "victory_dances").start();
            if (unload) {
                p.setFlying(false);
                p.setAllowFlight(false);

                playerManager.resetPlayer(false);
                playerManager.giveLobbyItems();
                new Scoreboards(this).removeBoard(p);
                teleportBack(p, "end");
            } else {
                Bukkit.getScheduler().runTaskLater(DeluxeBedwars.getInstance(), () -> {
                    p.setFlying(false);
                    p.setAllowFlight(false);

                    playerManager.resetPlayer(false);
                    playerManager.giveLobbyItems();

                    this.players.remove(playerHandler);
                    new Scoreboards(this).removeBoard(p);
                    teleportBack(p, "end");

                    new BukkitRunnable() {
                        public void run() {
                            DeluxeBedwars.getInstance().getGameManager().removePlayer(p);
                        }
                    }.runTaskLater(DeluxeBedwars.getInstance(), 20L);
                }, 300L);
            }
        }
    }

    public void checkWinner() {
        if (this.state.equals(Enums.ArenaState.WAITING) || this.state.equals(Enums.ArenaState.STARTING)) return;

        List<TeamHandler> teams = new ArrayList<>();
        for (TeamHandler team : this.teams) {
            int aliveCount = team.getAlivePlayers().size();
            if (aliveCount > 0) teams.add(team);
        }

        if (teams.size() <= 1) {
            if (!this.hiddenPlayers.isEmpty())
                for (Player player : this.hiddenPlayers) {
                    if (getPlayer(player) == null) continue;

                    for (PlayerHandler playerHandler : this.players) {
                        playerHandler.getPlayer().showPlayer(player);
                        player.showPlayer(playerHandler.getPlayer());
                    }
                }

            this.state = Enums.ArenaState.ENDED;
            if (teams.size() == 1) {
                Map<Player, Integer> kills = sortKills();
                for (String line : Variables.messages.getStringList("ended.lines")) {
                    if (line.contains("%list%")) {
                        int i = 1;
                        if (kills.size() > 0) for (Map.Entry<Player, Integer> entry : kills.entrySet()) {
                            if (i > Math.max(Variables.messages.getInt("ended.limit"), 3)) continue;

                            Player player = entry.getKey();
                            Integer count = entry.getValue();
                            send(Utils.centeredMessage(Variables.messages.getString("ended.text")
                                    .replace("%format%", Variables.messages.getString("ended.format." + i) != null ? Variables.messages.getString("ended.format." + i) : "")
                                    .replace("%display_name%", player.getDisplayName())
                                    .replace("%name%", player.getName())
                                    .replace("%kills%", String.valueOf(count))));
                            i++;
                        }

                        continue;
                    }

                    ConfigurationSection section = Variables.config.getConfigurationSection("teams." + teams.get(0).getName());
                    send(Utils.centeredMessage(line
                            .replace("%winner_display_names%", teams.get(0).getPlayers().get(0).getPlayer().getDisplayName())
                            .replace("%team_color%", section.getString("color"))
                            .replace("%team_name%", section.getString("name"))
                            .replace("%team_short%", section.getString("short"))));
                }
            }

            teleportPlayers(false);
            Bukkit.getScheduler().runTaskLater(DeluxeBedwars.getInstance(), this::reloadArena, 350L);
        }
    }

    public void send(String text) {
        for (PlayerHandler playerHandler : this.players)
            playerHandler.getPlayer().sendMessage(Utils.colorize(text));
    }

    public TeamHandler getTeam(String name) {
        for (TeamHandler team : this.teams) {
            if (team.getName().equalsIgnoreCase(name)) return team;
        }

        return null;
    }

    public void deleteItems() {
        for (Location loc : this.diamondGenerators) {
            for (Entity entity : this.world.getNearbyEntities(loc, 10, 10, 10))  {
                if (entity instanceof Item)
                    entity.remove();
            }
        }

        for (Location loc : this.emeraldGenerators) {
            for (Entity entity : this.world.getNearbyEntities(loc, 10, 10, 10))  {
                if (entity instanceof Item)
                    entity.remove();
            }
        }

        for (TeamHandler team : this.teams) {
            for (Entity entity : this.world.getNearbyEntities(team.getSpawnLocation(), 20, 20, 20))  {
                if (entity instanceof Item)
                    entity.remove();
            }
        }
    }

    public void startGame() {
        if (this.name == null) return;

        deleteItems();
        int teamSize = this.teams.size();
        int count = 0;

        Scoreboard s = null;
        if (Variables.messages.getBoolean("health_bar.enabled")) {
            ScoreboardManager sm = Bukkit.getScoreboardManager();
            s = sm.getNewScoreboard();
            Objective h = s.registerNewObjective("showhealth", Criterias.HEALTH);
            h.setDisplaySlot(DisplaySlot.BELOW_NAME);
            h.setDisplayName(Utils.colorize(Variables.messages.getString("health_bar.style")));
            this.health = h;
        }

        for (PlayerHandler playerHandler : this.players) {
            Random rand = new Random();
            int empty_team = count / teamSize;
            TeamHandler team = this.teams.get(rand.nextInt(teamSize));
            while (team.getPlayers().size() != empty_team || team.getPlayers().size() >= team.getMaxPlayers())
                team = this.teams.get(rand.nextInt(teamSize));
            count++;

            if (team == null) break;
            if (playerHandler == null) continue;
            Player p = playerHandler.getPlayer();
            p.closeInventory();

            ConfigurationSection section = Variables.config.getConfigurationSection("teams." + team.getName());
            if (Variables.messages.getBoolean("tablist.enabled") && section != null) {
                this.oldTablist.put(p, p.getPlayerListName());

                p.setPlayerListName(Utils.colorize(Variables.messages.getString("tablist.style")
                        .replace("%teamcolor%", section.getString("color"))
                        .replace("%teamname%", section.getString("name"))
                        .replace("%teamshort%", section.getString("short"))
                        .replace("%name%", p.getName())
                        .replace("%display_name%", p.getDisplayName())));
            }

            if (s != null && !CitizensAPI.getNPCRegistry().isNPC(p))
                p.setScoreboard(s);

            team.addPlayer(playerHandler);
            DeluxeBedwars.getInstance().getGameManager().addPlayer(p, this, team);
            Countdown.deathTasks.remove(p);
            new PlayerManager(p).spawn(true);
            playerHandler.setTeam(team);

            for (String line : Variables.messages.getStringList("started")) {

                p.sendMessage(Utils.centeredMessage(line
                        .replace("%team_color%", section.getString("color"))
                        .replace("%team_name%", section.getString("name"))
                        .replace("%team_short%", section.getString("short"))
                        .replace("%kit%", "Default")));
            }
        }

        for (TeamHandler team : this.teams) {
            if (team.getPlayers().size() < 1) team.setBedStatus(false);
            new GeneratorHandler(this).islandGenerator(team);

            NPCs.add(team.spawnShopNPC());
            NPCs.add(team.spawnUpgradeNPC());
        }

        new GeneratorHandler(this).startOtherGenerators();
        DeluxeBedwars.getInstance().getGameManager().addArena(this);
        Countdown.StartCountdown countdown = Countdown.startTasks.getOrDefault(this, null);
        if (countdown != null) countdown.end();
        Countdown.GameCountdown countdown2 = Countdown.arenaTasks.getOrDefault(this, null);
        if (countdown2 != null) countdown2.end();
        this.nextEvent.startTask();

        this.state = Enums.ArenaState.PLAYING;
        new Countdown.GameCountdown(this).runTaskTimer(DeluxeBedwars.getInstance(), 0, 15L);
        //checkWinner();
    }
}