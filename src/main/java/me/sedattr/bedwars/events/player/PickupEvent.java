package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.managers.Countdown;
import me.sedattr.bedwars.managers.PlayerManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PickupEvent implements Listener {
    private final DeluxeBedwars plugin;

    public PickupEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        ArenaHandler arena = this.plugin.getGameManager().getArena(e.getPlayer());
        if (arena == null) return;

        if (arena.getState() == Enums.ArenaState.WAITING || arena.getState() == Enums.ArenaState.STARTING) {
            e.setCancelled(true);
            return;
        }

        if (Countdown.deathTasks.containsKey(e.getPlayer()) || !arena.getPlayer(e.getPlayer()).isAlive()) {
            e.setCancelled(true);
            return;
        }

        if (e.getItem() == null || e.getItem().getItemStack() == null || e.getItem().getItemStack().getType().equals(Material.AIR)) return;
        String type = e.getItem().getItemStack().getType().name();
        if (type.contains("BED")) {
            e.setCancelled(true);
            e.getItem().remove();
            return;
        }

        if (type.contains("SWORD")) {
            new PlayerManager(e.getPlayer()).checkSword();
            return;
        }


        if (type.contains("WOOD") || type.contains("LOG") || type.contains("LOG_2"))
            new PlayerManager(e.getPlayer()).checkWood();
    }
}
