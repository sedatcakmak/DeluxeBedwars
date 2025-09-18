package me.sedattr.bedwars.managers;

import com.hakan.scoreboard.api.ScoreboardAPI;
import com.hakan.scoreboard.scoreboard.ScoreBoard;
import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.*;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Scoreboards {
    private final ArenaHandler arena;

    public Scoreboards(ArenaHandler arena) {
        this.arena = arena;

        SimpleDateFormat dateFormat = new SimpleDateFormat(Variables.config.getString("messages.date"));
        Variables.date = dateFormat.format(new Date());

        SimpleDateFormat timeFormat = new SimpleDateFormat(Variables.config.getString("messages.time"));
        Variables.time = timeFormat.format(new Date());
    }

    public void lobbyScoreboard(Player player) {
        ConfigurationSection section = Variables.config.getConfigurationSection("scoreboards.lobby");
        if (!section.getBoolean("enabled"))
            return;

        DataHandler data = new DataHandler(player);
        String prestige = data.getPlayerPrestige();

        ScoreboardAPI.ScoreboardManager scoreboardManager = ScoreboardAPI.getManager();
        scoreboardManager.setTitle(Utils.colorize(section.getString("title")));
        int i = section.getStringList("lines").size();
        for (String line : section.getStringList("lines")) {
            scoreboardManager.setLine(i, Utils.colorize(line
                    .replace("%time%", Variables.time)
                    .replace("%prestige%", prestige != null ? prestige : "")
                    .replace("%date%", Variables.date)
                    .replace("%level%", Utils.format(data.getPlayerInfo("level")))
                    .replace("%coins%", Utils.format(data.getPlayerInfo("money")))
                    .replace("%xp%", Utils.format(data.getPlayerInfo("xp")))
                    .replace("%max_xp%", "100")
                    .replace("%chests%", Utils.format(data.getPlayerInfo("chests")))
                    .replace("%total_wins%", Utils.format(data.getPlayerStat("wins")))
                    .replace("%total_kills%", Utils.format(data.getPlayerStat("final_kills") + data.getPlayerStat("kills")))
                    .replace("%version%", DeluxeBedwars.getInstance().getDescription().getVersion())));
            i--;
        }
        scoreboardManager.setPlayer(player);
        ScoreBoard scoreBoard = scoreboardManager.create();
        scoreBoard.open();

        Variables.boards.put(player, scoreBoard);
    }

    public void waitingScoreboard(int second) {
        ConfigurationSection waitingConfiguration = Variables.config.getConfigurationSection("scoreboards.waiting");
        if (!waitingConfiguration.getBoolean("enabled")) return;

        ScoreboardAPI.ScoreboardManager scoreboardManager = ScoreboardAPI.getManager();
        scoreboardManager.setTitle(Utils.colorize(waitingConfiguration.getString("title")));
        int i = waitingConfiguration.getStringList("lines").size();
        for (String line : waitingConfiguration.getStringList("lines")) {
            scoreboardManager.setLine(i, Utils.colorize(line
                    .replace("%time%", Variables.time)
                    .replace("%date%", Variables.date)
                    .replace("%map%", arena.getName())
                    .replace("%arena_number%", String.valueOf(DeluxeBedwars.getInstance().getGameManager().getArenas().indexOf(this.arena)+1))
                    .replace("%mode%", Variables.messages.getString("types." + arena.getType()) != null ? Variables.messages.getString("types." + arena.getType()) : arena.getType())
                    .replace("%version%", DeluxeBedwars.getInstance().getDescription().getVersion())
                    .replace("%players%", String.valueOf(arena.getPlayers().size()))
                    .replace("%max_players%", String.valueOf(arena.getMaxPlayers()))
                    .replace("%status%", arena.getPlayers().size() >= arena.getMinPlayers() ? waitingConfiguration.getString("status.starting").replace("%time%", String.valueOf(second)) : waitingConfiguration.getString("status.waiting"))));
            i--;
        }

        for (PlayerHandler playerHandler : this.arena.getPlayers()) {
            scoreboardManager.setPlayer(playerHandler.getPlayer());
            ScoreBoard scoreBoard = scoreboardManager.create();
            scoreBoard.open();

            Variables.boards.put(playerHandler.getPlayer(), scoreBoard);
        }
    }

    public void playingScoreboard() {
        ConfigurationSection playingConfiguration = Variables.config.getConfigurationSection("scoreboards.playing");
        if (!playingConfiguration.getBoolean("enabled")) return;

        for (PlayerHandler playerHandler : arena.getPlayers()) {
            ArrayList<String> lines = new ArrayList<>();
            for (String line : playingConfiguration.getStringList("lines")) {
                if (line.contains("%status%")) {
                    for (TeamHandler team : this.arena.getTeams()) {
                        if (!playingConfiguration.getBoolean("show_all_teams") && !team.getBedStatus() && team.getAlivePlayers().size() < 1)
                            continue;

                        ConfigurationSection section = Variables.config.getConfigurationSection("teams." + team.getName());
                        if (section == null) continue;
                        String emoji = playingConfiguration.getString("status.alive").replace("%count%", String.valueOf(team.getAlivePlayers().size()));
                        if (team.getBedStatus()) emoji = playingConfiguration.getString("status.bed");
                        else if (team.getAlivePlayers().size() <= 0)
                            emoji = playingConfiguration.getString("status.dead");

                        String you = team.getName().equalsIgnoreCase(playerHandler.getTeam().getName()) ? playingConfiguration.getString("status.you") : "";
                        lines.add(Utils.colorize(playingConfiguration.getString("status.text")
                                .replace("%you%", you)
                                .replace("%name%", section.getString("name"))
                                .replace("%short%", section.getString("short"))
                                .replace("%status%", emoji)
                                .replace("%color%", section.getString("color"))));
                    }
                    continue;
                }

                lines.add(Utils.colorize(line
                        .replace("%time%", Variables.time)
                        .replace("%map%", this.arena.getName())
                        .replace("%arena_number%", String.valueOf(DeluxeBedwars.getInstance().getGameManager().getArenas().indexOf(this.arena) + 1))
                        .replace("%date%", Variables.date)
                        .replace("%next_event%", this.arena.getNextEvent().getEventName())
                        .replace("%event_time%", String.valueOf(this.arena.getNextEvent().getEventTime()))));
            }

            ScoreboardAPI.ScoreboardManager scoreboardManager = ScoreboardAPI.getManager();
            scoreboardManager.setTitle(Utils.colorize(playingConfiguration.getString("title")));
            int i = lines.size();
            for (String line : lines) {
                scoreboardManager.setLine(i, line);
                i--;
            }
            scoreboardManager.setPlayer(playerHandler.getPlayer());
            ScoreBoard scoreBoard = scoreboardManager.create();
            scoreBoard.open();

            Variables.boards.put(playerHandler.getPlayer(), scoreBoard);
        }
    }

    public void removeBoard(Player player) {
        ScoreBoard board = Variables.boards.remove(player);

        if (board != null)
            board.close();
    }
}
