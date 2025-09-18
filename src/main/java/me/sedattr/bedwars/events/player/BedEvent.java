package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class BedEvent implements Listener {
    private final DeluxeBedwars plugin;

    public BedEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e) {
        if (e == null) return;

        ArenaHandler arena = this.plugin.getGameManager().getArena(e.getPlayer());
        if (arena != null)
            e.setCancelled(true);
    }
}
