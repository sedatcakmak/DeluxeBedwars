package me.sedattr.bedwars.commands;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.PartyHandler;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BedwarsPartyCommand implements CommandExecutor {
    private final DeluxeBedwars plugin;

    public BedwarsPartyCommand(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            Utils.sendMessage(commandSender, "not_player");
            return false;
        }

        Player player = (Player) commandSender;
        PartyHandler party = this.plugin.getGameManager().getParty(player);
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "invite":
                    if (args.length < 2) {
                        Utils.sendMessage(player, "write_player");
                        return false;
                    }

                    PartyHandler partyOwner = this.plugin.getGameManager().getPartyOwner(player);
                    Player invited = Bukkit.getPlayer(args[1]);
                    if (invited == null) {
                        Utils.sendMessage(player, "wrong_player");
                        return false;
                    }

                    if (invited == player) {
                        Utils.sendMessage(player, "cant_invite_yourself");
                        return false;
                    }

                    if (partyOwner != null && partyOwner.getInvitedMembers().contains(invited.getUniqueId())) {
                        Utils.sendMessage(player, "already_invited");
                        return false;
                    }

                    if (partyOwner == null)
                        partyOwner = new PartyHandler(player);

                    partyOwner.addInvitedMember(invited.getUniqueId());
                    List<String> invitedMessage = Variables.messages.getStringList("invited");
                    if (invitedMessage != null && !invitedMessage.isEmpty())
                        for (String line : invitedMessage)
                            invited.sendMessage(Utils.colorize(line
                                    .replace("%display_name%", partyOwner.getOwner().getName())
                                    .replace("%name%", partyOwner.getOwner().getName())));

                    player.sendMessage(Utils.colorize(Utils.sendMessage(null, "invited_player")
                            .replace("%name%", invited.getName())
                            .replace("%display_name%", invited.getDisplayName())));
                    return true;
                case "accept":
                    if (args.length < 2) {
                        Utils.sendMessage(player, "write_player");
                        return false;
                    }

                    if (party != null) {
                        Utils.sendMessage(player, "have_party");
                        return false;
                    }

                    Player playerOwner = Bukkit.getPlayer(args[1]);
                    if (playerOwner == null) {
                        Utils.sendMessage(player, "wrong_player");
                        return false;
                    }

                    PartyHandler invitedParty = this.plugin.getGameManager().getPartyOwner(playerOwner);
                    if (invitedParty == null) {
                        Utils.sendMessage(player, "havent_party");
                        return false;
                    }

                    if (!invitedParty.getInvitedMembers().contains(player.getUniqueId())) {
                        Utils.sendMessage(player, "not_invited");
                        return false;
                    }

                    invitedParty.removeInvitedMember(player.getUniqueId());
                    invitedParty.addMember(player.getUniqueId());

                    for (Player p : invitedParty.getPlayers())
                        p.sendMessage(Utils.sendMessage(null, "player_accepted_invite")
                                .replace("%name%", player.getName()
                                        .replace("%display_name%", player.getDisplayName())));

                    player.sendMessage(Utils.sendMessage(null, "accepted_invite")
                            .replace("%name%", invitedParty.getOwner().getName()
                                    .replace("%display_name%", invitedParty.getOwner().getDisplayName())));
                    return true;
                case "info":
                    if (party == null) {
                        Utils.sendMessage(player, "not_in_party");
                        return false;
                    }

                    List<String> messages = Variables.messages.getStringList("party_info");
                    if (messages != null && !messages.isEmpty())
                        for (String line : messages)
                            player.sendMessage(Utils.colorize(line
                                    .replace("%players%", String.valueOf(party.getPlayers().size()))
                                    .replace("%display_name%", party.getOwner().getName())
                                    .replace("%name%", party.getOwner().getName())));
                    return true;
                case "leave":
                    if (party == null) {
                        Utils.sendMessage(player, "not_in_party");
                        return false;
                    }

                    party.removeMember(player.getUniqueId());
                    player.sendMessage(Utils.colorize(Variables.messages.getString("left_from_party")
                            .replace("%name%", party.getOwner().getName())
                            .replace("%displayname%", party.getOwner().getDisplayName())));

                    for (Player partyPlayer : party.getPlayers())
                        partyPlayer.sendMessage(Utils.colorize(Variables.messages.getString("player_left_from_party")
                                .replace("%name%", player.getName())
                                .replace("%displayname%", player.getDisplayName())));

                    if (player == party.getOwner()) {
                        for (Player p : party.getPlayers())
                            Utils.sendMessage(p, "disbanded_party");
                        this.plugin.getGameManager().getParties().remove(party);
                    } else if (party.getPlayers().size() <= 1) {
                        this.plugin.getGameManager().getParties().remove(party);
                        Utils.sendMessage(party.getOwner(), "disbanded_party_alone");
                    }

                    return true;
                case "disband":
                    if (party == null) {
                        Utils.sendMessage(player, "not_in_party");
                        return false;
                    }

                    if (party.getOwner() != player) {
                        Utils.sendMessage(player, "not_party_owner");
                        return false;
                    }

                    for (Player p : party.getPlayers())
                        Utils.sendMessage(p, "disbanded_party");

                    this.plugin.getGameManager().getParties().remove(party);
                    return true;
            }
        }

        List<String> messages = Variables.messages.getStringList("party_usage");
        if (messages != null && !messages.isEmpty())
            for (String line : messages)
                commandSender.sendMessage(Utils.colorize(line));
        return false;
    }
}