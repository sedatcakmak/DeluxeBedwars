package me.sedattr.bedwars.commands;

import java.util.List;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaSetupHandler;
import me.sedattr.bedwars.handlers.TeamSetupHandler;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.Menus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class BedwarsTeamCommand implements CommandExecutor {
    private final DeluxeBedwars plugin;

    public BedwarsTeamCommand(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            Utils.sendMessage(commandSender, "not_player");
            return false;
        }

        Player player = (Player) commandSender;
        ArenaSetupHandler setup = this.plugin.getGameManager().getSetup(player, "");
        TeamSetupHandler team = setup != null ? setup.getTeam() : null;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (setup == null) {
                    Utils.sendMessage(player, "setup.not_in_setup");
                    return false;
                }

                if (!setup.getFinished()) {
                    Utils.sendMessage(player, "setup.finish_setup");
                    return false;
                }

                if (team != null) {
                    Utils.sendMessage(player, "setup.finish_team_first");
                    return false;
                }

                if (args.length < 2) {
                    Utils.sendMessage(player, "write_team_name");
                    return false;
                }

                ConfigurationSection section = Variables.config.getConfigurationSection("teams." + args[1]);
                if (section == null || section.getString("name") == null) {
                    Utils.sendMessage(player, "setup.wrong_team_name");
                    return false;
                }

                new TeamSetupHandler(setup, player, args[1]);
                player.sendMessage(Utils.colorize(Utils.sendMessage(null, "setup.created_team").replace("%name%", args[1])));
                return true;
            }

            if (team == null) {
                Utils.sendMessage(player, "setup.create_team_first");
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "menu":
                    new Menus(player).openNormalMenu("team_setup");
                    return true;
                case "save":
                    List<String> result;
                    try {
                        result = team.save();
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
                case "cancel":
                    setup.setTeam(null);
                    Utils.sendMessage(player, "setup.cancelled");
                    return true;
            }
        }

        List<String> usage = Variables.messages.getStringList("setup.team_usage");
        if (usage != null && !usage.isEmpty())
            for (String line : usage)
                player.sendMessage(Utils.colorize(line));
        return false;
    }
}