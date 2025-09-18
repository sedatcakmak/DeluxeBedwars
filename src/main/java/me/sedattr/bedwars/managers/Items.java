package me.sedattr.bedwars.managers;

import com.hakan.invapi.inventory.invs.HInventory;
import com.hakan.invapi.inventory.item.ClickableItem;
import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.*;
import me.sedattr.bedwars.helpers.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Items {
    private final Player player;
    private final ArenaHandler arena;
    private final TeamHandler team;

    public Items(Player player, ArenaHandler arena) {
        this.player = player;
        this.arena = arena;
        this.team = DeluxeBedwars.getInstance().getGameManager().getTeam(player);
    }

    public void otherItems(ConfigurationSection section, HInventory gui) {
        if (section == null)
            return;

        Set<String> items = section.getKeys(false);
        if (items == null || items.isEmpty())
            return;

        for (String item : items)
            createItem(section.getConfigurationSection(item), gui);
    }

    public ItemStack createItem(ConfigurationSection section, HInventory gui) {
        if (section == null) return null;

        String data = section.getString("data") != null ? section.getString("data") : null;
        if (data == null) {
            if (this.team != null) data = String.valueOf(Variables.blockDatas.get(this.team.getName().toLowerCase()));
            else data = "0";
        }

        ItemBuilder builder = null;
        if (section.getString("material").contains("POTION") && section.getString("main") != null) {
            builder = new ItemBuilder(PotionType.getByEffect(PotionEffectType.getByName(section.getString("main"))), Math.max(section.getInt("amount"), 1), 1);
        } else if (section.getString("material").contains("PLAYER_HEAD") || section.getString("material").contains("SKULL_ITEM")) {
            String skin = section.getString("value") != null ? section.getString("value") : section.getString("skin");

            if (skin != null && !skin.equals(""))
                builder = new ItemBuilder(new SkullTexture().getSkull(skin.replace("%player%", this.player.getName())));
        }

        if (builder == null)
            builder = new ItemBuilder(Material.getMaterial(section.getString("material")), Math.max(section.getInt("amount"), 1), Byte.parseByte(data));


        String name = section.getString("name") != null ? section.getString("name") : null;
        if (this.team != null && name != null) {
            name = name.replace("%team_name%", Variables.config.getString("teams." + this.team.getName() + ".name"))
                    .replace("%team_color%", Variables.config.getString("teams." + this.team.getName() + ".color"))
                    .replace("%team_short%", Variables.config.getString("teams." + this.team.getName() + ".short"));
        }

        if (this.arena != null && name != null)
            name = name.replace("%arena_name%", this.arena.getName());

        if (name != null) {
            DataHandler dataHandler = new DataHandler(this.player);

            int finalKills = dataHandler.getPlayerStat("final_kills");
            int finalDeaths = dataHandler.getPlayerStat("final_deaths");
            int kills = dataHandler.getPlayerStat("kills");
            int deaths = dataHandler.getPlayerStat("deaths");
            int losses = dataHandler.getPlayerStat("losses");
            int wins = dataHandler.getPlayerStat("wins");
            name = name
                    .replace("%fk/fd%", String.format("%.2f", (double) finalKills / (finalDeaths <= 0 ? 1 : finalDeaths)))
                    .replace("%k/d%", String.format("%.2f", (double) kills / (deaths <= 0 ? 1 : deaths)))
                    .replace("%w/l%", String.format("%.2f", (double) wins / (losses <= 0 ? 1 : losses)))
                    .replace("%win_streak%", Utils.format(dataHandler.getPlayerStat("win_streak")))
                    .replace("%kills%", Utils.format(kills))
                    .replace("%final_kills%", Utils.format(finalKills))
                    .replace("%deaths%", Utils.format(deaths))
                    .replace("%losses%", Utils.format(losses))
                    .replace("%final_deaths%", Utils.format(finalDeaths))
                    .replace("%wins%", Utils.format(wins))
                    .replace("%broken_beds%", Utils.format(dataHandler.getPlayerStat("broken_beds")))
                    .replace("%lost_beds%", Utils.format(dataHandler.getPlayerStat("lost_beds")))
                    .replace("%coins%", Utils.format(dataHandler.getPlayerInfo("money")))
                    .replace("%level%", Utils.format(dataHandler.getPlayerInfo("level")))
                    .replace("%prestige%", Utils.format(dataHandler.getPlayerInfo("prestige")))
                    .replace("%xp%", Utils.format(dataHandler.getPlayerInfo("xp")));

            builder.setName(Utils.colorize(name));
        }

        List<String> lores = section.getStringList("lore");
        if (lores != null && lores.size() > 0) for (String lore : lores) {
            if (lore == null) continue;

            if (this.team != null) {
                lore = lore.replace("%team_name%", Variables.config.getString("teams." + this.team.getName() + ".name"))
                        .replace("%team_color%", Variables.config.getString("teams." + this.team.getName() + ".color"))
                        .replace("%team_short%", Variables.config.getString("teams." + this.team.getName() + ".short"));
            }

            ArenaSetupHandler setup = DeluxeBedwars.getInstance().getGameManager().getSetup(this.player, "");
            if (setup != null) {
                TeamSetupHandler team = setup.getTeam();
                if (team != null) {
                    ConfigurationSection status = Variables.menus.getConfigurationSection("team_setup.status");
                    List<String> missing = team.missing();
                    if (lore.contains("%team_missing%")) {
                        if (missing == null || missing.size() <= 0) continue;
                        for (String line : missing)
                            builder.addLoreLine(Utils.colorize(status.getString("missing")
                                    .replace("%missing%", line)));
                        continue;
                    }

                    Location minLocation = team.getMinLocation();
                    Location maxLocation = team.getMaxLocation();
                    Location spawnLocation = team.getSpawnLocation();
                    Location upgradeLocation = team.getUpgradeLocation();
                    Location shopLocation = team.getShopLocation();
                    Location bedLocation = team.getBedLocation();
                    Location generatorLocation = team.getGenerator();
                    lore = lore
                            .replace("%team_status%", missing != null ? status.getString("not_ready") : status.getString("ready"))
                            .replace("%team_max_players%", String.valueOf(team.getMaxPlayers()))
                            .replace("%min_protection%", minLocation != null ? minLocation.getBlockX() + ", " + minLocation.getBlockY() + ", " + minLocation.getBlockZ() : status.getString("not_set"))
                            .replace("%max_protection%", maxLocation != null ? maxLocation.getBlockX() + ", " + maxLocation.getBlockY() + ", " + maxLocation.getBlockZ() : status.getString("not_set"))
                            .replace("%spawn_location%", spawnLocation != null ? spawnLocation.getBlockX() + ", " + spawnLocation.getBlockY() + ", " + spawnLocation.getBlockZ() : status.getString("not_set"))
                            .replace("%upgrade_location%", upgradeLocation != null ? upgradeLocation.getBlockX() + ", " + upgradeLocation.getBlockY() + ", " + upgradeLocation.getBlockZ() : status.getString("not_set"))
                            .replace("%shop_location%", shopLocation != null ? shopLocation.getBlockX() + ", " + shopLocation.getBlockY() + ", " + shopLocation.getBlockZ() : status.getString("not_set"))
                            .replace("%bed_location%", bedLocation != null ? bedLocation.getBlockX() + ", " + bedLocation.getBlockY() + ", " + bedLocation.getBlockZ() : status.getString("not_set"))
                            .replace("%generator_location%", generatorLocation != null ? generatorLocation.getBlockX() + ", " + generatorLocation.getBlockY() + ", " + generatorLocation.getBlockZ() : status.getString("not_set"));
                }

                ConfigurationSection status = Variables.menus.getConfigurationSection("arena_setup.status");
                List<String> missing = setup.missing();
                if (lore.contains("%missing%")) {
                    if (missing == null || missing.size() <= 0) continue;
                    for (String line : missing)
                        builder.addLoreLine(Utils.colorize(status.getString("missing")
                                .replace("%missing%", line)));
                    continue;
                }

                Location minLocation = setup.getMinLocation();
                Location maxLocation = setup.getMaxLocation();
                Location spectateLocation = setup.getSpectateLocation();
                Location waitingLocation = setup.getWaitingLocation();
                List<Location> diamondGenerators = setup.getDiamondGenerators();
                List<Location> emeraldGenerators = setup.getEmeraldGenerators();

                List<String> types = new ArrayList<>(Variables.messages.getConfigurationSection("types").getKeys(false));
                String nextType = setup.getType() != null && (types.size()-1 > types.indexOf(setup.getType())) ? types.get(types.indexOf(setup.getType())+1) : types.get(0);
                List<String> modes = new ArrayList<>(Variables.messages.getConfigurationSection("modes").getKeys(false));
                String nextMode = setup.getMode() != null && (modes.size()-1 > modes.indexOf(setup.getMode())) ? modes.get(modes.indexOf(setup.getMode())+1) : modes.get(0);

                if (lore.contains("%diamond_list%")) {
                    if (diamondGenerators == null || diamondGenerators.size() <= 0) continue;
                    for (Location loc : diamondGenerators)
                        builder.addLoreLine(Utils.colorize(status.getString("list")
                                .replace("%y%", String.valueOf(loc.getBlockY()))
                                .replace("%z%", String.valueOf(loc.getBlockZ()))
                                .replace("%x%", String.valueOf(loc.getBlockX()))));
                    continue;
                }

                if (lore.contains("%emerald_list%")) {
                    if (emeraldGenerators == null || emeraldGenerators.size() <= 0) continue;
                    for (Location loc : emeraldGenerators)
                        builder.addLoreLine(Utils.colorize(status.getString("list")
                                .replace("%y%", String.valueOf(loc.getBlockY()))
                                .replace("%z%", String.valueOf(loc.getBlockZ()))
                                .replace("%x%", String.valueOf(loc.getBlockX()))));
                    continue;
                }

                lore = lore
                        .replace("%diamond_status%", diamondGenerators.size() > 0 ? status.getString("added") : status.getString("not_added"))
                        .replace("%emerald_status%", emeraldGenerators.size() > 0 ? status.getString("added") : status.getString("not_added"))
                        .replace("%status%", missing != null ? status.getString("not_ready") : status.getString("ready"))
                        .replace("%next_type%", nextType)
                        .replace("%next_mode%", nextMode)
                        .replace("%mode%", setup.getMode() != null ? setup.getMode() : status.getString("not_set"))
                        .replace("%type%", setup.getType() != null ? setup.getType() : status.getString("not_set"))
                        .replace("%min_players%", String.valueOf(setup.getMinPlayers()))
                        .replace("%max_players%", String.valueOf(setup.getMaxPlayers()))
                        .replace("%min_location%", minLocation != null ? minLocation.getBlockX() + ", " + minLocation.getBlockY() + ", " + minLocation.getBlockZ() : status.getString("not_set"))
                        .replace("%max_location%", maxLocation != null ? maxLocation.getBlockX() + ", " + maxLocation.getBlockY() + ", " + maxLocation.getBlockZ() : status.getString("not_set"))
                        .replace("%waiting_location%", waitingLocation != null ? waitingLocation.getBlockX() + ", " + waitingLocation.getBlockY() + ", " + waitingLocation.getBlockZ() : status.getString("not_set"))
                        .replace("%spectate_location%", spectateLocation != null ? spectateLocation.getBlockX() + ", " + spectateLocation.getBlockY() + ", " + spectateLocation.getBlockZ() : status.getString("not_set"));
            }

            if (this.arena != null) {
                String status = Variables.messages.getString("status.waiting");
                if (this.arena.getState() == Enums.ArenaState.PLAYING) status = Variables.messages.getString("status.playing");
                if (this.arena.getState() == Enums.ArenaState.STARTING) status = Variables.messages.getString("status.starting");
                if (this.arena.getState() == Enums.ArenaState.ENDED) status = Variables.messages.getString("status.ended");

                lore = lore
                        .replace("%arena_players%", String.valueOf(this.arena.getPlayers().size()))
                        .replace("%arena_min_players%", String.valueOf(this.arena.getMinPlayers()))
                        .replace("%arena_max_players%", String.valueOf(this.arena.getMaxPlayers()))
                        .replace("%status%", status)
                        .replace("%arena_name%", this.arena.getName());
            }

            DataHandler dataHandler = new DataHandler(this.player);
            for (String entry : Variables.cosmetics.getKeys(false)) {
                Set<String> cosmetics = Variables.cosmetics.getConfigurationSection(entry).getKeys(false);
                cosmetics.remove("default");

                String type = "";
                if (entry.equalsIgnoreCase("victory_dances")) type = "dance";
                if (entry.equalsIgnoreCase("kill_messages")) type = "killmessage";
                if (entry.equalsIgnoreCase("projectile_trails")) type = "trail";
                if (entry.equalsIgnoreCase("final_kill_effects")) type = "finalkill";
                if (entry.equalsIgnoreCase("kill_effects")) type = "kill";
                if (entry.equalsIgnoreCase("death_cries")) type = "cry";
                if (entry.equalsIgnoreCase("bed_destroys")) type = "bed";
                if (entry.equalsIgnoreCase("wood_skins")) type = "wood";
                if (entry.equalsIgnoreCase("shop_npcs")) type = "shop";
                if (entry.equalsIgnoreCase("upgrade_npcs")) type = "upgrade";
                if (entry.equalsIgnoreCase("kits")) type = "kit";

                String selected = dataHandler.getPlayerSelectedCosmetic(entry);
                if (dataHandler.getRandom(entry)) selected = "Random";
                if (!dataHandler.getRandom(entry) && selected != null) selected = Variables.cosmetics.getString(entry + "." + dataHandler.getPlayerSelectedCosmetic(entry) + ".name");
                if (selected == null) selected = "Default";

                lore = lore
                        .replace("%" + type + "_percent%", String.valueOf((dataHandler.getPlayerUnlockedCosmetics(entry).size() * 100) / (cosmetics.size() <= 0 ? 1 :cosmetics.size())))
                        .replace("%unlocked_" + type + "%", String.valueOf(dataHandler.getPlayerUnlockedCosmetics(entry).size()))
                        .replace("%selected_" + type + "%", selected)
                        .replace("%total_" + type + "%", String.valueOf(cosmetics.size()));
            }

            int finalKills = dataHandler.getPlayerStat("final_kills");
            int finalDeaths = dataHandler.getPlayerStat("final_deaths");
            int kills = dataHandler.getPlayerStat("kills");
            int deaths = dataHandler.getPlayerStat("deaths");
            int losses = dataHandler.getPlayerStat("losses");
            int wins = dataHandler.getPlayerStat("wins");
            lore = lore
                    .replace("%fk/fd%", String.format("%.2f", (double) finalKills / (finalDeaths <= 0 ? 1 : finalDeaths)))
                    .replace("%k/d%", String.format("%.2f", (double) kills / (deaths <= 0 ? 1 : deaths)))
                    .replace("%w/l%", String.format("%.2f", (double) wins / (losses <= 0 ? 1 : losses)))
                    .replace("%win_streak%", Utils.format(dataHandler.getPlayerStat("win_streak")))
                    .replace("%kills%", Utils.format(kills))
                    .replace("%final_kills%", Utils.format(finalKills))
                    .replace("%deaths%", Utils.format(deaths))
                    .replace("%losses%", Utils.format(losses))
                    .replace("%final_deaths%", Utils.format(finalDeaths))
                    .replace("%wins%", Utils.format(wins))
                    .replace("%broken_beds%", Utils.format(dataHandler.getPlayerStat("broken_beds")))
                    .replace("%lost_beds%", Utils.format(dataHandler.getPlayerStat("lost_beds")))
                    .replace("%coins%", Utils.format(dataHandler.getPlayerInfo("money")))
                    .replace("%level%", Utils.format(dataHandler.getPlayerInfo("level")))
                    .replace("%prestige%", Utils.format(dataHandler.getPlayerInfo("prestige")))
                    .replace("%xp%", Utils.format(dataHandler.getPlayerInfo("xp")));

            builder.addLoreLine(Utils.colorize(lore));
        }

        builder.setUnbreakable(section.getBoolean("unbreakable"));
        List<String> enchants = section.getStringList("enchants");
        if (enchants != null && enchants.size() > 0) for (String enchant : enchants) {
            if (enchant == null) continue;
            String[] args = enchant.split("[:]", 2);
            if (args.length < 2) continue;

            builder.addEnchant(Enchantment.getByName(args[0]), Integer.parseInt(args[1]));
        }

        List<String> flags = section.getStringList("flags");
        if (flags != null && flags.size() > 0) for (String flag : flags) {
            if (flag == null) continue;

            builder.addFlags(ItemFlag.valueOf(flag));
        }

        if (gui != null && section.getInt("slot") > 0) gui.setItem(section.getInt("slot") - 1, ClickableItem.empty(builder.toItemStack()));
        else if (gui != null && section.getIntegerList("slots") != null && section.getIntegerList("slots").size() > 0) for (int i : section.getIntegerList("slots")) gui.setItem(i-1, ClickableItem.empty(builder.toItemStack()));
        return builder.toItemStack();
    }
}