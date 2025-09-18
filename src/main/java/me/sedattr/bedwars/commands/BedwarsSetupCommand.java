package me.sedattr.bedwars.commands;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaSetupHandler;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.Menus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BedwarsSetupCommand implements CommandExecutor {
    private final DeluxeBedwars plugin;

    public BedwarsSetupCommand(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            Utils.sendMessage(commandSender, "not_player");
            return false;
        }

        Player player = (Player) commandSender;
        ArenaSetupHandler setup = this.plugin.getGameManager().getSetup(player, "");
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (setup != null) {
                    Utils.sendMessage(player, "setup.in_setup");
                    return false;
                }

                if (args.length < 2) {
                    Utils.sendMessage(player, "write_arena_name");
                    return false;
                }

                if (this.plugin.getGameManager().getSetup(player, args[1]) != null || this.plugin.getGameManager().getArenaByName(args[1]) != null) {
                    Utils.sendMessage(player, "setup.cant_be_same_name");
                    return false;
                }

                new ArenaSetupHandler(player.getWorld(), player, args[1]);
                player.sendMessage(Utils.colorize(Utils.sendMessage(null, "setup.created").replace("%name%", args[1])));
                return true;
            } else if (args[0].equalsIgnoreCase("edit")) {
                ArenaSetupHandler newSetup = this.plugin.getGameManager().getSetup(player, args.length < 2 ? "" : args[1]);
                if (newSetup == null) {
                    Utils.sendMessage(player, "setup.cant_find_setup");
                    return false;
                }

                new Menus(player).openNormalMenu("arena_setup");
                return true;
            }

            if (setup == null) {
                Utils.sendMessage(player, "setup.cant_find_setup");
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "menu":
                    new Menus(player).openNormalMenu("arena_setup");
                    return true;
                case "save":
                    List<String> result;
                    try {
                        result = setup.save();
                    } catch (NullPointerException e) {
                        Utils.sendMessage(player, "setup.save_error");
                        return false;
                    }

                    if (result != null) {
                        String newResult = String.join(", ", result);

                        player.sendMessage(Utils.colorize(Utils.sendMessage(null, "setup.save_missing")
                                .replace("%missing%", newResult)));
                        return false;
                    }

                    player.sendMessage(Utils.colorize(Utils.sendMessage(null, "setup.saved").replace("%name%", setup.getName())));
                    return true;
                case "exit":
                    if (!setup.getFinished()) {
                        Utils.sendMessage(player, "setup.finish_setup");
                        return false;
                    }

                    if (setup.getTeams().size() <= 0) {
                        Utils.sendMessage(player, "setup.add_team_first");
                        return false;
                    }

                    this.plugin.getGameManager().removeSetup(setup);
                    Utils.sendMessage(player, "setup.exit");
                    return true;
                case "cancel":
                    this.plugin.getGameManager().removeSetup(setup);
                    Utils.sendMessage(player, "setup.cancelled");
                    return true;
            }
        }

        if (Variables.messages.getConfigurationSection("types") == null) return false;
        if (Variables.messages.getConfigurationSection("modes") == null) return false;

        List<String> types = new ArrayList<>(Variables.messages.getConfigurationSection("types").getKeys(false));
        String newTypes = String.join(", ", types);

        List<String> modes = new ArrayList<>(Variables.messages.getConfigurationSection("modes").getKeys(false));
        String newModes = String.join(", ", modes);

        for (String line : Variables.messages.getStringList("setup.setup_usage")) {
            player.sendMessage(Utils.colorize(line
                    .replace("%modes%", newModes)
                    .replace("%types%", newTypes)));
        }
        return false;
    }
}