package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelEvent implements Listener {
    private final DeluxeBedwars plugin;

    public FoodLevelEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        ArenaHandler arena = this.plugin.getGameManager().getArena((Player) e.getEntity());
        if (arena == null) return;

        e.setCancelled(true);
    }
}
