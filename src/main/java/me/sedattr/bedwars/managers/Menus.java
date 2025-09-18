package me.sedattr.bedwars.managers;

import com.hakan.invapi.InventoryAPI;
import com.hakan.invapi.inventory.invs.HInventory;
import com.hakan.invapi.inventory.item.ClickableItem;
import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.*;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Menus {
    private final DeluxeBedwars plugin;
    private final Player player;

    public Menus(Player player) {
        this.player = player;
        this.plugin = DeluxeBedwars.getInstance();
    }

    public void openConfirmCosmeticMenu(String type, String cosmetic) {
        ConfigurationSection section = Variables.menus.getConfigurationSection("confirm_cosmetic");
        if (section == null) {
            Bukkit.getConsoleSender().sendMessage("§8[§bDeluxeBedwars§8] §cConfirm cosmetic menu is broken!");
            return;
        }

        HInventory gui = InventoryAPI.getInventoryManager()
                .setTitle(Utils.colorize(section.getString("title")))
                .setSize(section.getInt("size") > 6 ? section.getInt("size") / 9 : section.getInt("size"))
                .setId("confirm_cosmetic")
                .create();

        new Items(this.player, null).otherItems(section.getConfigurationSection("other"), gui);
        for (String entry : section.getConfigurationSection("items").getKeys(false)) {
            ItemStack item = new Items(this.player, null).createItem(section.getConfigurationSection("items." + entry), null);
            if (item == null) continue;

            List<String> lores = section.getStringList("items." + entry + ".lore");
            List<String> lore = new ArrayList<>();

            if (lores != null && lores.size() > 0) for (String line : lores) {
                lore.add(Utils.colorize(line
                        .replace("%price%", Utils.format(Variables.cosmetics.getInt(type + "." + cosmetic + ".price")))
                        .replace("%item%", Variables.cosmetics.getString(type + "." + cosmetic + ".name"))));
            }

            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            item.setItemMeta(meta);

            gui.setItem(section.getInt("items." + entry + ".slot") - 1, ClickableItem.of(item, (event) -> {
                if (entry.equalsIgnoreCase("cancel"))
                    this.player.closeInventory();
                else if (entry.equalsIgnoreCase("confirm")) {
                    DataHandler data = new DataHandler(this.player);
                    data.addPlayerCosmetic(type, cosmetic);
                    data.removePlayerMoney(Variables.cosmetics.getInt(type + "." + cosmetic + ".price"));
                    this.player.sendMessage("bought");
                    this.player.closeInventory();

                    if (data.getPlayerSelectedCosmetic(type) == null
                            || data.getPlayerSelectedCosmetic(type).equalsIgnoreCase("random")
                            || data.getPlayerSelectedCosmetic(type).equalsIgnoreCase("default")) {
                        data.setRandom(type, null);
                        data.setPlayerSelectedCosmetic(type, cosmetic);
                    }
                }
            }));
        }

        gui.open(this.player);
    }

    public void cosmeticBuy(InventoryAction action, String type, String entry, String status) {
        DataHandler data = new DataHandler(this.player);
        ConfigurationSection itemSection = Variables.cosmetics.getConfigurationSection(type + "." + entry);

        if (action == InventoryAction.PICKUP_HALF && !entry.equalsIgnoreCase("default") && !entry.equalsIgnoreCase("random")) {
            if (itemSection != null) {
                switch (type.toLowerCase()) {
                    case "death_cries":
                        if (itemSection.getString("sound") == null)
                            break;
                        if (itemSection.getString("sound").equalsIgnoreCase("none"))
                            break;

                        Sound sound = Sound.valueOf(itemSection.getString("sound"));
                        this.player.playSound(this.player.getLocation(), sound, Math.max(itemSection.getInt("volume"), 1), Math.max(itemSection.getInt("pitch"), 1));
                        return;
                }
            }
            return;
        }

        switch (entry.toLowerCase()) {
            case "close":
                this.player.closeInventory();
                return;
            case "back":
                openNormalMenu("cosmetics");
                return;
        }

        switch (status.toLowerCase()) {
            case "available":
                openConfirmCosmeticMenu(type, entry);
                return;
            case "unlocked":
                data.setRandom(type, null);
                if (entry.equalsIgnoreCase("random")) {
                    List<String> cosmetics = data.getPlayerUnlockedCosmetics(type);
                    cosmetics.add("default");

                    if (cosmetics.size() > 0)
                        data.setPlayerSelectedCosmetic(type, cosmetics.get(new Random().nextInt(cosmetics.size())));

                    data.setRandom(type, true);
                } else {
                    if (itemSection == null)
                        return;

                    data.setPlayerSelectedCosmetic(type, entry);
                }

                this.player.sendMessage("selected successfully");
                openCosmeticListMenu(type);
        }
    }

    public void openCosmeticListMenu(String type) {
        ConfigurationSection section = Variables.menus.getConfigurationSection(type);
        if (section == null) {
            Bukkit.getConsoleSender().sendMessage("§8[§bDeluxeBedwars§8] §cCosmetic menu with name " + type + " is not found!");
            return;
        }

        HInventory gui = InventoryAPI.getInventoryManager()
                .setTitle(Utils.colorize(section.getString("title")))
                .setSize(section.getInt("size") > 6 ? section.getInt("size") / 9 : section.getInt("size"))
                .setId("cosmetic_list")
                .create();

        new Items(this.player, null).otherItems(section.getConfigurationSection("other"), gui);
        DataHandler data = new DataHandler(this.player);
        for (String entry : Variables.menus.getConfigurationSection("cosmetic_settings.other").getKeys(false)) {
            ItemStack item = new Items(this.player, null).createItem(Variables.menus.getConfigurationSection("cosmetic_settings.other." + entry), null);

            String status = "unlocked";
            if (data.getRandom(type) && entry.equalsIgnoreCase("random")) status = "selected";
            else if (!data.getRandom(type) && data.getPlayerSelectedCosmetic(type) != null && data.getPlayerSelectedCosmetic(type).equals(entry))
                status = "selected";

            List<String> lore = new ArrayList<>();
            List<String> lines = Variables.menus.getStringList("cosmetic_settings.other." + entry + ".lore");
            if (lines != null && lines.size() > 0) for (String line : lines) {
                lore.add(Utils.colorize(line
                        .replace("%status%", Variables.menus.getString("cosmetic_settings.status." + status)
                                .replace("%required%", Utils.format(Variables.cosmetics.getInt(type + "." + entry + ".price") - data.getPlayerInfo("money"))))));
            }
            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            item.setItemMeta(meta);

            String finalStatus = status;
            if (status.equalsIgnoreCase("unlocked"))
                gui.setItem(section.getInt(entry) - 1, ClickableItem.of(item, (event) -> cosmeticBuy(event.getAction(), type, entry, finalStatus)));
            else
                gui.setItem(section.getInt(entry) - 1, ClickableItem.empty(item));
        }

        int i = 0;
        for (String entry : Variables.cosmetics.getConfigurationSection(type).getKeys(false)) {
            if (entry.equalsIgnoreCase("default")) continue;
            if (section.getConfigurationSection("items." + entry) == null) continue;

            ConfigurationSection cosmeticSection = Variables.cosmetics.getConfigurationSection(type + "." + entry);
            ItemStack item = new Items(this.player, null).createItem(section.getConfigurationSection("items." + entry), null);
            if (item == null) continue;

            String status = "available";
            if (!cosmeticSection.getBoolean("buyable")) status = "unavailable";
            else if (!data.getRandom(type) && data.getPlayerSelectedCosmetic(type) != null && data.getPlayerSelectedCosmetic(type).equals(entry))
                status = "selected";
            else if (data.hasPlayerCosmetic(type, entry)) status = "unlocked";
            else if (!data.hasPlayerMoney(cosmeticSection.getInt("price"))) status = "need_money";

            List<String> lore = new ArrayList<>();
            List<String> lines = section.getStringList("item.lore");
            if (lines != null && lines.size() > 0) for (String line : lines) {
                if (line.contains("%description%")) {
                    List<String> description = cosmeticSection.getStringList("description");
                    if (description != null && description.size() > 0)
                        for (String line2 : description)
                            lore.add(Utils.colorize(line2
                                    .replace("%rarity%", Variables.messages.getString("rarities." + cosmeticSection.getString("rarity")))
                                    .replace("%status%", Variables.menus.getString("cosmetic_settings.status." + status))
                                    .replace("%required%", Utils.format(cosmeticSection.getInt("price") - data.getPlayerInfo("money")))
                                    .replace("%price%", Utils.format(cosmeticSection.getInt("price")))));
                    continue;
                }

                lore.add(Utils.colorize(line
                        .replace("%price%", Utils.format(cosmeticSection.getInt("price")))
                        .replace("%rarity%", Variables.messages.getString("rarities." + cosmeticSection.getString("rarity")))
                        .replace("%status%", Variables.menus.getString("cosmetic_settings.status." + status)
                                .replace("%required%", Utils.format(cosmeticSection.getInt("price") - data.getPlayerInfo("money"))))));
            }

            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            if (section.getString("item.name") != null)
                meta.setDisplayName(Utils.colorize(section.getString("item.name").replace("%name%", cosmeticSection.getString("name"))));
            item.setItemMeta(meta);

            String finalStatus = status;
            if (status.equalsIgnoreCase("available") || status.equalsIgnoreCase("unlocked"))
                gui.setItem(section.getIntegerList("item.slots").get(i) - 1, ClickableItem.of(item, (event) -> cosmeticBuy(event.getAction(), type, entry, finalStatus)));
            else
                gui.setItem(section.getIntegerList("item.slots").get(i) - 1, ClickableItem.empty(item));

            i++;
        }

        gui.open(this.player);
    }

    public void reloadNormalMenu(String type, String name) {
        ConfigurationSection section = Variables.menus.getConfigurationSection(type);
        HInventory gui = InventoryAPI.getInventory(this.player);
        if (gui == null) {
            openNormalMenu(type);
            return;
        }

        new Items(this.player, null).otherItems(section.getConfigurationSection("other"), gui);
        ItemStack item = new Items(this.player, null).createItem(section.getConfigurationSection("items." + name), null);
        gui.setItem(section.getInt("items." + name + ".slot") - 1, ClickableItem.of(item, (event) -> mapSetup(event.getAction(), type, name)));
    }

    public void openNormalMenu(String type) {
        ConfigurationSection section = Variables.menus.getConfigurationSection(type);
        HInventory gui = InventoryAPI.getInventoryManager()
                .setTitle(Utils.colorize(section.getString("title")))
                .setSize(section.getInt("size") > 6 ? section.getInt("size") / 9 : section.getInt("size"))
                .setId(type)
                .create();

        ArenaHandler arena = DeluxeBedwars.getInstance().getGameManager().getArena(this.player);

        new Items(this.player, null).otherItems(section.getConfigurationSection("other"), gui);
        for (String entry : section.getConfigurationSection("items").getKeys(false)) {
            ItemStack item = new Items(this.player, arena).createItem(section.getConfigurationSection("items." + entry), null);
            if (item == null)
                continue;
            gui.setItem(section.getInt("items." + entry + ".slot") - 1, ClickableItem.of(item, (event) -> mapSetup(event.getAction(), type, entry)));
        }

        gui.open(this.player);
    }

    public void mapSetup(InventoryAction action, String type, String entry) {
        if (type.equalsIgnoreCase("cosmetics")) {
            openCosmeticListMenu(entry);
            return;
        }

        if (type.equalsIgnoreCase("arena_setup")) {
            ArenaSetupHandler setup = DeluxeBedwars.getInstance().getGameManager().getSetup(this.player, "");
            if (setup == null)
                return;

            switch (entry.toLowerCase()) {
                case "team":
                    new Menus(player).openNormalMenu("team_setup");
                    return;
                case "save":
                    List<String> result;
                    try {
                        result = setup.save();
                    } catch (NullPointerException x) {
                        Utils.sendMessage(player, "setup.save_error");
                        return;
                    }
                    if (result != null) return;

                    player.sendMessage(Utils.colorize(Utils.sendMessage(null, "setup.saved").replace("%name%", setup.getName())));
                    player.closeInventory();
                    return;
                case "diamond_generator":
                case "emerald_generator":
                    if (entry.equalsIgnoreCase("emerald_generator") && setup.getEmeraldGenerators().contains(this.player.getLocation()))
                        return;
                    if (entry.equalsIgnoreCase("diamond_generator") && setup.getDiamondGenerators().contains(this.player.getLocation()))
                        return;
                    setup.addGenerator(player.getLocation(), entry.replace("_generator", ""));
                    break;
                case "type":
                case "mode":
                    List<String> types = new ArrayList<>(Variables.messages.getConfigurationSection("types").getKeys(false));
                    String nextType = setup.getType() != null && (types.size() - 1 > types.indexOf(setup.getType())) ? types.get(types.indexOf(setup.getType()) + 1) : types.get(0);

                    List<String> modes = new ArrayList<>(Variables.messages.getConfigurationSection("modes").getKeys(false));
                    String nextMode = setup.getMode() != null && (modes.size() - 1 > modes.indexOf(setup.getMode())) ? modes.get(modes.indexOf(setup.getMode()) + 1) : modes.get(0);

                    if (entry.equalsIgnoreCase("type"))
                        setup.setType(nextType);
                    else
                        setup.setMode(nextMode);
                    break;
                case "waiting_location":
                case "spectate_location":
                case "min_location":
                case "max_location":
                    setup.setLocation(player.getLocation(), entry.replace("_location", ""));
                    break;
                case "min_players":
                case "max_players":
                    int currentCount = entry.equalsIgnoreCase("max_players") ? setup.getMaxPlayers() : setup.getMinPlayers();
                    int newCount = action == InventoryAction.PICKUP_HALF ? currentCount - 1 : currentCount + 1;

                    if (newCount < 0)
                        return;
                    setup.setPlayers(newCount, entry.equalsIgnoreCase("max_players") ? "max" : "min");
                    break;
            }

            new Menus(player).reloadNormalMenu("arena_setup", entry);
            new Menus(player).reloadNormalMenu("arena_setup", "save");
        }
        if (type.equalsIgnoreCase("team_setup")) {
            ArenaSetupHandler arenaSetup = DeluxeBedwars.getInstance().getGameManager().getSetup(player, "");
            if (arenaSetup == null)
                return;

            TeamSetupHandler setup = arenaSetup.getTeam();
            if (setup == null)
                return;

            switch (entry.toLowerCase()) {
                case "save":
                    List<String> result;
                    try {
                        result = setup.save();
                    } catch (NullPointerException x) {
                        Utils.sendMessage(player, "setup.save_error");
                        return;
                    }
                    if (result != null) return;

                    player.sendMessage(Utils.colorize(Utils.sendMessage(null, "setup.saved").replace("%name%", setup.getName())));
                    player.closeInventory();
                    return;
                case "bed_location":
                case "spawn_location":
                case "shop_location":
                case "upgrade_location":
                    setup.setLocation(player.getLocation(), entry.replace("_location", ""));
                    break;
                case "generator":
                    setup.setGenerator(player.getLocation());
                    break;
                case "min_protection":
                case "max_protection":
                    setup.setProtection(player.getLocation(), entry.replace("_protection", ""));
                    break;
                case "max_players":
                    int currentCount = setup.getMaxPlayers();
                    int newCount = action == InventoryAction.PICKUP_HALF ? currentCount - 1 : currentCount + 1;

                    if (newCount < 0)
                        return;
                    setup.setMaxPlayers(newCount);
                    break;
            }

            reloadNormalMenu("team_setup", entry);
            reloadNormalMenu("team_setup", "save");
        }
    }

    public void openTrapsMenu() {
        ConfigurationSection section = Variables.menus.getConfigurationSection("traps");
        HInventory gui = InventoryAPI.getInventoryManager()
                .setTitle(Utils.colorize(section.getString("title")))
                .setSize(section.getInt("size") > 6 ? section.getInt("size") / 9 : section.getInt("size"))
                .setId("traps")
                .create();

        TeamHandler team = DeluxeBedwars.getInstance().getGameManager().getTeam(this.player);
        if (team == null) return;

        ArenaHandler arena = DeluxeBedwars.getInstance().getGameManager().getArena(this.player);
        Items items = new Items(this.player, arena);
        items.otherItems(section.getConfigurationSection("other"), gui);

        for (String entry : Variables.items.getConfigurationSection("traps").getKeys(false)) {
            ConfigurationSection trapSection = Variables.items.getConfigurationSection("traps." + entry);

            ItemStack item = new Items(this.player, arena).createItem(trapSection, null);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Utils.colorize(section.getString("item.name")
                    .replace("%trap_name%", trapSection.getString("name"))));

            String price = Variables.menus.getString("upgrades.traps.prices.normal." + (team.getUpgrades().getTraps().size() + 1));
            if (price == null)
                price = Variables.menus.getString("upgrades.traps.prices.normal." + team.getUpgrades().getTraps().size());

            String[] itemCounts = Utils.hasItem(this.player, price);
            String status = section.getString("status.available");
            if (Variables.menus.getString("upgrades.traps.prices.normal." + (team.getUpgrades().getTraps().size() + 1)) == null)
                status = section.getString("status.maximum");
            else if (Integer.parseInt(itemCounts[0]) < Integer.parseInt(itemCounts[1]))
                status = section.getString("status.not_available");

            List<String> lore = new ArrayList<>();
            for (String line : section.getStringList("item.lore")) {
                if (line.equalsIgnoreCase("%trap_description%")) {
                    for (String line2 : trapSection.getStringList("description"))
                        lore.add(Utils.colorize(line2));

                    continue;
                }

                lore.add(Utils.colorize(line
                        .replace("%status%", status)
                        .replace("%price%", itemCounts[1])
                        .replace("%required%", Utils.format(Integer.parseInt(itemCounts[1]) - Integer.parseInt(itemCounts[0])))
                        .replace("%item%", Variables.config.getString("messages.materials." + itemCounts[2] + ".name"))
                        .replace("%color%", Variables.config.getString("messages.materials." + itemCounts[2] + ".color"))));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            String finalPrice = price;
            gui.setItem(section.getInt("item.slots." + entry) - 1, ClickableItem.of(item, (event) -> {
                if (team.getUpgrades().getTraps().size() >= Variables.menus.getInt("upgrades.traps.maximum_trap")) {
                    player.sendMessage("your team already have maximum trap size");
                    return;
                }

                String[] count = Utils.hasItem(player, finalPrice);
                if (Integer.parseInt(count[0]) < Integer.parseInt(count[1])) {
                    player.sendMessage("you need more " + (Integer.parseInt(count[1]) - Integer.parseInt(count[0])) + " item.");
                    return;
                }

                new PlayerManager(this.player).removeItem(count[2], Integer.parseInt(count[1]));
                team.getUpgrades().addTrap(entry);
                team.send(player.getName() + " bought " + entry);
                new Menus(player).openTrapsMenu();
            }));
        }

        gui.open(this.player);
    }

    public void upgradeBuy(TeamHandler team, String entry) {
        PlayerManager playerManager = new PlayerManager(this.player);

        switch (entry.toLowerCase()) {
            case "heal":
            case "dragon":
                Boolean opened = false;
                if (entry.equalsIgnoreCase("heal")) opened = team.getUpgrades().getHealPool();
                if (entry.equalsIgnoreCase("dragon")) opened = team.getUpgrades().getDragonBuff();
                if (opened)
                    player.sendMessage("Your team already have this upgrade!");
                else {
                    String[] count = Utils.hasItem(player, Variables.items.getString("upgrades." + entry + ".prices.normal"));
                    if (Integer.parseInt(count[0]) < Integer.parseInt(count[1])) {
                        player.sendMessage("you need more " + (Integer.parseInt(count[1]) - Integer.parseInt(count[0])) + " item.");
                        return;
                    }

                    playerManager.removeItem(count[2], Integer.parseInt(count[1]));
                    team.send(player.getName() + " bought upgrade");

                    if (entry.equalsIgnoreCase("heal")) team.getUpgrades().setHealPool(true);
                    if (entry.equalsIgnoreCase("dragon")) team.getUpgrades().setDragonBuff(true);

                    new Menus(player).openUpgradesMenu();
                }
                break;

            case "sharpness":
            case "haste":
            case "protection":
            case "forge":
                int level = 0;
                if (entry.equalsIgnoreCase("sharpness")) level = team.getUpgrades().getSharpnessLevel();
                if (entry.equalsIgnoreCase("protection")) level = team.getUpgrades().getProtectionLevel();
                if (entry.equalsIgnoreCase("haste")) level = team.getUpgrades().getHasteLevel();
                if (entry.equalsIgnoreCase("forge")) level = team.getUpgrades().getGeneratorLevel();
                if (level >= Variables.items.getConfigurationSection("upgrades." + entry.toLowerCase() + ".prices.normal").getKeys(false).size()) {
                    player.sendMessage("Your team already have maximum upgrade!");
                    return;
                } else {
                    String[] count = Utils.hasItem(player, Variables.items.getString("upgrades.protection.prices.normal." + (level + 1)));
                    if (Integer.parseInt(count[0]) < Integer.parseInt(count[1])) {
                        player.sendMessage("you need more " + (Integer.parseInt(count[1]) - Integer.parseInt(count[0])) + " item.");
                        return;
                    }

                    playerManager.removeItem(count[2], Integer.parseInt(count[1]));
                    team.send(player.getName() + " bought upgrade");

                    if (entry.equalsIgnoreCase("sharpness")) {
                        team.getUpgrades().setSharpnessLevel(level + 1);
                        for (PlayerHandler playerHandler : team.getPlayers())
                            new PlayerManager(playerHandler.getPlayer()).checkSword();
                    }
                    if (entry.equalsIgnoreCase("protection")) {
                        team.getUpgrades().setProtectionLevel(level + 1);
                        for (PlayerHandler playerHandler : team.getPlayers())
                            new PlayerManager(playerHandler.getPlayer()).checkArmor(false);
                    }
                    if (entry.equalsIgnoreCase("haste")) {
                        team.getUpgrades().setHasteLevel(level + 1);

                        for (PlayerHandler playerHandler : team.getPlayers()) {
                            playerHandler.getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);
                            playerHandler.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 999999, level), true);
                        }
                    }
                    if (entry.equalsIgnoreCase("forge")) {
                        team.getUpgrades().setGeneratorLevel(level + 1);
                        new GeneratorHandler(DeluxeBedwars.getInstance().getGameManager().getArena(player)).islandGenerator(team);
                    }

                    openUpgradesMenu();
                }
                break;
        }
    }

    public void openUpgradesMenu() {
        ConfigurationSection section = Variables.menus.getConfigurationSection("upgrades");
        HInventory gui = InventoryAPI.getInventoryManager()
                .setTitle(Utils.colorize(section.getString("title")))
                .setSize(section.getInt("size") > 6 ? section.getInt("size") / 9 : section.getInt("size"))
                .setId("upgrades")
                .create();

        TeamHandler team = DeluxeBedwars.getInstance().getGameManager().getTeam(this.player);
        if (team == null) return;

        ArenaHandler arena = DeluxeBedwars.getInstance().getGameManager().getArena(this.player);
        Items items = new Items(this.player, arena);
        items.otherItems(section.getConfigurationSection("other"), gui);

        ItemStack trap = items.createItem(section.getConfigurationSection("trap"), null);
        gui.setItem(section.getInt("trap.slot") - 1, ClickableItem.of(trap, (event) -> new Menus(player).openTrapsMenu()));

        for (int i = 0; i < section.getInt("traps.maximum_trap"); i++) {
            ItemStack traps;
            if (team.getUpgrades().getTraps().size() >= i + 1 && team.getUpgrades().getTraps().get(i) != null)
                traps = items.createItem(Variables.items.getConfigurationSection("traps." + team.getUpgrades().getTraps().get(i)), null);
            else traps = items.createItem(section.getConfigurationSection("traps"), null);
            ItemMeta trapsMeta = traps.getItemMeta();

            traps.setAmount(i + 1);
            trapsMeta.setDisplayName(Utils.colorize(section.getString("traps.name")
                    .replace("%trap_name%", team.getUpgrades().getTraps().size() >= i + 1 && team.getUpgrades().getTraps().get(i) != null ? Variables.items.getString("traps." + team.getUpgrades().getTraps().get(i) + ".name") : section.getString("traps.no_trap"))
                    .replace("%number%", String.valueOf(i + 1))));

            List<String> lore = new ArrayList<>();
            String[] itemCounts = Utils.hasItem(this.player, section.getString("traps.prices.normal." + (team.getUpgrades().getTraps().size() + 1)) != null ? section.getString("traps.prices.normal." + (team.getUpgrades().getTraps().size() + 1)) : section.getString("traps.prices.normal." + team.getUpgrades().getTraps().size()));
            for (String line : section.getStringList("traps.lore")) {
                lore.add(Utils.colorize(line
                        .replace("%price%", itemCounts[1])
                        .replace("%required%", String.valueOf(Integer.parseInt(itemCounts[1]) - Integer.parseInt(itemCounts[0])))
                        .replace("%item%", Variables.config.getString("messages.materials." + itemCounts[2] + ".name"))
                        .replace("%color%", Variables.config.getString("messages.materials." + itemCounts[2] + ".color"))));
            }
            trapsMeta.setLore(lore);
            traps.setItemMeta(trapsMeta);

            gui.setItem(section.getIntegerList("traps.slots").get(i) - 1, ClickableItem.empty(traps));
        }

        for (String entry : section.getKeys(false)) {
            if (entry.equalsIgnoreCase("status")
                    || entry.equalsIgnoreCase("title")
                    || entry.equalsIgnoreCase("size")) continue;

            if (!Variables.items.getBoolean("upgrades." + entry + ".enabled")) continue;

            if (entry.equalsIgnoreCase("dragon")
                    || entry.equalsIgnoreCase("heal")) {
                Boolean upgrade = false;
                if (entry.equalsIgnoreCase("dragon")) upgrade = team.getUpgrades().getDragonBuff();
                else if (entry.equalsIgnoreCase("heal")) upgrade = team.getUpgrades().getHealPool();

                ItemStack item = items.createItem(section.getConfigurationSection(entry), null);
                ItemMeta meta = item.getItemMeta();

                String[] itemCounts = Utils.hasItem(this.player, Variables.items.getString("upgrades." + entry + ".prices.normal"));
                List<String> lore = new ArrayList<>();
                String status = section.getString("status.available");
                if (upgrade) {
                    status = section.getString("status.bought");
                    if (section.getBoolean(entry + ".glow")) item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
                } else if (Integer.parseInt(itemCounts[0]) < Integer.parseInt(itemCounts[1]))
                    status = section.getString("status.not_available");

                for (String line : section.getStringList(entry + ".description")) {
                    String levelColor = upgrade ? section.getString("level_color.bought") : section.getString("level_color.not_bought");

                    lore.add(Utils.colorize(line
                            .replace("%level_color%", levelColor)
                            .replace("%status%", status)
                            .replace("%price%", itemCounts[1])
                            .replace("%required%", String.valueOf(Integer.parseInt(itemCounts[1]) - Integer.parseInt(itemCounts[0])))
                            .replace("%item%", Variables.config.getString("messages.materials." + itemCounts[2] + ".name"))
                            .replace("%color%", Variables.config.getString("messages.materials." + itemCounts[2] + ".color"))));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);

                gui.setItem(section.getInt(entry + ".slot") - 1, ClickableItem.of(item, (event) -> upgradeBuy(team, entry)));
            }

            if (entry.equalsIgnoreCase("haste")
                    || entry.equalsIgnoreCase("protection")
                    || entry.equalsIgnoreCase("forge")
                    || entry.equalsIgnoreCase("sharpness")) {
                int level = 0;
                if (entry.equalsIgnoreCase("protection")) level = team.getUpgrades().getProtectionLevel();
                else if (entry.equalsIgnoreCase("haste")) level = team.getUpgrades().getHasteLevel();
                else if (entry.equalsIgnoreCase("sharpness")) level = team.getUpgrades().getSharpnessLevel();
                else if (entry.equalsIgnoreCase("forge")) level = team.getUpgrades().getGeneratorLevel();

                ItemStack item = items.createItem(section.getConfigurationSection(entry), null);
                ItemMeta meta = item.getItemMeta();
                if (level > 0 && section.getBoolean(entry + ".glow")) item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                Set<String> levels = Variables.items.getConfigurationSection("upgrades." + entry + ".prices.normal").getKeys(false);
                List<String> lore = new ArrayList<>();

                String itemSection = Variables.items.getString("upgrades." + entry + ".prices.normal." + (level + 1));
                if (itemSection == null)
                    itemSection = Variables.items.getString("upgrades." + entry + ".prices.normal." + level);

                String[] itemCounts = Utils.hasItem(this.player, itemSection);
                String status = section.getString("status.available");
                if (level >= Variables.items.getConfigurationSection("upgrades." + entry + ".prices.normal").getKeys(false).size())
                    status = section.getString("status.maximum");
                else if (Integer.parseInt(itemCounts[0]) < Integer.parseInt(itemCounts[1]))
                    status = section.getString("status.not_available");

                for (String line : section.getStringList(entry + ".description")) {
                    if (line.contains("%levels%")) {
                        for (String tier : levels) {
                            String levelColor = level >= Integer.parseInt(tier) ? section.getString("level_color.bought") : section.getString("level_color.not_bought");

                            String[] entryCount = Utils.hasItem(this.player, Variables.items.getString("upgrades." + entry + ".prices.normal." + tier));
                            lore.add(Utils.colorize(section.getString(entry + ".level")
                                    .replace("%tier%", tier)
                                    .replace("%description%", Variables.items.getString("upgrades." + entry + ".descriptions." + tier))
                                    .replace("%price%", entryCount[1])
                                    .replace("%item%", Variables.config.getString("messages.materials." + entryCount[2] + ".name"))
                                    .replace("%color%", Variables.config.getString("messages.materials." + entryCount[2] + ".color"))
                                    .replace("%level_color%", levelColor)));
                        }
                        continue;
                    }

                    lore.add(Utils.colorize(line
                            .replace("%status%", status)
                            .replace("%price%", itemCounts[1])
                            .replace("%required%", String.valueOf(Integer.parseInt(itemCounts[1]) - Integer.parseInt(itemCounts[0])))
                            .replace("%item%", Variables.config.getString("messages.materials." + itemCounts[2] + ".name"))
                            .replace("%color%", Variables.config.getString("messages.materials." + itemCounts[2] + ".color"))));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);

                gui.setItem(section.getInt(entry + ".slot") - 1, ClickableItem.of(item, (event) -> upgradeBuy(team, entry)));
            }
        }

        gui.open(this.player);
    }

    public void openArenasMenu(String arenaType) {
        String typeName = !arenaType.equals("") ? Variables.messages.getString("types." + arenaType) : "";
        if (typeName == null || typeName.equals("")) {
            arenaType = "";
            typeName = Variables.messages.getString("all_arenas");
        }

        ConfigurationSection section = Variables.menus.getConfigurationSection("arenas");
        HInventory gui = InventoryAPI.getInventoryManager()
                .setTitle(Utils.colorize(section.getString("title")
                        .replace("%type%", typeName)))
                .setSize(section.getInt("size") > 6 ? section.getInt("size") / 9 : section.getInt("size"))
                .setId("arenas")
                .create();

        new Items(this.player, null).otherItems(section.getConfigurationSection("other"), gui);
        ItemStack random = new Items(this.player, null).createItem(section.getConfigurationSection("random"), null);
        if (random != null) {
            String finalArenaType = arenaType;
            gui.setItem(section.getInt("random.slot") - 1, ClickableItem.of(random, (event) -> joinArena("random", finalArenaType)));
        }

        int i = 0;
        for (ArenaHandler arena : this.plugin.getGameManager().getArenas()) {
            String type = "waiting";
            if (arena.getState() == Enums.ArenaState.PLAYING) type = "playing";
            if (arena.getState() == Enums.ArenaState.ENDED) type = "ended";
            if (arena.getState() == Enums.ArenaState.STARTING) type = "starting";
            if (!section.getStringList("arenas.show").contains(type)) continue;
            if (!arenaType.equals("") && !arena.getType().equalsIgnoreCase(arenaType)) continue;

            ItemStack item = new Items(this.player, arena).createItem(section.getConfigurationSection("arenas." + type), null);
            gui.setItem(section.getIntegerList("arenas.slots").get(i) - 1, ClickableItem.of(item, (event) -> joinArena(arena.getName(), "arenaType")));
            i++;
        }

        gui.open(this.player);
    }

    public void joinArena(String arenaName, String type) {
        if (arenaName.equalsIgnoreCase("random")) {
            HashMap<Integer, Integer> arenas = new HashMap<>();

            for (int i = 0; i < this.plugin.getGameManager().getArenas().size(); i++) {
                ArenaHandler arenaHandler = this.plugin.getGameManager().getArenas().get(i);
                if (arenaHandler == null) continue;
                if (arenaHandler.getPlayers().size() >= arenaHandler.getMaxPlayers()) continue;
                if (arenaHandler.getState() != Enums.ArenaState.WAITING && arenaHandler.getState() != Enums.ArenaState.STARTING) continue;
                if (!type.equals("") && !arenaHandler.getType().equalsIgnoreCase(type)) continue;

                arenas.put(i, arenaHandler.getPlayers().size());
            }

            ArrayList<Integer> newArenas = new ArrayList<>(arenas.keySet());
            Collections.sort(newArenas);

            if (newArenas.size() <= 0) {
                Utils.sendMessage(player, "cant_found_arena");
                return;
            }

            ArenaHandler newArena = this.plugin.getGameManager().getArenas().get(newArenas.get(0));
            newArena.join(player);
        }

        ArenaHandler arenaHandler = DeluxeBedwars.getInstance().getGameManager().getArenaByName(arenaName);
        if (DeluxeBedwars.getInstance().getGameManager().getArena(player) == null && arenaHandler != null) {
            if (arenaHandler.getState() == Enums.ArenaState.WAITING || arenaHandler.getState() == Enums.ArenaState.STARTING)
                arenaHandler.join(player);
        }
    }

    public void openShopMenu(String type) {
        Variables.menuID.put(this.player.getUniqueId(), type);
        ConfigurationSection menuSection = Variables.menus.getConfigurationSection("shop");
        ConfigurationSection section = Variables.items.getConfigurationSection("categories");

        String categoryName = ChatColor.stripColor(Utils.colorize(section.getString(type + ".name")));
        HInventory gui = InventoryAPI.getInventoryManager()
                .setTitle(Utils.colorize(menuSection.getString("title")
                        .replace("%category%", categoryName)))
                .setSize(menuSection.getInt("size") > 6 ? menuSection.getInt("size") / 9 : menuSection.getInt("size"))
                .setId("shop")
                .create();

        ConfigurationSection glassSection = menuSection.getConfigurationSection("glasses");
        new Items(this.player, null).createItem(glassSection.getConfigurationSection("notSelected"), gui);

        ItemStack selectedGlass = new Items(this.player, null).createItem(glassSection.getConfigurationSection("selected"), null);
        if (selectedGlass != null) gui.setItem(glassSection.getInt("selected.slots." + type) - 1, ClickableItem.empty(selectedGlass));

        for (String entry : section.getKeys(false)) {
            if (entry.contains("lore")) continue;
            ConfigurationSection entrySection = section.getConfigurationSection(entry);
            if (entrySection == null) continue;

            ItemStack item = new Items(this.player, null).createItem(entrySection, null);
            if (item == null) continue;

            gui.setItem(entrySection.getInt("slot") - 1, ClickableItem.of(item, (event) -> new Menus(player).openShopMenu(entry)));
        }

        for (String item : section.getStringList(type + ".items")) {
            String[] args = item.split("[:]", 2);

            PlayerHandler playerHandler = DeluxeBedwars.getInstance().getGameManager().getPlayer(this.player);

            if (args[0].equalsIgnoreCase("pickaxe") || args[0].equalsIgnoreCase("axe")) {
                if (playerHandler == null) continue;

                if (args[0].equalsIgnoreCase("pickaxe")) {
                    args[0] = Variables.items.getString("ranking.pickaxe." + (playerHandler.getPickaxe() - 1)) != null ? Variables.items.getString("ranking.pickaxe." + (playerHandler.getPickaxe() - 1)) : Variables.items.getString("ranking.pickaxe." + (playerHandler.getPickaxe()));
                } else if (args[0].equalsIgnoreCase("axe")) {
                    args[0] = Variables.items.getString("ranking.axe." + (playerHandler.getAxe() - 1)) != null ? Variables.items.getString("ranking.axe." + (playerHandler.getAxe() - 1)) : Variables.items.getString("ranking.axe." + (playerHandler.getAxe()));
                }
            }

            ItemStack builder = new Items(this.player, null).createItem(Variables.items.getConfigurationSection("items." + args[0]), null);

            String[] itemCounts = Utils.hasItem(this.player, Variables.items.getString("items." + args[0] + ".prices.normal"));
            List<String> lores = Integer.parseInt(itemCounts[0]) < Integer.parseInt(itemCounts[1]) ? Variables.menus.getStringList("shop.itemLores.notBuyable") : Variables.menus.getStringList("shop.itemLores.buyable");

            List<String> newLore = new ArrayList<>();
            for (String line : lores) {
                if (line.contains("%description%") && Variables.items.getStringList("items." + args[0] + ".description") != null)
                    for (String line2 : Variables.items.getStringList("items." + args[0] + ".description"))
                        newLore.add(Utils.colorize(line2));
                else newLore.add(Utils.colorize(line
                        .replace("%price%", itemCounts[1])
                        .replace("%required%", String.valueOf(Integer.parseInt(itemCounts[1]) - Integer.parseInt(itemCounts[0])))
                        .replace("%item%", Variables.config.getString("messages.materials." + itemCounts[2] + ".name"))
                        .replace("%color%", Variables.config.getString("messages.materials." + itemCounts[2] + ".color"))));
            }

            ItemMeta meta = builder.getItemMeta();
            meta.setLore(newLore);
            builder.setItemMeta(meta);

            NBTItem nbti = new NBTItem(builder);
            nbti.setString("BedwarsITEM", args[0]);
            gui.setItem(Integer.parseInt(args[1]) - 1, ClickableItem.of(builder, (event) -> new PlayerManager(this.player).buyEvent(args[0])));
        }

        gui.open(this.player);
    }
}