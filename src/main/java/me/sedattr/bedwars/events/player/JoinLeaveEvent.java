package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.ArenaSetupHandler;
import me.sedattr.bedwars.handlers.GameManager;
import me.sedattr.bedwars.managers.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveEvent implements Listener {
    private final DeluxeBedwars plugin;

    public JoinLeaveEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        new PlayerManager(player).giveLobbyItems();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        GameManager gameHandler = this.plugin.getGameManager();
        ArenaSetupHandler setup = gameHandler.getSetup(player, "");
        if (setup != null) gameHandler.removeSetup(setup);

        ArenaHandler arena = gameHandler.getArena(player);
        if (arena == null) return;

        arena.leave(player);
    }
}
