package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.managers.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeEvent implements Listener {
    private final DeluxeBedwars plugin;

    public WorldChangeEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChange(PlayerChangedWorldEvent e) {
        new PlayerManager(e.getPlayer()).giveLobbyItems();
    }
}
