package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class InteractAtEntityEvent implements Listener {
    private final DeluxeBedwars plugin;

    public InteractAtEntityEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void interactStand(PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;

        if (e.getRightClicked() instanceof ArmorStand && !((ArmorStand) e.getRightClicked()).isVisible())
            e.setCancelled(true);
    }
}
