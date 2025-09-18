package me.sedattr.bedwars.handlers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.nms.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PlayerHandler {
    private final Player player;
    private final ArenaHandler arena;
    private TeamHandler team;
    private Boolean alive = true;
    private Boolean shear = false;
    private Boolean trapped = false;
    private Boolean invis = false;
    private BukkitTask invisTask;
    private Entity lastDamager;
    private String armor = "leatherarmor";
    private Integer pickaxe = Variables.items.getInt("ranking.pickaxe.default");
    private Integer axe = Variables.items.getInt("ranking.axe.default");

    public PlayerHandler(Player player, ArenaHandler arena) {
        this.player = player;
        this.arena = arena;
        this.team = DeluxeBedwars.getInstance().getGameManager().getTeam(player);
    }

    public void setTeam(TeamHandler team) {
        this.team = team;
    }

    public Entity getLastDamager() {
        return this.lastDamager;
    }

    public void killMessage(String fell) {
        Player killer = null;
        if (this.lastDamager != null) {
            if (this.lastDamager instanceof Player) killer = (Player) this.lastDamager;
            else if (this.lastDamager.hasMetadata("BedwarsOWNER")) killer = (Player) this.lastDamager.getMetadata("BedwarsOWNER").get(0).value();
        }

        if (killer != null)
            this.arena.addKill(killer, 1);

        this.team = DeluxeBedwars.getInstance().getGameManager().getTeam(this.player);
        String type = "died";
        if (this.lastDamager != null) {
            if (fell.equalsIgnoreCase("fall")) type = "fall_void";
            else if (fell.equalsIgnoreCase("knock") && this.lastDamager instanceof Player) type = "knocked_void";
            else {
                if (this.lastDamager instanceof Player) type = "killed";
                if (this.lastDamager instanceof IronGolem) type = "golem";
                if (this.lastDamager instanceof Fireball) type = "fireball";
                if (this.lastDamager instanceof TNTPrimed) type = "tnt";
                if (this.lastDamager instanceof Arrow) type = "arrow";
                if (this.lastDamager instanceof Silverfish) type = "silverfish";
            }
        }

        if (this.lastDamager instanceof Player &&
                this.lastDamager == this.player)
            type = "died";
        else if (killer != null && killer == this.player)
            type = "died";

        DataHandler data = killer != null ? new DataHandler(killer) : new DataHandler(this.player);
        String text = data.getPlayerSelectedCosmetic("kill_messages");
        if (text == null) text = "default";

        ConfigurationSection playerSection = this.team != null ? Variables.config.getConfigurationSection("teams." + this.team.getName()) : null;
        if (playerSection == null) return;

        String addon = this.team.getBedStatus() ? "normal_messages" : "final_messages";
        String message = Variables.cosmetics.getString("kill_messages." + text + "." + addon + "." + type);
        if (message == null || message.equals(""))
            message = Variables.cosmetics.getString("kill_messages." + text + "." + this.lastDamager.getType().name().toLowerCase()) != null ? Variables.cosmetics.getString("kill_messages." + text + "." + this.lastDamager.getType().name().toLowerCase()) : Variables.cosmetics.getString("kill_messages." + text + "." + this.lastDamager.getType().name());
        if (message == null || message.equals(""))
            return;

        String newMessage;
        if (killer != null) {
            ConfigurationSection killerSection = DeluxeBedwars.getInstance().getGameManager().getTeam(killer) != null ? Variables.config.getConfigurationSection("teams." + DeluxeBedwars.getInstance().getGameManager().getTeam(killer).getName()) : null;
            if (killerSection == null) return;

            DataHandler killerData = new DataHandler(killer);
            if (!this.team.getBedStatus()) killerData.addPlayerStat("final_kills", 1);
            newMessage = message
                    .replace("%final_kills%", String.valueOf(killerData.getPlayerStat("final_kills")))
                    .replace("%broken_beds%", String.valueOf(killerData.getPlayerStat("broken_beds")))
                    .replace("%killer_color%", killerSection.getString("color"))
                    .replace("%killer%", killer.getName())
                    .replace("%killer_name%", killerSection.getString("name"))
                    .replace("%player_color%", playerSection.getString("color"))
                    .replace("%player%", this.player.getName())
                    .replace("%player_name%", playerSection.getString("name"));
        } else
            newMessage = message
                    .replace("%player_color%", playerSection.getString("color"))
                    .replace("%player%", this.player.getName())
                    .replace("%player_name%", playerSection.getString("name"));

        this.arena.send(Utils.colorize(newMessage));
    }

    public void setTrapped(Boolean trapped) {
        this.trapped = trapped;
    }

    public void setLastDamager(Entity lastDamager) {
        this.lastDamager = lastDamager;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (PlayerHandler.this.lastDamager == lastDamager) PlayerHandler.this.lastDamager = null;
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 200L);
    }

    public Boolean isInvis() {
        return this.invis;
    }

    public void setInvis(Boolean invis, int time) {
        this.invis = invis;

        if (invis) {
            for (PlayerHandler playerHandler : this.arena.getPlayers()) {
                if (playerHandler.getPlayer() == this.player) continue;
                if (playerHandler.getTeam() == this.team) continue;

                DeluxeBedwars.getInstance().getVersionManager().getNMS().hideShowArmor(this.player, playerHandler.getPlayer(), false);
            }

            final Boolean[] foot = {true};
            this.invisTask = new BukkitRunnable() {
                public void run() {
                    Location l = PlayerHandler.this.player.getLocation();
                    l.setY(Math.floor(l.getY()));
                    if (!l.clone().subtract(0.0D, 1.0D, 0.0D).getBlock().isEmpty()) {
                        double x = Math.cos(Math.toRadians(PlayerHandler.this.player.getLocation().getYaw())) * 0.25D;
                        double y = Math.sin(Math.toRadians(PlayerHandler.this.player.getLocation().getYaw())) * 0.25D;
                        if (foot[0])
                            l.add(x, 0.025D, y);
                        else
                            l.subtract(x, -0.025D, y);

                        ParticleEffect.FOOTSTEP.display(0.0F, 0.0F, 0.0F, 0.0F, 2, l, 100.0D);
                        foot[0] = !foot[0];
                    }
                }
            }.runTaskTimer(DeluxeBedwars.getInstance(), 5, 5);

            new BukkitRunnable() {
                @Override
                public void run() {
                    removeInvis();
                    if (PlayerHandler.this.invisTask != null) PlayerHandler.this.invisTask.cancel();
                }
            }.runTaskLater(DeluxeBedwars.getInstance(), time);
        } else {
            removeInvis();
            if (this.invisTask != null) this.invisTask.cancel();
        }
    }

    public void removeInvis() {
        if (this.player.hasPotionEffect(PotionEffectType.INVISIBILITY))
            this.player.removePotionEffect(PotionEffectType.INVISIBILITY);

        for (PlayerHandler playerHandler : this.arena.getPlayers()) {
            if (playerHandler.getPlayer() == this.player) continue;
            if (playerHandler.getTeam() == this.team) continue;

            DeluxeBedwars.getInstance().getVersionManager().getNMS().hideShowArmor(this.player, playerHandler.getPlayer(), true);
        }
    }

    public Boolean isTrapped() {
        return this.trapped;
    }

    public void setShear(Boolean shear) {
        this.shear = shear;
    }

    public Boolean getShear() {
        return this.shear;
    }

    public int getAxe() {
        return this.axe;
    }

    public int getPickaxe() {
        return this.pickaxe;
    }

    public void setPickaxe(int pickaxe) {
        this.pickaxe = pickaxe;
    }

    public void setAxe(int axe) {
        this.axe = axe;
    }

    public String getArmor() {
        return this.armor;
    }

    public void setArmor(String armor) {
        this.armor = armor;
    }

    public Player getPlayer() {
        return this.player;
    }

    public TeamHandler getTeam() {
        return this.team;
    }

    public ArenaHandler getArena() {
        return this.arena;
    }

    public Boolean isAlive() {
        return this.alive;
    }

    public void setAlive(Boolean alive) {
        this.alive = alive;
    }
}