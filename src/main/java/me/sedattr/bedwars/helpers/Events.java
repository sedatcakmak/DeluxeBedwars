package me.sedattr.bedwars.helpers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Events {
    private Enums.EventState eventType;
    private BukkitTask task;
    private final ArenaHandler arena;
    private int eventTime;
    private int nextTime;
    private String eventName;
    private String diamondNumber = "I";
    private String emeraldNumber = "I";
    private int diamondTime = Variables.config.getInt("generators.diamond.tiers.level1.spawn");
    private int emeraldTime = Variables.config.getInt("generators.emerald.tiers.level1.spawn");

    public Events(ArenaHandler arena) {
        this.arena = arena;
    }

    public void cancel() {
        this.eventType = Enums.EventState.DIAMOND_II;
        this.eventName = Utils.colorize(Variables.config.getString("events.diamond2.name"));
        this.eventTime = Variables.config.getInt("events.diamond2.time");
        this.task.cancel();
    }

    public BukkitTask getTask() {
        return this.task;
    }

    public String getEventName() {
        return this.eventName;
    }

    public int getEventTime() {
        return this.eventTime;
    }

    public void startTask() {
        if (this.eventType == null) this.eventType = Enums.EventState.DIAMOND_II;
        if (this.eventName == null || this.eventName.equals("")) this.eventName = Utils.colorize(Variables.config.getString("events.diamond2.name"));
        if (this.eventTime <= 0) this.eventTime = Variables.config.getInt("events.diamond2.time");

        this.task = (new BukkitRunnable() {
            public void run() {
                Events.this.eventTime--;
                if (Events.this.eventTime <= 0) {
                    Events.this.nextTime = 0;
                    Events.this.nextEvent();
                }
            }
        }).runTaskTimer(DeluxeBedwars.getInstance(), 20L, 20L);
    }

    public int getDiamondTime() {
        return this.diamondTime;
    }

    public String getDiamondNumber() {
        return this.diamondNumber;
    }

    public String getEmeraldNumber() {
        return this.emeraldNumber;
    }

    public int getEmeraldTime() {
        return this.emeraldTime;
    }

    private void nextEvent() {
        switch (this.eventType) {
            case DIAMOND_II:
                this.arena.send(Utils.colorize(Variables.config.getString("generators.diamond.message")
                        .replace("%tier%", "2")
                        .replace("%tier_roman%", "II")));
                this.eventType = Enums.EventState.EMERALD_II;
                this.eventName = Utils.colorize(Variables.config.getString("events.emerald2.name"));
                this.eventTime = Variables.config.getInt("events.emerald2.time");
                this.nextTime = Variables.config.getInt("generators.diamond.tiers.level2.spawn");
                this.diamondNumber = "II";
                this.diamondTime = this.nextTime;
                break;
            case EMERALD_II:
                this.arena.send(Utils.colorize(Variables.config.getString("generators.emerald.message")
                        .replace("%tier%", "2")
                        .replace("%tier_roman%", "II")));
                this.eventType = Enums.EventState.DIAMOND_III;
                this.eventName = Utils.colorize(Variables.config.getString("events.diamond3.name"));
                this.eventTime = Variables.config.getInt("events.diamond3.time");
                this.nextTime = Variables.config.getInt("generators.emerald.tiers.level2.spawn");
                this.emeraldNumber = "II";
                this.emeraldTime = this.nextTime;
                break;
            case DIAMOND_III:
                this.arena.send(Utils.colorize(Variables.config.getString("generators.diamond.message")
                        .replace("%tier%", "3")
                        .replace("%tier_roman%", "III")));
                this.eventType = Enums.EventState.EMERALD_III;
                this.eventName = Utils.colorize(Variables.config.getString("events.emerald3.name"));
                this.eventTime = Variables.config.getInt("events.emerald3.time");
                this.nextTime = Variables.config.getInt("generators.diamond.tiers.level3.spawn");
                this.diamondNumber = "III";
                this.diamondTime = this.nextTime;
                break;
            case EMERALD_III:
                this.arena.send(Utils.colorize(Variables.config.getString("generators.emerald.message")
                        .replace("%tier%", "3")
                        .replace("%tier_roman%", "III")));
                this.eventType = Enums.EventState.BED_DESTROY;
                this.eventName = Utils.colorize(Variables.config.getString("events.bed_destroy.name"));
                this.eventTime = Variables.config.getInt("events.bed_destroy.time");
                this.nextTime = Variables.config.getInt("generators.emerald.tiers.level3.spawn");
                this.emeraldNumber = "III";
                this.emeraldTime = this.nextTime;
                break;
            case BED_DESTROY:
                this.arena.send(Utils.sendMessage(null, "beds_gone"));
                this.arena.getTeams().forEach(team -> team.setBedStatus(false));
                this.eventType = Enums.EventState.SUDDEN_DEATH;
                this.eventTime = Variables.config.getInt("events.sudden_death.time");
                this.eventName = Utils.colorize(Variables.config.getString("events.sudden_death.name"));
                break;
            case SUDDEN_DEATH:
                this.eventType = Enums.EventState.GAME_END;
                this.eventTime = Variables.config.getInt("events.game_end.time");
                this.eventName = Utils.colorize(Variables.config.getString("events.game_end.name"));
                for (TeamHandler team : this.arena.getTeams()) {
                    int aliveCount = team.getAlivePlayers().size();
                    if (aliveCount <= 0) continue;

                    EnderDragon dragon = (EnderDragon) this.arena.getSpectateLocation().getWorld().spawnEntity(this.arena.getSpectateLocation(), EntityType.ENDER_DRAGON);
                    dragon.setCustomName(team.getName() + "'s Dragon");
                    dragon.setMetadata("BedwarsDRAGON", new FixedMetadataValue(DeluxeBedwars.getInstance(), team));
                    dragon.setMetadata("BedwarsDRAGONARENA", new FixedMetadataValue(DeluxeBedwars.getInstance(), this.arena));
                    this.arena.addDragon(dragon);

                    if (team.getUpgrades().getDragonBuff()) {
                        EnderDragon dragon2 = (EnderDragon) this.arena.getSpectateLocation().getWorld().spawnEntity(this.arena.getSpectateLocation(), EntityType.ENDER_DRAGON);
                        dragon2.setCustomName(team.getName() + "'s Dragon");
                        dragon2.setMetadata("BedwarsDRAGON", new FixedMetadataValue(DeluxeBedwars.getInstance(), team));
                        dragon2.setMetadata("BedwarsDRAGONARENA", new FixedMetadataValue(DeluxeBedwars.getInstance(), this.arena));
                        this.arena.addDragon(dragon2);
                    }
                }

                break;
            case GAME_END:
                if (this.arena.getState() != Enums.ArenaState.ENDED) {
                    this.arena.endGame();
                    this.task.cancel();
                }
                break;
        }
    }
}
