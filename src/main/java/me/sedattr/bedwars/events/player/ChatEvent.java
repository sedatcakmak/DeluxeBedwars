package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.GameManager;
import me.sedattr.bedwars.handlers.PlayerHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.Countdown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {
    private final DeluxeBedwars plugin;

    public ChatEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        GameManager game = this.plugin.getGameManager();
        PlayerHandler playerHandler = game.getPlayer(player);
        if (playerHandler == null) return;

        ArenaHandler arena = game.getArena(player);
        TeamHandler team = game.getTeam(player);
        if (arena == null) return;


        String message = null;
        if (arena.getState() == Enums.ArenaState.ENDED) message = Variables.config.getString("format.chat.end");
        else if (arena.getState() == Enums.ArenaState.WAITING || arena.getState() == Enums.ArenaState.STARTING) message = Variables.config.getString("format.chat.lobby");
        else if (Countdown.deathTasks.containsKey(player) || !playerHandler.isAlive()) message = Variables.config.getString("format.chat.spectator");
        else if (arena.getState() == Enums.ArenaState.PLAYING) {
            if (e.getMessage().startsWith("!")) message = Variables.config.getString("format.chat.all");
            else message = Variables.config.getString("format.chat.team");
        }
        if (message == null || message.equals("")) return;
        e.setCancelled(true);

        message = message.replace("%name%", player.getName())
                .replace("%display_name%", player.getDisplayName())
                .replace("%message%", e.getMessage().startsWith("!") && arena.getState() == Enums.ArenaState.PLAYING && playerHandler.isAlive() && !Countdown.deathTasks.containsKey(player) ? e.getMessage().substring(1) : e.getMessage());

        if (team != null) message = message
                .replace("%team_color%", Variables.config.getString("teams." + team.getName() + ".color"))
                .replace("%team_name%", Variables.config.getString("teams." + team.getName() + ".name"));
        message = Utils.colorize(message);
        if (arena.getState() == Enums.ArenaState.PLAYING) {
            if (Countdown.deathTasks.containsKey(player) || !playerHandler.isAlive()) {
                for (PlayerHandler handler : arena.getPlayers())
                    if (Countdown.deathTasks.containsKey(handler.getPlayer()) || !handler.isAlive())
                        handler.getPlayer().sendMessage(message);

                return;
            }

            if (!e.getMessage().startsWith("!")) {
                if (team == null) return;

                for (PlayerHandler handler : team.getPlayers())
                    handler.getPlayer().sendMessage(message);
            } else if (e.getMessage().length() > 1) {
                for (PlayerHandler handler : arena.getPlayers())
                    handler.getPlayer().sendMessage(message);
            }
        } else {
                for (PlayerHandler handler : arena.getPlayers())
                    handler.getPlayer().sendMessage(message);
        }
    }
}
