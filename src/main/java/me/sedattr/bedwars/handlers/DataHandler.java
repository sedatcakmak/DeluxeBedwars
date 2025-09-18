package me.sedattr.bedwars.handlers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataHandler {
    Player player;

    public DataHandler(Player player) {
        this.player = player;
    }

    public int getPlayerInfo(String type) {
        return Variables.data.getInt("players." + this.player.getUniqueId() + ".infos." + type);
    }

    public String getPlayerPrestige() {
        return Variables.data.getString("players." + this.player.getUniqueId() + ".infos.prestige");
    }

    public Boolean hasPlayerMoney(int count) {
        return getPlayerInfo("money") >= count;
    }

    public void removePlayerMoney(int count) {
        Variables.data.set("players." + this.player.getUniqueId() + ".infos.money", getPlayerInfo("money")-count);
        saveData();
    }

    // player, "victory_dances"
    public String getPlayerSelectedCosmetic(String type) {
        return Variables.data.getString("players." + this.player.getUniqueId() + ".cosmetics." + type + ".selected");
    }

    public int getPlayerStat(String type) {
        return Variables.data.getInt("players." + this.player.getUniqueId() + ".stats." + type);
    }

    public void addPlayerStat(String type, int count) {
        Variables.data.set("players." + this.player.getUniqueId() + ".stats." + type, (getPlayerStat(type)+count));
        saveData();
    }

    // player, "victory_dances"
    public List<String> getPlayerUnlockedCosmetics(String type) {
        List<String> cosmetics = Variables.data.getStringList("players." + this.player.getUniqueId() + ".cosmetics." + type + ".unlocked");

        return cosmetics != null ? cosmetics : new ArrayList<>();
    }

    // player, "victory_dances", "dragon_rider"
    public Boolean hasPlayerCosmetic(String type, String cosmetic) {
        List<String> cosmetics = Variables.data.getStringList("players." + this.player.getUniqueId() + ".cosmetics." + type + ".unlocked");

        return cosmetic != null && cosmetics.size() > 0 && cosmetics.contains(cosmetic);
    }

    // player, "victory_dances", "dragon_rider"
    public void setPlayerSelectedCosmetic(String type, String cosmetic) {
        Variables.data.set("players." + this.player.getUniqueId() + ".cosmetics." + type + ".selected", cosmetic);
        saveData();
    }

    // player, "victory_dances", true
    public void setRandom(String type, Boolean cosmetic) {
        Variables.data.set("players." + this.player.getUniqueId() + ".cosmetics." + type + ".random", cosmetic);
        saveData();
    }

    // player, "victory_dances", true
    public Boolean getRandom(String type) {
        return Variables.data.getBoolean("players." + this.player.getUniqueId() + ".cosmetics." + type + ".random");
    }

    // player, "victory_dances", "dragon_rider"
    public void addPlayerCosmetic(String type, String cosmetic) {
        List<String> cosmetics = getPlayerUnlockedCosmetics(type);
        cosmetics.add(cosmetic);

        Variables.data.set("players." + this.player.getUniqueId() + ".cosmetics." + type + ".unlocked", cosmetics);
        saveData();
    }

    // getCosmetic("victory_dances", "wither_rider");
    public ConfigurationSection getCosmetic(String type, String input) {
        return Variables.cosmetics.getConfigurationSection(type + "." + input);
    }

    public void saveData() {
        File file = new File(DeluxeBedwars.getInstance().getDataFolder(), "data.yml");
        try {
            Variables.data.save(file);
        } catch (IOException ignored) {
        }
    }
}