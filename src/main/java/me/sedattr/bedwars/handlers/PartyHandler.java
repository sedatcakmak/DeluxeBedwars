package me.sedattr.bedwars.handlers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.helpers.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyHandler {
    private final UUID owner;
    private final List<UUID> members = new ArrayList<>();
    private final List<UUID> invitedMembers = new ArrayList<>();

    public PartyHandler(Player owner) {
        this.owner = owner.getUniqueId();
        this.members.add(owner.getUniqueId());

        DeluxeBedwars.getInstance().getGameManager().addParty(this);
    }

    public Player getOwner() {
        return Bukkit.getPlayer(this.owner);
    }

    public List<UUID> getInvitedMembers() {
        return this.invitedMembers;
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    public void removeInvitedMember(UUID uuid) {
        this.invitedMembers.remove(uuid);
    }

    public void addInvitedMember(UUID uuid) {
        this.invitedMembers.add(uuid);

        new BukkitRunnable() {
            @Override
            public void run() {
                PartyHandler.this.invitedMembers.remove(uuid);
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 1200);
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        this.members.forEach(u -> players.add(Bukkit.getPlayer(u)));

        return players;
    }

    public List<UUID> getMembers() {
        return this.members;
    }

    public Boolean canJoin(ArenaHandler arena) {
        int players = arena.getPlayers().size();

        if (players + this.members.size() > arena.getMaxPlayers())
            return false;

        int teams = 0;
        for (TeamHandler team : arena.getTeams()) {
            if (team.getMaxPlayers() >= this.members.size())
                teams++;
        }

        return teams > 0;
    }

    public void join(ArenaHandler arena) {
        if (!canJoin(arena)) {
            Utils.sendMessage(getOwner(), "cant_join_to_arena");
            return;
        }

        for (Player player : getPlayers()) {
            if (player == getOwner()) continue;

            ArenaHandler playerArena = DeluxeBedwars.getInstance().getGameManager().getArena(player);
            if (playerArena != null) {
                if (arena == playerArena)
                    continue;
                if (arena.getName().equalsIgnoreCase(playerArena.getName()))
                    continue;

                playerArena.leave(player);
            }

            arena.join(player);
        }
    }

    public void leave() {
        for (Player player : getPlayers()) {
            if (player == getOwner()) continue;

            ArenaHandler playerArena = DeluxeBedwars.getInstance().getGameManager().getArena(player);
            if (playerArena != null)
                playerArena.leave(player);
        }
    }
}
