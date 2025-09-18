package me.sedattr.bedwars.events.entity;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.DataHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class LaunchEvent implements Listener {
    private final DeluxeBedwars plugin;

    public LaunchEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void launchSkull(ProjectileLaunchEvent e) {
        Projectile entity = e.getEntity();

        if (entity == null)
            return;
        if (entity.getShooter() == null)
            return;
        if (entity.hasMetadata("BedwarsCOSMETIC"))
            e.setCancelled(true);

        if (!(e.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) e.getEntity().getShooter();
        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;

        if (e.getEntity() instanceof EnderPearl) {
            Variables.teleportedPlayers.add(player);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Variables.teleportedPlayers.remove(player);
                }
            }.runTaskLater(DeluxeBedwars.getInstance(), 200);
        }

        e.getEntity().setMetadata("BedwarsOWNER", new FixedMetadataValue(DeluxeBedwars.getInstance(), player));
        new BukkitRunnable() {
            public void run() {
                if (Variables.bridgeEggs.containsKey(player) && Variables.bridgeEggs.get(player) == entity) {
                    final int[] blocks = {0};
                    new BukkitRunnable() {
                        public void run() {

                            if (e.getEntity().isDead() || blocks[0] >= 100) {
                                if (!e.getEntity().isDead()) e.getEntity().remove();
                                Variables.bridgeEggs.remove(player);
                                cancel();
                                return;
                            }

                            Location location = e.getEntity().getLocation();
                            for (int i = location.getBlockX() - 1; i < location.getBlockX() + 1; i++) {
                                for (int j = location.getBlockY(); j < location.getBlockY() + 1; j++) {
                                    for (int k = location.getBlockZ() - 1; k < location.getBlockZ() + 1; k++) {
                                        Block block = e.getEntity().getWorld().getBlockAt(i, j - 2, k);
                                        if (block.getType() != Material.AIR)
                                            continue;
                                        Location blockLocation = block.getLocation();

                                        if (Utils.isNearby(arena, blockLocation)) {
                                            if (!e.getEntity().isDead()) e.getEntity().remove();
                                            cancel();
                                            return;
                                        }

                                        for (TeamHandler team : arena.getTeams()) {
                                            if (team != null && team.isInside(team.getFirstLocation(), team.getSecondLocation(), blockLocation)) {
                                                if (!e.getEntity().isDead()) e.getEntity().remove();
                                                cancel();
                                                return;
                                            }
                                        }

                                        block.setType(Material.WOOL);

                                        BlockState blockState = block.getState();
                                        blockState.setData(new Wool(Variables.dyeColors.get(LaunchEvent.this.plugin.getGameManager().getTeam(player).getName().toLowerCase())));
                                        blockState.update();

                                        arena.addPlacedBlock(block);
                                        block.setMetadata("BedwarsMATCH", new FixedMetadataValue(DeluxeBedwars.getInstance(), arena));

                                        blocks[0]++;
                                    }
                                }
                            }
                        }
                    }.runTaskTimer(DeluxeBedwars.getInstance(), 0L, 1L);
                }
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 2L);

        String trail = new DataHandler(player).getPlayerSelectedCosmetic("projectile_trails");
        if (trail == null || trail.equals("null")) return;

        ConfigurationSection trailConfiguration = Variables.cosmetics.getConfigurationSection("projectile_trails." + trail);
        if (trailConfiguration == null || trailConfiguration.getString("type") == null) return;

        new BukkitRunnable() {
            public void run() {
                if (e.getEntity().isDead() || e.getEntity().isOnGround() || !e.getEntity().isValid())
                    cancel();

                switch (trailConfiguration.getString("type").toLowerCase()) {
                    case "item":
                        Item item = e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), new ItemStack(Material.getMaterial(trailConfiguration.getString("material"))));
                        item.setPickupDelay(Integer.MAX_VALUE);

                        new BukkitRunnable() {
                            public void run() {
                                item.remove();
                            }
                        }.runTaskLater(DeluxeBedwars.getInstance(), 20L);
                        break;
                    case "effect":
                        e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.valueOf(trailConfiguration.getString("effect")), Integer.MAX_VALUE);
                        break;
                    default:
                        cancel();
                }
            }
        }.runTaskTimer(DeluxeBedwars.getInstance(), 2L, 2L);
    }
}
