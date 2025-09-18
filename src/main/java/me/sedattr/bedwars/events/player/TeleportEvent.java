package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.managers.Countdown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportEvent implements Listener {
    private final DeluxeBedwars plugin;

    public TeleportEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e){
        Player player = e.getPlayer();
        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;

        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            e.setCancelled(true);
            player.setNoDamageTicks(5);
            if (!Countdown.deathTasks.containsKey(player)) player.teleport(e.getTo());
        }
    }
}
