package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.cosmetics.Cosmetics;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.Countdown;
import me.sedattr.bedwars.managers.PlayerManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageEvent implements Listener {
    private final DeluxeBedwars plugin;

    public DamageEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity().hasMetadata("BedwarsCOSMETIC"))
            e.setCancelled(true);
        if (!(e.getEntity() instanceof Player)) return;

        Player p = (Player) e.getEntity();
        ArenaHandler arena = this.plugin.getGameManager().getArena(p);
        if (arena == null) return;

        if (Variables.teleportedPlayers.contains(p)) {
            e.setDamage(e.getDamage()/25);
            Variables.teleportedPlayers.remove(p);
            return;
        }

        Enums.ArenaState state = arena.getState();
        if (state == Enums.ArenaState.ENDED || state == Enums.ArenaState.WAITING || state == Enums.ArenaState.STARTING) {
            e.setCancelled(true);
            return;
        } else if (Countdown.deathTasks.containsKey(p)) {
            e.setCancelled(true);
            return;
        }
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
            e.setDamage(e.getDamage()/2);

        Entity player = ((Player) e.getEntity()).getKiller();
        if (player != null && p != player) {
            TeamHandler victimTeam = this.plugin.getGameManager().getPlayerTeams().get(p);
            TeamHandler killerTeam = this.plugin.getGameManager().getPlayerTeams().get(player);
            if (victimTeam != null && killerTeam != null) {
                if (victimTeam.getName().equalsIgnoreCase(killerTeam.getName())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (e.getFinalDamage() >= p.getHealth()) {
            new Cosmetics(p, p.getLocation(), p.getLocation(), true, "death_cries").start();

            e.setDamage(0.0D);
            new PlayerManager(p).deadEvent(arena, e.getCause() == EntityDamageEvent.DamageCause.FALL ? "fall" : "normal");
        }
    }
}
