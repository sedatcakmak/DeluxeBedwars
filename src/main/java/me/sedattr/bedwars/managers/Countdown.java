package me.sedattr.bedwars.managers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.PlayerHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class Countdown {
    public static final HashMap<ArenaHandler, StartCountdown> startTasks = new HashMap<>();
    public static final HashMap<ArenaHandler, GameCountdown> arenaTasks = new HashMap<>();
    public static final HashMap<Player, DeathCountdown> deathTasks = new HashMap<>();
    public static final HashMap<Player, LeaveCountdown> leaveTasks = new HashMap<>();

    public static class StartCountdown extends BukkitRunnable {
        private int time;
        private final ArenaHandler arena;

        public StartCountdown(ArenaHandler arena) {
            this.arena = arena;
            this.time = 15;

            this.arena.setState(Enums.ArenaState.STARTING);
            startTasks.put(this.arena, this);
        }

        public void end() {
            cancel();
            startTasks.remove(this.arena);
        }

        @Override
        public void run() {
            for (PlayerHandler playerHandler : this.arena.getPlayers())
                playerHandler.getPlayer().setLevel(this.time);

            if (this.time > 0) new Scoreboards(this.arena).waitingScoreboard(this.time);
            if (this.time < 1) {
                end();

                this.arena.startGame();
            } else if (this.time == 20 || this.time == 10 || this.time <= 5) {
                String color = "&c";
                if (this.time == 20) color = "&e";
                else if (this.time == 10) color = "&6";

                this.arena.send(Variables.messages.getString("starting")
                        .replace("%color%", color)
                        .replace("%time%", String.valueOf(this.time)));
            }

            this.time -= 1;
        }
    }

    public static class GameCountdown extends BukkitRunnable {
        private final ArenaHandler arena;

        public GameCountdown(ArenaHandler arena) {
            this.arena = arena;

            arenaTasks.put(arena, this);
        }

        public void end() {
            cancel();

            for (PlayerHandler playerHandler : this.arena.getPlayers())
                new Scoreboards(this.arena).removeBoard(playerHandler.getPlayer());
        }

        @Override
        public void run() {
            new Scoreboards(this.arena).playingScoreboard();
        }
    }

    public static class LeaveCountdown extends BukkitRunnable {
        private int time;
        private final Player player;
        private final ArenaHandler arena;

        public LeaveCountdown(Player player, ArenaHandler arena) {
            this.player = player;
            this.arena = arena;
            this.time = 3;
        }

        public void end() {
            leaveTasks.remove(this.player);
            cancel();

            this.player.sendMessage(Utils.colorize(Variables.messages.getString("cancelled")));
        }

        @Override
        public void run() {
            if (!leaveTasks.containsKey(player)) leaveTasks.put(player, this);

            if (this.time < 1) {
                cancel();
                leaveTasks.remove(this.player);
                this.arena.leave(this.player);
            } else {
                String color = "&c";
                if (this.time == 3) color = "&e";
                else if (this.time == 2) color = "&6";

                this.player.sendMessage(Utils.colorize(Variables.messages.getString("teleporting")
                        .replace("%color%", color)
                        .replace("%time%", String.valueOf(this.time))));
            }

            this.time -= 1;
        }
    }

    public static class DeathCountdown extends BukkitRunnable {
        private int time;
        private final Player player;
        private final ArenaHandler arena;

        public DeathCountdown(Player player) {
            this.player = player;
            this.time = 5;

            player.setAllowFlight(true);
            player.setFlying(true);
            this.arena = DeluxeBedwars.getInstance().getGameManager().getArena(this.player);
        }

        @Override
        public void run() {
            if (this.arena == null || this.arena.getState() == Enums.ArenaState.ENDED) cancel();

            if (this.time < 1) {
                if (this.arena != null) {
                    this.arena.removeHiddenPlayer(this.player);
                    this.player.showPlayer(this.player);
                    for (PlayerHandler playerHandler : this.arena.getPlayers())
                        playerHandler.getPlayer().showPlayer(this.player);
                }

                cancel();
                new PlayerManager(this.player).spawn(false);

                this.player.setGameMode(GameMode.SURVIVAL);
                this.player.setFlying(false);
                this.player.setAllowFlight(false);

                this.player.setNoDamageTicks(Math.max(Variables.config.getInt("settings.spawn_protection"), 1)*20);

                Utils.sendMessage(this.player, "respawned");
                if (this.arena != null && this.arena.getState() != Enums.ArenaState.ENDED) deathTasks.remove(this.player);
            } else {
                if (!deathTasks.containsKey(this.player)) deathTasks.put(this.player, this);

                String color = "&c";
                if (this.time > 4) color = "&e";
                else if (this.time > 2) color = "&6";

                this.player.sendMessage(Utils.colorize(Utils.sendMessage(null, "respawning")
                        .replace("%color%", color)
                        .replace("%time%", String.valueOf(this.time))));
            }

            this.time -= 1;
        }
    }
}
