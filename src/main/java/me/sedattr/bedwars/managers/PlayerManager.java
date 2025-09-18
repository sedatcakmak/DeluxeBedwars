package me.sedattr.bedwars.managers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.*;
import me.sedattr.bedwars.helpers.ItemBuilder;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.nbtapi.NBTItem;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayerManager {
    Player player;
    TeamHandler team;

    public PlayerManager(Player player) {
        GameManager handler = DeluxeBedwars.getInstance().getGameManager();
        this.team = handler.getTeam(player);
        this.player = player;

        handler.getPlayerTeams().put(player, team);
    }

    public ItemStack getDefaultItem() {
        ItemStack sword = new ItemStack(Material.WOOD_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.spigot().setUnbreakable(true);
        sword.setItemMeta(meta);

        Integer level = this.team.getUpgrades().getSharpnessLevel();
        if (level > 0) sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);

        return sword;
    }

    public void spawn(Boolean first) {
        resetPlayer(false);

        PlayerHandler playerHandler = DeluxeBedwars.getInstance().getGameManager().getPlayer(this.player);
        if (this.team != null && this.team.getUpgrades().getHasteLevel() > 0) {
            this.player.removePotionEffect(PotionEffectType.FAST_DIGGING);

            this.player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 999999, this.team.getUpgrades().getHasteLevel()-1), true);
        }

        this.player.getInventory().setItem(0, getDefaultItem());
        if (playerHandler != null) {
            checkArmor(true);
            checkPickaxe(true);
            checkAxe(true);
            checkShears();
        }

        if (first) {
            String kit = new DataHandler(player).getPlayerSelectedCosmetic("kits");
            if (kit != null && !kit.equals("")) {
                List<String> items = Variables.cosmetics.getStringList("kits." + kit + ".items");
                if (items != null && items.size() > 0) {
                    for (String name : items) {
                        String[] args = name.split("[:]", 3);
                        if (args.length < 3) continue;

                        Material mat = Material.getMaterial(args[0]);
                        if (mat == null) continue;

                        ItemStack item = new ItemStack(mat, Integer.parseInt(args[2]), Byte.parseByte(args[1]));
                        this.player.getInventory().addItem(item);
                    }
                    this.player.updateInventory();
                }

                List<String> potions = Variables.cosmetics.getStringList("kits." + kit + ".potions");
                if (potions != null && potions.size() > 0) for (String name : potions) {
                    String[] args = name.split("[:]", 3);
                    if (args.length < 3) continue;

                    PotionEffectType type = PotionEffectType.getByName(args[0]);
                    if (type == null) continue;

                    this.player.addPotionEffect(new PotionEffect(type, Integer.parseInt(args[2])*20, Integer.parseInt(args[1])));
                }
            }
        }

        this.player.teleport(this.team.getSpawnLocation());
    }

    public void removeItems(Player player, Integer requiredItem, List<ItemStack> items) {
        int itemCount = 0;
        boolean enabled = false;
        for (ItemStack is : items) {
            if (enabled) continue;

            if (itemCount + is.getAmount() <= requiredItem) {
                player.getInventory().removeItem(is);
                itemCount += is.getAmount();
            } else if (itemCount + is.getAmount() > requiredItem) {
                int newAmount = is.getAmount() - (requiredItem - itemCount);

                is.setAmount(newAmount);
                player.updateInventory();

                enabled = true;
            }
        }
    }

    public void buyEvent(String name) {
        ConfigurationSection section = Variables.items.getConfigurationSection("items." + name);

        Material material = Material.getMaterial(section.getString("material"));
        if (material == null) return;

        String[] requiredItem = section.getString("prices.normal").split("[:]", 2);

        int count = 0;
        ItemStack woodSword = null;
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().equals(Material.AIR)) continue;

            if (item.getType().name().equals(requiredItem[0])) {
                count+=item.getAmount();
                items.add(item);
            }

            if (item.getType().name().contains("WOOD_SWORD") || item.getType().name().contains("WOODEN_SWORD"))
                woodSword = item;
        }

        if (count < Integer.parseInt(requiredItem[1])) return;

        ItemStack item = new Items(player, null).createItem(section, null);
        ItemMeta meta = item.getItemMeta();
        boolean bought = false;

        if (material.name().contains("SHEAR") || material.name().contains("SWORD") || material.name().contains("BOW") || material.name().contains("PICKAXE") || material.name().contains("AXE")) {
            meta.spigot().setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        if (material.name().contains("SWORD")) {
            Integer level = DeluxeBedwars.getInstance().getGameManager().getTeam(player).getUpgrades().getSharpnessLevel();
            if (level > 0)
                item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, level);

            if (woodSword != null) player.getInventory().removeItem(woodSword);
        }

        if (material.name().contains("SHEAR")) {
            PlayerHandler playerHandler = DeluxeBedwars.getInstance().getGameManager().getPlayer(player);

            if (Variables.items.getBoolean("settings.save.shears.enabled") && playerHandler.getShear()) {
                player.sendMessage("You have shear already!");
                return;
            }

            removeItems(player, Integer.parseInt(requiredItem[1]), items);
            bought = true;
            playerHandler.setShear(true);
        }

        if (material.name().contains("WOOL") || material.name().contains("CLAY") || material.name().contains("GLASS")) {
            TeamHandler team = DeluxeBedwars.getInstance().getGameManager().getTeam(player);
            if (team!=null) item.setDurability(Variables.blockDatas.get(DeluxeBedwars.getInstance().getGameManager().getTeam(player).getName().toLowerCase()).shortValue());
        }

        if (!material.name().contains("SWORD") && (material.name().equals("WOOD") || material.name().equals("LOG") || material.name().equals("LOG_2"))) {
            String wood = new DataHandler(player).getPlayerSelectedCosmetic("wood_skins");
            if (wood == null) {
                if (Variables.cosmetics.getConfigurationSection("wood_skins.default") == null) return;
                else wood = "default";
            }

            short data = (short) Variables.cosmetics.getInt("wood_skins." + wood + ".data");
            Material mat = Material.getMaterial(Variables.cosmetics.getString("wood_skins." + wood + ".material"));
            if (mat == null) return;

            item.setDurability(data);
            item.setType(mat);

            if (!bought) {
                removeItems(player, Integer.parseInt(requiredItem[1]), items);
                bought = true;
            }
        }

        if (section.getString("type") != null) {
            if (section.getString("type").equalsIgnoreCase("potion")) {
                NBTItem nbti = new NBTItem(item);
                nbti.setString("BedwarsPOTION", section.getName());

                bought = true;
                removeItems(player, Integer.parseInt(requiredItem[1]), items);
                player.getInventory().addItem(nbti.getItem());
            }

            if (section.getString("type").equalsIgnoreCase("bridge") && !bought) {
                NBTItem nbti = new NBTItem(item);
                nbti.setString("BedwarsBRIDGEEGG", player.getName());

                bought = true;
                removeItems(player, Integer.parseInt(requiredItem[1]), items);
                player.getInventory().addItem(nbti.getItem());
            }

            if (section.getString("type").equalsIgnoreCase("spawnegg") && !bought) {
                NBTItem nbti = new NBTItem(item);
                nbti.setString("BedwarsENTITYEGG", section.getName());

                bought = true;
                removeItems(player, Integer.parseInt(requiredItem[1]), items);
                player.getInventory().addItem(nbti.getItem());
            }

            if (section.getString("type").equalsIgnoreCase("spawnthrowable") && !bought) {
                NBTItem nbti = new NBTItem(item);
                nbti.setString("BedwarsENTITYTHROWABLE", section.getName());

                bought = true;
                removeItems(player, Integer.parseInt(requiredItem[1]), items);
                player.getInventory().addItem(nbti.getItem());
            }

            PlayerHandler playerHandler = DeluxeBedwars.getInstance().getGameManager().getPlayer(player);
            if (section.getString("type").equalsIgnoreCase("armor")) {
                ConfigurationSection armors = Variables.items.getConfigurationSection("ranking.armor");
                if (playerHandler != null)
                    if (armors.getInt(section.getName()) >= armors.getInt(playerHandler.getArmor())) {
                        player.sendMessage("This armor is trash than your armor!");
                        return;
                    } else {
                        playerHandler.setArmor(section.getName());
                        new PlayerManager(player).checkArmor(true);

                        if (!bought) {
                            removeItems(player, Integer.parseInt(requiredItem[1]), items);
                            bought = true;
                        }
                    }
            }

            if (section.getString("type").equalsIgnoreCase("pickaxe") || section.getString("type").equalsIgnoreCase("axe")) {
                if (playerHandler != null) {
                    int newNumber = section.getString("type").equalsIgnoreCase("pickaxe") ? playerHandler.getPickaxe()-1 : playerHandler.getAxe()-1;

                    if (Variables.items.getString("ranking." + section.getString("type").toLowerCase() + "." + newNumber) != null) {
                        if (!bought)
                            removeItems(player, Integer.parseInt(requiredItem[1]), items);

                        if (section.getString("type").equalsIgnoreCase("pickaxe")) {
                            playerHandler.setPickaxe(newNumber);
                            new PlayerManager(player).checkPickaxe(false);
                        } else {
                            playerHandler.setAxe(newNumber);
                            new PlayerManager(player).checkAxe(false);
                        }
                    } else {
                        player.sendMessage("You have maximum item already!");
                        return;
                    }
                }
            }
        } else {
            if (!bought)
                removeItems(player, Integer.parseInt(requiredItem[1]), items);

            player.getInventory().addItem(item);
        }

        if (Variables.menuID.containsKey(player.getUniqueId())) new Menus(player).openShopMenu(Variables.menuID.get(player.getUniqueId()));
    }

    public void removeItem(String item, Integer count) {
        int itemCount = 0;
        boolean enabled = false;
        for (ItemStack is : player.getInventory()) {
            if (is == null || is.getType() == Material.AIR) continue;
            if (enabled) return;
            if (!is.getType().name().equalsIgnoreCase(item)) continue;

            if (itemCount + is.getAmount() <= count) {
                player.getInventory().removeItem(is);
                itemCount += is.getAmount();
            } else if (itemCount + is.getAmount() > count) {
                int newAmount = is.getAmount() - (count - itemCount);

                is.setAmount(newAmount);
                player.updateInventory();
                enabled = true;
            }
        }
    }

    public void hideShow(String type) {
        if (!Variables.config.getString("lobby.location.world").equalsIgnoreCase(this.player.getWorld().getName()))
            return;

        List<Player> players = this.player.getWorld().getPlayers();
        if (players == null)
            return;
        if (players.isEmpty())
            return;

        players.remove(this.player);
        switch (type.toLowerCase()) {
            case "hide":
            case "hide_item":
                for (Player p : players)
                    this.player.hidePlayer(p);

                return;
            case "show":
            case "show_item":
                for (Player p : players)
                    this.player.showPlayer(p);
        }
    }

    public void giveLobbyItems() {
        if (!DeluxeBedwars.getInstance().isEnabled())
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!Variables.config.getString("lobby.location.world").equalsIgnoreCase(PlayerManager.this.player.getWorld().getName()))
                    return;

                ConfigurationSection itemList = Variables.items.getConfigurationSection("lobby_items");
                if (itemList != null && itemList.getKeys(false).size() > 0) for (String entry : itemList.getKeys(false)) {
                    ConfigurationSection section = itemList.getConfigurationSection(entry);
                    if (section.getString("type") != null && section.getString("type").equalsIgnoreCase("hide")) {
                        if (Variables.hiddenPlayers.contains(PlayerManager.this.player))
                            section = section.getConfigurationSection("show_item");
                        else
                            section = section.getConfigurationSection("hide_item");
                    }

                    ItemStack item = new Items(PlayerManager.this.player, null).createItem(section, null);
                    NBTItem nbti = new NBTItem(item);
                    nbti.setString("BedwarsINVENTORYITEM", "lobby_items."+ entry);
                    PlayerManager.this.player.getInventory().setItem(section.getInt("slot")-1, nbti.getItem());
                }
                new Scoreboards(null).lobbyScoreboard(PlayerManager.this.player);
            }
        }.runTaskLaterAsynchronously(DeluxeBedwars.getInstance(), 10);
    }

    public void resetPlayer(boolean dead) {
        Utils.removeArmors(this.player);
        this.player.setHealth(20.0D);
        this.player.setFoodLevel(20);
        this.player.setSaturation(20.0F);
        this.player.setLevel(0);
        this.player.setExp(0);
        this.player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect effect : this.player.getActivePotionEffects())
            this.player.removePotionEffect(effect.getType());

        if (dead) {
            this.player.setGameMode(GameMode.ADVENTURE);
            this.player.setAllowFlight(true);
            this.player.setFlying(true);

            List<String> approvedItems = Variables.config.getStringList("settings.approved_items");
            List<ItemStack> items = new ArrayList<>();
            HashMap<String, Integer> itemCount = new HashMap<>();

            for (ItemStack item : this.player.getInventory().getContents()) {
                if (item == null) continue;
                if (item.getType().equals(Material.AIR)) continue;

                if (approvedItems.contains(item.getType().name()))
                    items.add(item);
            }

            if (!items.isEmpty())
                for (ItemStack item : items) {
                    if (item == null || item.getType().equals(Material.AIR))
                        continue;

                    itemCount.put(item.getType().name(), itemCount.getOrDefault(item.getType().name(), 0) + item.getAmount());
                }

            PlayerHandler handler = DeluxeBedwars.getInstance().getGameManager().getPlayer(this.player);
            Player give = null;
            if (handler != null && handler.getLastDamager() != null) {
                if (handler.getLastDamager() instanceof Player) give = (Player) handler.getLastDamager();
                else if (handler.getLastDamager().hasMetadata("BedwarsOWNER")) give = (Player) handler.getLastDamager().getMetadata("BedwarsOWNER").get(0).value();
            }

            if (!items.isEmpty())
                for (ItemStack item : items) {
                    if (give != null) {
                        give.getInventory().addItem(item);
                        continue;
                    }

                    Item drop = this.player.getLocation().getWorld().dropItem(this.player.getLocation(), item);
                    drop.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                }

            if (!itemCount.isEmpty())
                for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
                    ConfigurationSection section = Variables.messages.getConfigurationSection("materials." + entry.getKey());
                    if (section != null && give != null) {
                        give.sendMessage(Utils.colorize(Utils.sendMessage(null, "dropped_item")
                                .replace("%color%", section.getString("color"))
                                .replace("%name%", section.getString("name"))
                                .replace("%count%", String.valueOf(entry.getValue()))));
                    }
                }
        }

        this.player.getInventory().clear();
    }

    public void deadEvent(ArenaHandler arena, String fell) {
        DeluxeBedwars.getInstance().getGameManager().getPlayer(this.player).killMessage(fell);
        resetPlayer(true);

        TeamHandler team = DeluxeBedwars.getInstance().getGameManager().getPlayerTeams().get(this.player);
        if (team == null) return;
        this.player.teleport(arena.getSpectateLocation());

        arena.addHiddenPlayer(this.player);
        this.player.hidePlayer(this.player);
        for (PlayerHandler playerHandler : arena.getPlayers())
            playerHandler.getPlayer().hidePlayer(this.player);

        if (team.getBedStatus()) {
            new Countdown.DeathCountdown(this.player).runTaskTimer(DeluxeBedwars.getInstance(), 0, 20);
            return;
        }

        this.player.sendMessage(Utils.colorize("&cYou are eliminated!"));
        for (ItemStack ecItem : this.player.getEnderChest().getContents()) {
            if (ecItem == null || ecItem.getType() == Material.AIR) continue;

            Item item = this.player.getWorld().dropItem(team.getGeneratorLocation(), ecItem);
            item.setVelocity(new Vector(0, 0, 0));
            item.setMetadata("BedwarsTEAM", new FixedMetadataValue(DeluxeBedwars.getInstance(), arena.getName()));
        }
        this.player.getEnderChest().clear();
        this.player.setAllowFlight(true);
        this.player.setFlying(true);

        PlayerHandler playerHandler = arena.getPlayer(this.player);
        playerHandler.setAlive(false);
        arena.checkWinner();
    }

    public void checkPickaxe(Boolean death) {
        PlayerHandler playerHandler = DeluxeBedwars.getInstance().getGameManager().getPlayer(this.player);

        if (death) {
            if (Variables.items.getBoolean("settings.save.pickaxe.enabled")) {
                int newNumber = playerHandler.getPickaxe() + Variables.items.getInt("settings.save.pickaxe.remove");

                if (newNumber > 0 && newNumber < Variables.items.getInt("ranking.pickaxe.default"))
                    playerHandler.setPickaxe(newNumber);
            } else {
                playerHandler.setPickaxe(Variables.items.getInt("ranking.pickaxe.default"));
            }
        }

        for (ItemStack item : this.player.getInventory().getContents()) {
            if (item == null || item.getType().equals(Material.AIR)) continue;

            String itemType = item.getType().name();
            if (itemType.contains("_PICKAXE"))
                this.player.getInventory().removeItem(item);
        }

        if (playerHandler.getPickaxe() > 0 && playerHandler.getPickaxe() != Variables.items.getInt("ranking.pickaxe.default")) {
            ConfigurationSection section = Variables.items.getConfigurationSection("items." + Variables.items.getString("ranking.pickaxe." + playerHandler.getPickaxe()));
            if (section != null) {
                ItemStack newItem = new Items(player, playerHandler.getArena()).createItem(section, null);
                if (newItem == null) return;

                this.player.getInventory().addItem(newItem);
            }
        }
    }

    public void checkAxe(Boolean death) {
        PlayerHandler playerHandler = DeluxeBedwars.getInstance().getGameManager().getPlayer(this.player);

        if (death) {
            if (Variables.items.getBoolean("settings.save.axe.enabled")) {
                int newNumber = playerHandler.getAxe() + Variables.items.getInt("settings.save.axe.remove");

                if (newNumber > 0 && newNumber < Variables.items.getInt("ranking.axe.default"))
                    playerHandler.setAxe(newNumber);
            } else {
                playerHandler.setAxe(Variables.items.getInt("ranking.axe.default"));
            }
        }

        for (ItemStack item : this.player.getInventory().getContents()) {
            if (item == null || item.getType().equals(Material.AIR)) continue;

            String itemType = item.getType().name();
            if (itemType.contains("_AXE"))
                this.player.getInventory().removeItem(item);
        }

        if (playerHandler.getAxe() > 0 && playerHandler.getAxe() != Variables.items.getInt("ranking.axe.default")) {
            ConfigurationSection section = Variables.items.getConfigurationSection("items." + Variables.items.getString("ranking.axe." + playerHandler.getAxe()));
            if (section != null) {
                ItemStack newItem = new Items(player, playerHandler.getArena()).createItem(section, null);
                if (newItem == null) return;

                this.player.getInventory().addItem(newItem);
            }
        }
    }

    public void checkArmor(Boolean death) {
        if (death) {
            PlayerHandler playerHandler = DeluxeBedwars.getInstance().getGameManager().getPlayer(this.player);

            if (!playerHandler.getArmor().equals("leatherarmor")) {
                ConfigurationSection section = Variables.items.getConfigurationSection("items." + playerHandler.getArmor());
                for (String entry : section.getConfigurationSection("pieces").getKeys(false)) {
                    if (entry == null) continue;
                    ItemStack newItem = new Items(this.player, playerHandler.getArena()).createItem(section.getConfigurationSection("pieces." + entry), null);
                    if (newItem == null) continue;
                    if (this.team.getUpgrades().getProtectionLevel() > 0) newItem.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, this.team.getUpgrades().getProtectionLevel());

                    entry = entry.toLowerCase();
                    if (entry.contains("helmet")) this.player.getInventory().setHelmet(newItem);
                    if (entry.contains("chestplate")) this.player.getInventory().setChestplate(newItem);
                    if (entry.contains("leggings")) this.player.getInventory().setLeggings(newItem);
                    if (entry.contains("boots")) this.player.getInventory().setBoots(newItem);
                }
            }

            List<ItemStack> armors = new ArrayList<>(Arrays.asList(
                    new ItemStack(Material.LEATHER_HELMET),
                    new ItemStack(Material.LEATHER_CHESTPLATE),
                    new ItemStack(Material.LEATHER_LEGGINGS),
                    new ItemStack(Material.LEATHER_BOOTS)));

            for (ItemStack armor : armors) {
                if (this.team.getUpgrades().getProtectionLevel() > 0) armor.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, this.team.getUpgrades().getProtectionLevel());

                LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
                meta.setColor(Variables.colors.get(this.team.getName().toLowerCase()));
                meta.spigot().setUnbreakable(true);
                armor.setItemMeta(meta);
            }

            PlayerInventory playerInventory = this.player.getInventory();
            if (playerInventory.getHelmet() == null || playerInventory.getHelmet().getType() == Material.AIR)
                this.player.getInventory().setHelmet(armors.get(0));

            if (playerInventory.getChestplate() == null || playerInventory.getChestplate().getType() == Material.AIR)
                this.player.getInventory().setChestplate(armors.get(1));

            if (playerInventory.getLeggings() == null || playerInventory.getLeggings().getType() == Material.AIR)
                this.player.getInventory().setLeggings(armors.get(2));

            if (playerInventory.getBoots() == null || playerInventory.getBoots().getType() == Material.AIR)
                this.player.getInventory().setBoots(armors.get(3));
        }

        new BukkitRunnable() {
            public void run() {
                for (ItemStack item : PlayerManager.this.player.getInventory().getArmorContents()) {
                    if (item == null || item.getType() == Material.AIR) continue;

                    int level = PlayerManager.this.team.getUpgrades().getProtectionLevel();
                    if (level <= 0) continue;

                    if (item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) < level) item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
                }
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 3L);
    }

    public void checkWood() {
        new BukkitRunnable() {
            public void run() {
                String wood = new DataHandler(PlayerManager.this.player).getPlayerSelectedCosmetic("wood_skins");
                if (wood == null) {
                    if (Variables.cosmetics.getConfigurationSection("wood_skins.default") == null) return;
                    else wood = "default";
                }

                for (ItemStack item : PlayerManager.this.player.getInventory().getContents()) {
                    if (item == null || item.getType().equals(Material.AIR)) continue;

                    String itemType = item.getType().name();
                    if (itemType.equals("WOOD") || itemType.equals("LOG") || itemType.equals("LOG_2")) {
                        short data = (short) Variables.cosmetics.getInt("wood_skins." + wood + ".data");
                        Material mat = Material.getMaterial(Variables.cosmetics.getString("wood_skins." + wood + ".material"));
                        if (mat == null) return;
                        if (mat == item.getType() && item.getDurability() == data) continue;

                        item.setDurability(data);
                        item.setType(mat);
                    }
                }
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 5L);
    }

    public void checkSword() {
        new BukkitRunnable() {
            public void run() {
                ItemStack defaultItem = null;
                int otherSwordCount = 0;

                for (ItemStack item : PlayerManager.this.player.getInventory().getContents()) {
                    if (item == null || item.getType().equals(Material.AIR)) continue;

                    String itemType = item.getType().name();
                    if (!itemType.contains("SWORD")) continue;

                    Integer level = PlayerManager.this.team.getUpgrades().getSharpnessLevel();

                    if (level > 0 && item.getEnchantmentLevel(Enchantment.DAMAGE_ALL) <= 0) item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, level);
                    else if (level <= 0 && item.getEnchantmentLevel(Enchantment.DAMAGE_ALL) > 0) item.removeEnchantment(Enchantment.DAMAGE_ALL);
                    if (itemType.equals("WOOD_SWORD") || itemType.equals("WOODEN_SWORD")) {
                        defaultItem = item;
                        continue;
                    }

                    otherSwordCount += 1;
                }

                if (defaultItem != null && otherSwordCount > 0) player.getInventory().removeItem(defaultItem);
                if (defaultItem == null && otherSwordCount < 1) {
                    ItemStack newItem = getDefaultItem().clone();
                    Integer level = PlayerManager.this.team.getUpgrades().getSharpnessLevel();
                    if (level > 0) newItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);

                    player.getInventory().addItem(newItem);
                }
            }
        }.runTaskLater(DeluxeBedwars.getInstance(), 5L);
    }

    public void checkShears() {
        PlayerHandler playerHandler = DeluxeBedwars.getInstance().getGameManager().getPlayer(this.player);

        if (!Variables.items.getBoolean("settings.save.shears.enabled")) return;
        if (!playerHandler.getShear()) return;

        ConfigurationSection section = Variables.items.getConfigurationSection("items." + Variables.items.getString("settings.save.shears.item"));

        if (section != null) {
            ItemBuilder newItem = new ItemBuilder(Material.getMaterial(section.getString("material")), section.getInt("amount"), (byte) section.getInt("data"));

            if (section.getStringList("enchants") != null)
                for (String enchant : section.getStringList("enchants")) {
                    String[] args = enchant.split("[:]", 2);
                    if (args.length < 2) continue;

                    newItem.addEnchant(Enchantment.getByName(args[0]), Integer.parseInt(args[1]));
                }

            newItem.setUnbreakable(true);
            newItem.addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

            this.player.getInventory().addItem(newItem.toItemStack());
        }
    }
}
