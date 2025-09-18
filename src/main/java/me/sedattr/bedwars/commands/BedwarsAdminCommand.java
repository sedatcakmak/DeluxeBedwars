package me.sedattr.bedwars.commands;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.cosmetics.Cosmetics;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.FileHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class BedwarsAdminCommand implements CommandExecutor {
    private final DeluxeBedwars plugin;

    public BedwarsAdminCommand(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("test")) {
                if (args.length >= 2 && commandSender instanceof Player) {
                    Player player = (Player) commandSender;

                    new Cosmetics(player, player.getLocation(), player.getLocation(), true, args[1]).start();
                }

                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                Utils.sendMessage(commandSender, "reloaded");
                new FileHandler("data.yml");
                new FileHandler("cosmetics.yml");
                new FileHandler("items.yml");
                new FileHandler("menus.yml");
                DeluxeBedwars.getInstance().reloadConfig();
                return true;
            }

            if (!(commandSender instanceof Player)) {
                Utils.sendMessage(commandSender, "not_player");
                return false;
            }

            Player player = (Player) commandSender;
            ArenaHandler arena = this.plugin.getGameManager().getArena(player);
            if (arena == null) {
                Utils.sendMessage(player, "not_in_game");
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "start":
                    if (arena.getState() == Enums.ArenaState.STARTING
                            || arena.getState() == Enums.ArenaState.WAITING) {
                        Utils.sendMessage(player, "already_started");
                        return false;
                    }
                    if (arena.getState() == Enums.ArenaState.ENDED) {
                        Utils.sendMessage(player, "already_ended");
                        return false;
                    }

                    arena.startGame();
                    Utils.sendMessage(player, "started");
                    return true;
                case "end":
                    if (arena.getState() != Enums.ArenaState.PLAYING) {
                        Utils.sendMessage(player, "not_started");
                        return false;
                    }
                    if (arena.getState() == Enums.ArenaState.ENDED) {
                        Utils.sendMessage(player, "already_ended");
                        return false;
                    }

                    arena.endGame();
                    Utils.sendMessage(player, "ended");
                    return true;
                case "check":
                    if (arena.getState() == Enums.ArenaState.STARTING || arena.getState() == Enums.ArenaState.WAITING) {
                        Utils.sendMessage(player, "not_started");
                        return false;
                    }
                    if (arena.getState() == Enums.ArenaState.ENDED) {
                        Utils.sendMessage(player, "already_ended");
                        return false;
                    }

                    arena.checkWinner();
                    Utils.sendMessage(player, "checked");
                    return true;
                case "setlobby":
                case "set_lobby":
                    YamlConfiguration configuration = new YamlConfiguration();
                    ConfigurationSection section = configuration.getConfigurationSection("lobby.location");
                    Location location = player.getLocation();

                    section.set("world", player.getWorld().getName());
                    section.set("x", location.getX());
                    section.set("y", location.getY());
                    section.set("z", location.getZ());
                    section.set("pitch", location.getPitch());
                    section.set("yaw", location.getYaw());

                    File file = new File(DeluxeBedwars.getInstance().getDataFolder(), "config.yml");
                    try {
                        configuration.save(file);
                    } catch (IOException ignored) {
                    }
            }
        }

        List<String> messages = Variables.messages.getStringList("admin_usage");
        if (messages != null && !messages.isEmpty())
            for (String line : messages)
                commandSender.sendMessage(Utils.colorize(line));

        return false;
    }
}
