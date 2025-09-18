package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.cosmetics.Cosmetics;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.PlayerHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.managers.Countdown;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DamageByEntityEvent implements Listener {
    private final DeluxeBedwars plugin;

    public DamageByEntityEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamageKiller(EntityDamageByEntityEvent e) {
        if (e.getEntity().hasMetadata("BedwarsCOSMETIC"))
            e.setCancelled(true);

        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;

        if (Countdown.deathTasks.containsKey(player))
            e.setCancelled(true);

        Entity killer = e.getDamager();
        if (killer == null) return;

        if (killer.getType() == EntityType.PRIMED_TNT || killer.getType() == EntityType.FIREBALL || killer.getType() == EntityType.ENDER_PEARL) {
            e.setDamage(4.0D);

            if (killer.getType() != EntityType.ENDER_PEARL) {
                Vector vector = player.getVelocity().clone();
                double distance = player.getLocation().distance(killer.getLocation());

                double more;
                double math;
                if (distance <= 2) {
                    math = 1.30;
                    more = 2;
                }
                else if (distance <= 4) {
                    math = 1.50;
                    more = 3;
                }
                else {
                    math = 1.70;
                    more = 4;
                }

                player.setVelocity(new Vector());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setVelocity(new Vector(vector.getX() * more, math, vector.getZ() * more));
                    }
                }.runTaskLater(this.plugin, 1);
            }
        }

        PlayerHandler playerHandler = arena.getPlayer(player);
        if (playerHandler == null)
            return;

        if (killer instanceof Player) {
            PlayerHandler killerHandler = arena.getPlayer((Player) killer);
            if (killerHandler != null) {
                if (!killerHandler.isAlive() || !playerHandler.isAlive())
                    e.setCancelled(true);

                if (killerHandler.getTeam() == playerHandler.getTeam() || playerHandler.getTeam().getName().equalsIgnoreCase(killerHandler.getTeam().getName()))
                    e.setCancelled(true);
            }
        }

        playerHandler.setLastDamager(killer);
        if (arena.getState() != Enums.ArenaState.WAITING && arena.getState() != Enums.ArenaState.STARTING && arena.getState() != Enums.ArenaState.ENDED) {
            if (playerHandler.isInvis())
                playerHandler.setInvis(false, 0);

            if (e.getFinalDamage() >= player.getHealth()) {
                new Cosmetics(player, player.getLocation(), player.getLocation(), true, "death_cries").start();

                TeamHandler team = this.plugin.getGameManager().getTeam(player);
                if (team != null && killer instanceof Player) {
                    String addon = team.getBedStatus() ? "" : "final_";

                    new Cosmetics((Player) killer, killer.getLocation(), player.getLocation(), false, addon+"kill_effects").start();
                }
            }
        }
    }
}