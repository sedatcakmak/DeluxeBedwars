package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.PlayerHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.Countdown;
import me.sedattr.bedwars.managers.PlayerManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class MoveEvent implements Listener {
    private final DeluxeBedwars plugin;

    public MoveEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player == null) return;
        if (player.isDead()) return;

        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;

        Enums.ArenaState state = arena.getState();
        if (state == Enums.ArenaState.ENDED || state == Enums.ArenaState.STARTING || state == Enums.ArenaState.WAITING) return;
        if (Countdown.deathTasks.containsKey(player)) return;

        if (player.getLocation().getY() < -50.0D) {
            new PlayerManager(player).deadEvent(arena, "knock");
            return;
        }

        PlayerHandler handler = this.plugin.getGameManager().getPlayer(player);
        if (handler == null) return;

        if (!handler.isAlive()) return;
        if (handler.isTrapped()) return;
        for (TeamHandler team : arena.getTeams()) {
            if (!team.getBedStatus()) continue;
            if (team == this.plugin.getGameManager().getTeam(player)) {
                if (player.hasPotionEffect(PotionEffectType.REGENERATION)) continue;

                if (team.getUpgrades().getHealPool()
                        && player.getLocation().distance(team.getBedLocation()) <= 10.0D) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                }
                continue;
            }
            if (player.getLocation().distance(team.getBedLocation()) > 5.0D) continue;

            List<String> traps = team.getUpgrades().getTraps();
            if (traps.size() <= 0) continue;

            ConfigurationSection section = Variables.items.getConfigurationSection("traps." + traps.get(0));
            if (section == null || section.getString("type") == null) return;

            team.getUpgrades().removeTrap(0);
            player.sendMessage("TRAP!");
            handler.setTrapped(true);

            team.send("alarm by " + player.getName());
            if (section.getString("type").equalsIgnoreCase("effect")) {
                List<String> enemyEffects = section.getStringList("effects.enemy");
                if (enemyEffects != null && enemyEffects.size() > 0) for (String effect : enemyEffects) {
                    String[] args = effect.split("[:]", 3);
                    if (args.length < 3) continue;

                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(args[0]), Integer.parseInt(args[2]) * 20, Integer.parseInt(args[1])-1));
                }

                List<String> teamEffects = section.getStringList("effects.team");
                if (teamEffects != null && teamEffects.size() > 0) for (String effect : teamEffects) {
                    String[] args = effect.split("[:]", 3);
                    if (args.length < 3) continue;

                    for (PlayerHandler playerHandler : team.getPlayers()) {
                        if (!playerHandler.isAlive()) continue;

                        playerHandler.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.getByName(args[0]), Integer.parseInt(args[2]) * 20, Integer.parseInt(args[1])-1));
                    }
                }
            }

            new BukkitRunnable() {
                public void run() {
                    handler.setTrapped(false);
                    cancel();
                }
            }.runTaskLater(DeluxeBedwars.getInstance(), 15 * 20L);
        }
    }
}
