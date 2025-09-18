package me.sedattr.bedwars.handlers;

import me.sedattr.bedwars.DeluxeBedwars;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private final List<ArenaHandler> arenas = new ArrayList<>();
    private final Map<Player, ArenaHandler> players = new HashMap<>();
    private final Map<Player, TeamHandler> playerTeams = new HashMap<>();
    private final List<ArenaSetupHandler> setups = new ArrayList<>();
    private final List<PartyHandler> parties = new ArrayList<>();

    public List<PartyHandler> getParties() {
        return this.parties;
    }

    public Map<Player, ArenaHandler> getPlayers() {
        return this.players;
    }

    public Map<Player, TeamHandler> getPlayerTeams() {
        return this.playerTeams;
    }

    public void addArena(ArenaHandler arena) {
        ArenaHandler oldArena = getArenaByName(arena.getName());

        if (oldArena != null)
            arenas.set(arenas.indexOf(oldArena), arena);
        else
            arenas.add(arena);
    }

    public void addParty(PartyHandler party) {
        parties.add(party);
    }

    public PartyHandler getParty(Player player) {
        for (PartyHandler party : parties) {
            if (party.getPlayers().contains(player) || party.getMembers().contains(player.getUniqueId()))
                return party;
        }

        return null;
    }

    public PartyHandler getPartyOwner(Player player) {
        for (PartyHandler party : parties) {
            if (party.getOwner() == player || party.getOwner().getUniqueId() == player.getUniqueId())
                return party;
        }

        return null;
    }

    public void addSetup(ArenaSetupHandler setup) {
        setups.add(setup);
    }

    public void removeSetup(ArenaSetupHandler setup) {
        if (!setups.contains(setup)) {
            setups.removeIf(s -> s.getPlayer() == setup.getPlayer());
            return;
        }

        setups.remove(setup);
    }

    public ArenaSetupHandler getSetup(Player player, String name) {
        for (ArenaSetupHandler s : setups) {
            if (s.getPlayer() == player || s.getPlayer().getUniqueId() == player.getUniqueId())
                return s;

            if (name != null && !name.equals("")) {
                if (s.getName().equalsIgnoreCase(name))
                    return s;
            }
        }

        ArenaHandler arena = DeluxeBedwars.getInstance().getGameManager().getArenaByName(name);
        if (arena != null)
            return new ArenaSetupHandler(player, arena);

        return null;
    }

    public void addPlayer(Player player, ArenaHandler arena, TeamHandler team) {
        if (arena != null) players.put(player, arena);
        if (team != null) playerTeams.put(player, team);
    }

    public List<ArenaHandler> getArenas() {
        return this.arenas;
    }

    public ArenaHandler getArena(Player player) {
        return players.getOrDefault(player, null);
    }

    public TeamHandler getTeam(Player player) {
        return playerTeams.getOrDefault(player, null);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        playerTeams.remove(player);
    }

    public PlayerHandler getPlayer(Player player) {
        ArenaHandler arena = getArena(player);
        if (arena == null) return null;

        for (PlayerHandler playerHandler : arena.getPlayers()) {
            if (player == playerHandler.getPlayer()) return playerHandler;
        }

        return null;
    }

    public ArenaHandler getArenaByName(String name) {
        for (ArenaHandler arena : arenas) {
            if (arena.getName().equalsIgnoreCase(name)) return arena;
        }

        return null;
    }
}
