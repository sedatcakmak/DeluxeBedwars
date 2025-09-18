package me.sedattr.bedwars.handlers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.helpers.Variables;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class GeneratorHandler {
    private final int[] nextTimes;
    private final ArenaHandler arena;

    public GeneratorHandler(ArenaHandler arena) {
        this.arena = arena;
        this.nextTimes = new int[]{this.arena.getNextEvent().getEmeraldTime(), this.arena.getNextEvent().getDiamondTime()};
    }

    public void islandGenerator(TeamHandler team) {
        List<BukkitTask> newTasks = team.getIslandTasks();
        if (newTasks.size() > 0) newTasks.forEach(BukkitTask::cancel);
        newTasks = new ArrayList<>();

        ConfigurationSection section = Variables.config.getConfigurationSection("generators.island.level" + (team.getUpgrades().getGeneratorLevel()+1));
        Set<String> entries = section.getKeys(false);
        if (entries != null && !entries.isEmpty())
            for (String entry : entries) {
                BukkitTask bukkitTask = (new BukkitRunnable() {
                    public void run() {
                        Material material = Material.getMaterial(section.getString(entry + ".material"));
                        if (material != null)
                            spawnItem(team.getName(), team.getGeneratorLocation(), section.getInt(entry + ".limit"), material);
                    }
                }).runTaskTimer(DeluxeBedwars.getInstance(), Math.max(section.getInt(entry + ".spawn"), 1)* 20L, Math.max(section.getInt(entry + ".spawn"), 1)*20);
                newTasks.add(bukkitTask);
            }

        team.setIslandTasks(newTasks);
    }

    public void spawnItem(String name, Location location, Integer amount, Material material) {
        if (amount > 0) {
            Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 3, 3, 3);
            if (nearbyEntities != null && !nearbyEntities.isEmpty()) {
                int count = nearbyEntities.stream().filter(entity -> entity instanceof Item && ((Item) entity).getItemStack().getType() == material).mapToInt(item -> ((Item) item).getItemStack().getAmount()).sum();
                if (count >= amount) return;
            }
        }

        Item item = location.getWorld().dropItem(location, new ItemStack(material, 1));
        item.setVelocity(new Vector(0, 0, 0));
        item.setMetadata("BedwarsTEAM", new FixedMetadataValue(DeluxeBedwars.getInstance(), name));
    }

    public void reloadEmeraldTexts() {
        HashMap<Location, List<ArmorStand>> stands = Variables.textStands.getOrDefault(this.arena, new HashMap<>());

        List<String> texts = Variables.config.getStringList("generators.emerald.text");
        List<BukkitTask> newTasks = Variables.tasks.getOrDefault(this.arena, new ArrayList<>());

        BukkitTask task = (new BukkitRunnable() {
            public void run() {
                for (Location emeraldLoc : GeneratorHandler.this.arena.getEmeraldGenerators()) {
                    for (int i = 0; i < texts.size(); i++) {
                        String line = Utils.colorize(texts.get(i)
                                .replace("%tier%", String.valueOf(GeneratorHandler.this.arena.getNextEvent().getEmeraldNumber().length()))
                                .replace("%tierroman%", GeneratorHandler.this.arena.getNextEvent().getEmeraldNumber())
                                .replace("%time%", String.valueOf(GeneratorHandler.this.nextTimes[0])));

                        if (stands.containsKey(emeraldLoc)) stands.get(emeraldLoc).get(i).setCustomName(line);
                    }

                    if (GeneratorHandler.this.nextTimes[0] <= 1)
                        spawnItem(GeneratorHandler.this.arena.getName(), emeraldLoc, Variables.config.getInt("generators.emerald.limit"), Material.EMERALD);
                }
                GeneratorHandler.this.nextTimes[0]--;
                if (GeneratorHandler.this.nextTimes[0] <= 0)
                    GeneratorHandler.this.nextTimes[0] = GeneratorHandler.this.arena.getNextEvent().getEmeraldTime();
            }
        }).runTaskTimer(DeluxeBedwars.getInstance(), 0L, 20L);
        newTasks.add(task);
        Variables.tasks.put(this.arena, newTasks);
    }

    public void reloadDiamondTexts() {
        HashMap<Location, List<ArmorStand>> stands = Variables.textStands.getOrDefault(this.arena, new HashMap<>());
        List<String> texts = Variables.config.getStringList("generators.diamond.text");
        List<BukkitTask> newTasks = Variables.otherTasks.getOrDefault(this.arena, new ArrayList<>());

        BukkitTask task = (new BukkitRunnable() {
            public void run() {
                for (Location diamondLoc : GeneratorHandler.this.arena.getDiamondGenerators()) {
                    for (int i = 0; i < texts.size(); i++) {
                        String line = Utils.colorize(texts.get(i)
                                .replace("%tier%", String.valueOf(GeneratorHandler.this.arena.getNextEvent().getDiamondNumber().length()))
                                .replace("%tierroman%", GeneratorHandler.this.arena.getNextEvent().getDiamondNumber())
                                .replace("%time%", String.valueOf(GeneratorHandler.this.nextTimes[1])));

                        if (stands.containsKey(diamondLoc)) stands.get(diamondLoc).get(i).setCustomName(line);
                    }

                    if (GeneratorHandler.this.nextTimes[1] <= 1)
                        spawnItem(GeneratorHandler.this.arena.getName(), diamondLoc, Variables.config.getInt("generators.diamond.limit"), Material.DIAMOND);
                }

                GeneratorHandler.this.nextTimes[1]--;
                if (GeneratorHandler.this.nextTimes[1] <= 0)
                    GeneratorHandler.this.nextTimes[1] = GeneratorHandler.this.arena.getNextEvent().getDiamondTime();
            }
        }).runTaskTimer(DeluxeBedwars.getInstance(), 0L, 20L);
        newTasks.add(task);
        Variables.tasks.put(this.arena, newTasks);
    }

    public void createDiamondTexts() {
        HashMap<Location, List<ArmorStand>> stands = Variables.textStands.getOrDefault(this.arena, new HashMap<>());
        List<String> texts = Variables.config.getStringList("generators.diamond.text");

        for (Location diamondLoc : this.arena.getDiamondGenerators()) {
            List<ArmorStand> armors = new ArrayList<>();

            double addNumber = 3.25 + (texts.size() * 0.25);
            for (String line : texts) {
                Location newLocation = diamondLoc.clone();
                newLocation.add(0, addNumber, 0);

                line = Utils.colorize(line.replace("%tier%", String.valueOf(this.arena.getNextEvent().getDiamondNumber().length()))
                        .replace("%tierroman%", this.arena.getNextEvent().getDiamondNumber())
                        .replace("%time%", String.valueOf(nextTimes[1])));

                ArmorStand armorStand = newLocation.getWorld().spawn(newLocation, ArmorStand.class);
                armorStand.setMarker(true);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setCustomName(line);
                armorStand.setCustomNameVisible(true);
                armors.add(armorStand);

                addNumber-=0.25;
            }

            stands.put(diamondLoc, armors);
        }
        Variables.textStands.put(this.arena, stands);
    }

    public void createEmeraldTexts() {
        HashMap<Location, List<ArmorStand>> stands = Variables.textStands.getOrDefault(this.arena, new HashMap<>());
        List<String> texts = Variables.config.getStringList("generators.emerald.text");

        for (Location emeraldLoc : this.arena.getEmeraldGenerators()) {
            List<ArmorStand> armors = new ArrayList<>();

            double addNumber = 3.25 + (texts.size() * 0.25);
            for (String line : texts) {
                Location newLocation = emeraldLoc.clone();
                newLocation.add(0, addNumber, 0);

                line = Utils.colorize(line.replace("%tier%", String.valueOf(this.arena.getNextEvent().getEmeraldNumber().length()))
                        .replace("%tierroman%", this.arena.getNextEvent().getEmeraldNumber())
                        .replace("%time%", String.valueOf(nextTimes[0])));

                ArmorStand armorStand = newLocation.getWorld().spawn(newLocation, ArmorStand.class);
                armorStand.setMarker(true);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setCustomName(line);
                armorStand.setCustomNameVisible(true);
                armors.add(armorStand);

                addNumber-=0.25;
            }

            stands.put(emeraldLoc, armors);
        }
        Variables.textStands.put(this.arena, stands);
    }

    public void startOtherGenerators() {
        List<BukkitTask> newTasks = Variables.tasks.getOrDefault(this.arena, new ArrayList<>());
        HashMap<Location, ArmorStand> stands = Variables.armorStands.getOrDefault(this.arena, new HashMap<>());

        for (Location emeraldLoc : this.arena.getEmeraldGenerators()) {
            BukkitTask armorStandTask = (new BukkitRunnable() {
                public void run() {
                    if (!stands.containsKey(emeraldLoc)) {
                        Location newLocation = emeraldLoc.clone();
                        newLocation.add(0, 1.5, 0);

                        ArmorStand armorStand = (ArmorStand) emeraldLoc.getWorld().spawnEntity(newLocation, EntityType.ARMOR_STAND);
                        armorStand.setMarker(true);
                        armorStand.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
                        armorStand.setVisible(false);
                        armorStand.setCustomNameVisible(false);
                        armorStand.setGravity(false);
                        armorStand.getLocation().setYaw(armorStand.getLocation().getYaw());
                        stands.put(emeraldLoc, armorStand);
                    } else {
                        ArmorStand oldStand = stands.get(emeraldLoc);
                        Location newLocation = oldStand.getLocation();
                        newLocation.setYaw(newLocation.getYaw() + 10);

                        oldStand.teleport(newLocation);
                    }
                }
            }).runTaskTimer(DeluxeBedwars.getInstance(), 3L, 3L);

            newTasks.add(armorStandTask);
        }

        for (Location diamondLoc : this.arena.getDiamondGenerators()) {
            BukkitTask armorStandTask = (new BukkitRunnable() {
                public void run() {
                    if (!stands.containsKey(diamondLoc)) {
                        Location newLocation = diamondLoc.clone();
                        newLocation.add(0, 1.5, 0);

                        ArmorStand armorStand = (ArmorStand) diamondLoc.getWorld().spawnEntity(newLocation, EntityType.ARMOR_STAND);
                        armorStand.setHelmet(new ItemStack(Material.DIAMOND_BLOCK));
                        armorStand.setVisible(false);
                        armorStand.setCustomNameVisible(false);
                        armorStand.setGravity(false);
                        armorStand.getLocation().setYaw(armorStand.getLocation().getYaw());
                        stands.put(diamondLoc, armorStand);
                    } else {
                        ArmorStand oldStand = stands.get(diamondLoc);
                        Location newLocation = oldStand.getLocation();
                        newLocation.setYaw(newLocation.getYaw() + 10);

                        oldStand.teleport(newLocation);
                    }
                }
            }).runTaskTimer(DeluxeBedwars.getInstance(), 3L, 3L);

            newTasks.add(armorStandTask);
        }

        Variables.tasks.put(this.arena, newTasks);
        Variables.armorStands.put(this.arena, stands);

        if (Variables.textStands.getOrDefault(this.arena, new HashMap<>()).size() < 1) {
            createDiamondTexts();
            createEmeraldTexts();
        }

        reloadDiamondTexts();
        reloadEmeraldTexts();
    }
}
