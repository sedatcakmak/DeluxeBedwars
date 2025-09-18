package me.sedattr.bedwars.handlers;

import java.util.ArrayList;
import java.util.List;

public class UpgradesHandler {
    private final TeamHandler team;
    private Integer protectionLevel = 0;
    private Integer hasteLevel = 0;
    private Integer generatorLevel = 0;
    private Integer sharpnessLevel = 0;
    private Boolean dragonBuff = false;
    private Boolean healPool = false;
    private final List<String> traps = new ArrayList<>();

    public UpgradesHandler(TeamHandler team) {
        this.team = team;
    }

    public TeamHandler getTeam() {
        return this.team;
    }

    public Boolean getDragonBuff() {
        return this.dragonBuff;
    }

    public void setDragonBuff(Boolean dragonBuff) {
        this.dragonBuff = dragonBuff;
    }

    public Integer getSharpnessLevel() {
        return this.sharpnessLevel;
    }

    public void setSharpnessLevel(Integer sharpnessLevel) {
        this.sharpnessLevel = sharpnessLevel;
    }

    public Integer getGeneratorLevel() {
        return this.generatorLevel;
    }

    public void setGeneratorLevel(Integer generatorLevel) {
        this.generatorLevel = generatorLevel;
    }

    public Integer getProtectionLevel() {
        return this.protectionLevel;
    }

    public void setProtectionLevel(Integer protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    public Integer getHasteLevel() {
        return this.hasteLevel;
    }

    public void setHasteLevel(Integer hasteLevel) {
        this.hasteLevel = hasteLevel;
    }

    public Boolean getHealPool() {
        return this.healPool;
    }

    public void setHealPool(Boolean healPool) {
        this.healPool = healPool;
    }

    public List<String> getTraps() {
        return this.traps;
    }

    public void addTrap(String type) {
        this.traps.add(type);
    }

    public void removeTrap(int index) {
        this.traps.remove(index);
    }
}
