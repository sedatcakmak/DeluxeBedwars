package me.sedattr.bedwars.events.entity;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.TeamHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class TargetEvent implements Listener {
    private final DeluxeBedwars plugin;

    public TargetEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void stopTarget(EntityTargetEvent e) {
        if (e.getEntity().hasMetadata("BedwarsCOSMETIC") || e.getEntity().hasMetadata("BedwarsWINEFFECT"))
            e.setCancelled(true);

        if (e.getEntity().hasMetadata("BedwarsTEAM")){
            if (!(e.getTarget() instanceof Player)) {
                e.setCancelled(true);
                return;
            }

            Player target = (Player) e.getTarget();
            TeamHandler team = (TeamHandler) e.getEntity().getMetadata("BedwarsTEAM").get(0).value();
            if (team == this.plugin.getGameManager().getTeam(target)) e.setCancelled(true);
            return;
        }

        if (e.getEntity().hasMetadata("BedwarsDRAGON")) {
            if (!(e.getTarget() instanceof Player)) return;

            TeamHandler dragonTeam = (TeamHandler) e.getEntity().getMetadata("BedwarsDRAGON").get(0).value();
            if (dragonTeam == null) return;

            TeamHandler playerTeam = this.plugin.getGameManager().getTeam((Player) e.getTarget());
            if (playerTeam == null) return;

            if (playerTeam == dragonTeam || playerTeam.getName().equalsIgnoreCase(dragonTeam.getName()))
                e.setCancelled(true);
        }
    }
}
