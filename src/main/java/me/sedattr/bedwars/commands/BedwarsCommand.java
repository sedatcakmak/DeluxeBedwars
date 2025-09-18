package me.sedattr.bedwars.commands;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.cosmetics.Cosmetics;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.Countdown;
import me.sedattr.bedwars.managers.Menus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BedwarsCommand implements CommandExecutor {
    private final DeluxeBedwars plugin;

    public BedwarsCommand(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            Utils.sendMessage(commandSender, "not_player");
            return false;
        }

        Player player = (Player) commandSender;
        ArenaHandler playerArena = this.plugin.getGameManager().getArena(player);
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("test-cosmetic")) {
                if (args.length >= 2) {
                    new Cosmetics(player, player.getLocation(), player.getLocation(), true, args[1]).start();
                    return true;
                }
            }

            switch (args[0].toLowerCase()) {
                case "leave":
                    if (playerArena == null) {
                        Utils.sendMessage(player, "not_in_game");
                        return false;
                    }

                    if (Countdown.leaveTasks.containsKey(player))
                        Countdown.leaveTasks.get(player).end();
                    else
                        new Countdown.LeaveCountdown(player, playerArena).runTaskTimer(DeluxeBedwars.getInstance(), 0, 20);
                    return true;
                case "cosmeticlist":
                case "cosmetics":
                case "stats":
                case "statlist":
                    if (playerArena != null)
                        if (playerArena.getState() != Enums.ArenaState.WAITING && playerArena.getState() != Enums.ArenaState.STARTING) {
                            Utils.sendMessage(player, "cant_use_while_in_game");
                            return false;
                        }

                    new Menus(player).openNormalMenu(args[0].toLowerCase());
                    return true;
                case "arena":
                case "arenalist":
                case "arenas":
                    if (this.plugin.getGameManager().getArena(player) != null) {
                        Utils.sendMessage(player, "already_playing");
                        return false;
                    }

                    new Menus(player).openArenasMenu(args.length >= 2 ? args[1] : "");
                    return true;
                case "join":
                    if (args.length < 2) {
                        Utils.sendMessage(player, "join_usage");
                        return false;
                    }

                    ArenaHandler arena = this.plugin.getGameManager().getArenaByName(args[1]);
                    if (arena == null) {
                        Utils.sendMessage(player, "wrong_arena");
                        return false;
                    }

                    arena.join(player);
                    return true;
            }
        }

        List<String> messages = Variables.messages.getStringList("player_usage");
        if (messages != null && !messages.isEmpty())
            for (String line : messages)
                commandSender.sendMessage(Utils.colorize(line));
        return false;
    }
}