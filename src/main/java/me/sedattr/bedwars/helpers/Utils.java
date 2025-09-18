package me.sedattr.bedwars.helpers;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.managers.DefaultFontInfo;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.entity.Fireball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Utils {
    public static String isBed(Player player, Block block) {
        ArenaHandler arena = DeluxeBedwars.getInstance().getGameManager().getArena(player);
        if (arena == null) return "";

        String bedOwner = "";

        for (TeamHandler team : arena.getTeams()) {
            for (int i = block.getX() - 2; i < block.getX() + 2; i++) {
                for (int j = block.getY() - 2; j < block.getY() + 2; j++) {
                    for (int k = block.getZ() - 2; k < block.getZ() + 2; k++) {
                        if (team.getBedLocation().getBlockX() == i && team.getBedLocation().getBlockY() == j && team.getBedLocation().getBlockZ() == k) {
                            bedOwner = team.getName();
                        }
                    }
                }
            }
        }

        return bedOwner;
    }

    public static boolean notBlock(Block block, String name) {
        ArenaHandler arena = block.hasMetadata("BedwarsMATCH") ? (ArenaHandler) block.getMetadata("BedwarsMATCH").get(0).value() : null;
        return arena == null || !arena.getName().equalsIgnoreCase(name);
    }

    public static boolean isNearby(ArenaHandler arena, Location loc) {
        List<Location> locations = new ArrayList<>();
        locations.addAll(arena.getDiamondGenerators());
        locations.addAll(arena.getEmeraldGenerators());

        double distance = Math.max(Variables.config.getDouble("generators.protection"), 3.0);
        for (Location location : locations) {
            if (location.distance(loc) <= distance)
                return true;
        }

        return false;
    }

    public static String[] hasItem(Player player, String name) {
        String[] requiredItem = name.split("[:]", 2);

        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().equals(Material.AIR)) continue;

            if (item.getType().name().equals(requiredItem[0])) {
                count+=item.getAmount();
            }
        }

        return new String[]{String.valueOf(count), requiredItem[1], requiredItem[0]};
    }

    public static String colorize(String s) {
        if (!Bukkit.getVersion().contains("1.16")) return ChatColor.translateAlternateColorCodes('&', s);

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher match = pattern.matcher(s);
        while (match.find()) {
            String hexColor = s.substring(match.start(), match.end());
            //s = s.replace(hexColor, ChatColor.of(hexColor).toString());
            match = pattern.matcher(s);
        }

        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String sendMessage(Player player, String text) {
        String message = Variables.messages.getString(text);
        if (message == null || message.equals("")) return message;
        message = colorize(message);

        if (message.contains("%center%"))
            message = centeredMessage(message);

        if (player != null) player.sendMessage(message);
        return message;
    }

    public static String sendMessage(CommandSender player, String text) {
        String message = Variables.messages.getString(text);
        if (message == null || message.equals("")) return message;
        message = colorize(message);

        if (message.contains("%center%"))
            message = centeredMessage(message);

        if (player != null) player.sendMessage(message);
        return message;
    }

    public static void setDirection(Fireball fireball, Vector direction) {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String nmsFireball = "net.minecraft.server." + version + "EntityFireball";
        String craftFireball = "org.bukkit.craftbukkit." + version + "entity.CraftFireball";
        try {
            Class<?> fireballClass = Class.forName(nmsFireball);
            Field fieldFireballDirX = fireballClass.getDeclaredField("dirX");
            Field fieldFireballDirY = fireballClass.getDeclaredField("dirY");
            Field fieldFireballDirZ = fireballClass.getDeclaredField("dirZ");
            Method craftFireballHandle = Class.forName(craftFireball).getDeclaredMethod("getHandle");

            Object handle = craftFireballHandle.invoke(fireball);
            fieldFireballDirX.set(handle, direction.getX() * 0.10D);
            fieldFireballDirY.set(handle, direction.getY() * 0.10D);
            fieldFireballDirZ.set(handle, direction.getZ() * 0.10D);
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
        }
    }

    public static String centeredMessage(String message) {
        message = colorize(message);

        if (!message.contains("%center%"))
            return message;

        message = message.replace("%center%", "");
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb.toString() + message;
    }

    public static String format(int n) {
        DecimalFormat format = new DecimalFormat();
        format.setRoundingMode(RoundingMode.DOWN);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);

        String formatted = format.format(n);
        return formatted.split("[.]", 2)[0];
    }

    public static void removeArmors(Player player) {
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }
}
