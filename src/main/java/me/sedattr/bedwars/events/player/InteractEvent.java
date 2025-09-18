package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.Items;
import me.sedattr.bedwars.managers.PlayerManager;
import me.sedattr.bedwars.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class InteractEvent implements Listener {
    private final DeluxeBedwars plugin;

    public InteractEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler()
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        ItemStack item = e.getItem();
        NBTItem nbti = e.getItem() != null && e.getItem().getType() != Material.AIR ? new NBTItem(item) : null;

        if (nbti != null) {
            String items = nbti.getString("BedwarsINVENTORYITEM");
            if (items != null && !items.equals("")) {
                ConfigurationSection section = Variables.items.getConfigurationSection(items);

                if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    List<String> leftCommands = section.getStringList("left_click");
                    if (leftCommands != null && leftCommands.size() > 0) for (String command : leftCommands) {
                        player.performCommand(command
                                .replace("%player%", player.getName())
                                .replace("%uuid%", player.getUniqueId().toString()));
                    }
                }

                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (section.getString("type") != null && section.getString("type").equalsIgnoreCase("hide")) {
                        if (Variables.cooldowns.contains(player)) {
                            player.sendMessage(Utils.colorize(section.getString("cooldown_message")));
                            return;
                        }

                        Variables.cooldowns.add(player);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Variables.cooldowns.remove(player);
                            }
                        }.runTaskLater(DeluxeBedwars.getInstance(), section.getInt("cooldown")* 20L);

                        PlayerManager manager = new PlayerManager(player);
                        if (Variables.hiddenPlayers.contains(player)) {
                            Variables.hiddenPlayers.remove(player);
                            String message = section.getString("show_item.message");
                            if (message != null && !message.equals(""))
                                player.sendMessage(Utils.colorize(message));

                            section = section.getConfigurationSection("hide_item");
                            manager.hideShow("show");
                        }
                        else {
                            Variables.hiddenPlayers.add(player);
                            String message = section.getString("hide_item.message");
                            if (message != null && !message.equals(""))
                                player.sendMessage(Utils.colorize(message));

                            section = section.getConfigurationSection("show_item");
                            manager.hideShow("hide");
                        }

                        ItemStack newItem = new Items(player, null).createItem(section, null);
                        NBTItem newNbti = new NBTItem(newItem);
                        newNbti.setString("BedwarsINVENTORYITEM", items);
                        player.getInventory().setItem(section.getInt("slot")-1, newNbti.getItem());
                        return;
                    }

                    List<String> rightCommands = section.getStringList("right_click");
                    if (rightCommands != null && rightCommands.size() > 0) for (String command : rightCommands) {
                        player.performCommand(command
                                .replace("%player%", player.getName())
                                .replace("%uuid%", player.getUniqueId().toString()));
                    }
                }
                return;
            }
        }

        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;

        if (arena.getState() == Enums.ArenaState.ENDED && player.getVehicle() != null) {
            if (player.getVehicle().hasMetadata("BedwarsCOSMETIC")) {
                if (player.getVehicle().getType() == EntityType.WITHER) {
                    WitherSkull ws = player.launchProjectile(WitherSkull.class);
                    ws.setCharged(true);
                    ws.setMetadata("BedwarsCOSMETIC", new FixedMetadataValue(DeluxeBedwars.getInstance(), player));
                    ws.setVelocity(player.getLocation().getDirection().multiply(10));
                    ws.setShooter(player);
                    ws.setIsIncendiary(true);
                    ws.setYield(5.0F);
                }
                if (player.getVehicle().getType() == EntityType.ENDER_DRAGON) {
                    Fireball fireball = player.launchProjectile(Fireball.class);
                    fireball.setMetadata("BedwarsCOSMETIC", new FixedMetadataValue(DeluxeBedwars.getInstance(), player));
                    fireball.setVelocity(player.getLocation().getDirection().multiply(10));
                    fireball.setIsIncendiary(true);
                    fireball.setShooter(player);
                    fireball.setYield(5.0F);
                }
            }
        }

        if (e.getClickedBlock() != null
                && e.getClickedBlock().getType().name().contains("BED")
                    && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) {
            if (!player.isSneaking())
                e.setCancelled(true);
            return;
        }

        if (nbti == null) return;
        String customEntity = nbti.getString("BedwarsENTITYEGG");
        if (customEntity != null && !customEntity.equals("")) {
            e.setCancelled(true);
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            TeamHandler team = this.plugin.getGameManager().getTeam(player);
            if (team == null) return;

            ConfigurationSection section = Variables.items.getConfigurationSection("items." + customEntity + ".settings");
            LivingEntity entity = (LivingEntity) e.getClickedBlock().getWorld().spawnEntity(e.getClickedBlock().getLocation().add(0, 1, 0), EntityType.valueOf(section.getString("entity")));
            entity.setMetadata("BedwarsTEAM", new FixedMetadataValue(DeluxeBedwars.getInstance(), team));
            entity.setMetadata("BedwarsOWNER", new FixedMetadataValue(DeluxeBedwars.getInstance(), player));
            entity.setMaxHealth(section.getDouble("health"));
            entity.setHealth(section.getDouble("health"));
            entity.setCustomNameVisible(true);

            team.startEntityName(player, entity, section);
            int i = e.getItem().getAmount();
            if (i > 1) {
                e.getItem().setAmount(i - 1);
                return;
            }
            player.getInventory().setItemInHand(null);
            return;
        }

        String customEntityThrowable = nbti.getString("BedwarsENTITYTHROWABLE");
        if (customEntityThrowable != null && !customEntityThrowable.equals("")) {
            e.setCancelled(true);
            if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            TeamHandler team = this.plugin.getGameManager().getTeam(player);
            if (team == null) return;

            Projectile egg;
            if (e.getItem().getType() == Material.EGG) egg = player.launchProjectile(Egg.class);
            else egg = player.launchProjectile(Snowball.class);
            egg.setMetadata("BedwarsTEAM", new FixedMetadataValue(DeluxeBedwars.getInstance(), team));
            egg.setMetadata("BedwarsTHROWABLE", new FixedMetadataValue(DeluxeBedwars.getInstance(), customEntityThrowable));
            egg.setMetadata("BedwarsOWNER", new FixedMetadataValue(DeluxeBedwars.getInstance(), player));
            egg.setShooter(player);

            int i = e.getItem().getAmount();
            if (i > 1) {
                e.getItem().setAmount(i - 1);
                return;
            }
            player.getInventory().setItemInHand(null);
            return;
        }

        String bridgeEgg = nbti.getString("BedwarsBRIDGEEGG");
        if (bridgeEgg != null && !bridgeEgg.equals("")) {
            e.setCancelled(true);
            if (e.getAction() != Action.RIGHT_CLICK_AIR)
                return;

            for (TeamHandler team : arena.getTeams())
                if (team != null && team.isInside(team.getFirstLocation(), team.getSecondLocation(), player.getLocation()))
                    return;

            if (Utils.isNearby(arena, player.getLocation()))
                return;

            if (Variables.bridgeEggs.containsKey(player)) {
                Projectile egg = Variables.bridgeEggs.get(player);
                if (egg != null && !egg.isDead() && egg.isValid()) {
                    Utils.sendMessage(player, "cooldown");
                    return;
                }
            }

            Projectile egg;
            if (e.getItem().getType() == Material.EGG)
                egg = player.launchProjectile(Egg.class);
            else egg = player.launchProjectile(Snowball.class);
            if (egg == null || egg.isDead() || !egg.isValid())
                return;

            egg.setShooter(player);
            Variables.bridgeEggs.put(player, egg);

            int i = e.getItem().getAmount();
            if (i > 1) {
                e.getItem().setAmount(i - 1);
                return;
            }
            player.getInventory().setItemInHand(null);
            return;
        }

        if (e.getItem().getType() != Material.FIREBALL) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (Variables.cooldowns.contains(player)) {
            Utils.sendMessage(player, "cooldown");
            return;
        }

        Location eyeLocation = player.getEyeLocation().add(0, -0.5, 0);
        Fireball f = (Fireball) eyeLocation.getWorld().spawnEntity(eyeLocation.add(player.getLocation().getDirection().multiply(0.3)), EntityType.FIREBALL);
        f.setYield(2.5F);
        f.setIsIncendiary(true);
        f.setShooter(player);
        f.setMetadata("BedwarsOWNER", new FixedMetadataValue(DeluxeBedwars.getInstance(), player));
        Utils.setDirection(f, player.getLocation().getDirection().normalize().multiply(1));
        Variables.cooldowns.add(player);
        new BukkitRunnable() {
            public void run() {
                Variables.cooldowns.remove(player);
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 20L);


        e.setCancelled(true);
        int i = e.getItem().getAmount();
        if (i > 1) {
            e.getItem().setAmount(i - 1);
            return;
        }
        player.getInventory().setItemInHand(null);
    }
}
