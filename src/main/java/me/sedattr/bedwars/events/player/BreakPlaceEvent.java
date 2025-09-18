package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.cosmetics.Cosmetics;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.TeamHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Utils;
import me.sedattr.bedwars.managers.Countdown;
import me.sedattr.bedwars.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.metadata.FixedMetadataValue;

public class BreakPlaceEvent implements Listener {
    private final DeluxeBedwars plugin;

    public BreakPlaceEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;
        Block block = e.getBlock();

        if (Countdown.deathTasks.containsKey(e.getPlayer()) || !arena.getPlayer(e.getPlayer()).isAlive()) {
            e.setCancelled(true);
            return;
        }

        if (arena.getState().equals(Enums.ArenaState.ENDED) || arena.getState().equals(Enums.ArenaState.WAITING) || arena.getState().equals(Enums.ArenaState.STARTING)) {
            e.setCancelled(true);
            return;
        }

        if (block.getType().toString().contains("BED")) {
            String bedOwner = Utils.isBed(player, block);
            TeamHandler playerTeam = this.plugin.getGameManager().getPlayerTeams().get(player);

            e.getBlock().getDrops().clear();
            Bed bed = (Bed) block.getState().getData();
            Block block1 = block.getRelative(bed.getFacing());
            Block block2 = block.getRelative(bed.getFacing().getOppositeFace());

            if (block.getType().name().contains("BED")) arena.addBrokenBlock(block.getX() + ":" + block.getY() + ":" + block.getZ() + ":" + block.getTypeId() + ":" + block.getData() + ":" + block.getType());
            if (block1 != null && block1.getType().name().contains("BED")) arena.addBrokenBlock(block1.getX() + ":" + block1.getY() + ":" + block1.getZ() + ":" + block1.getTypeId() + ":" + block1.getData() + ":" + block1.getType());
            if (block2 != null && block2.getType().name().contains("BED")) arena.addBrokenBlock(block2.getX() + ":" + block2.getY() + ":" + block2.getZ() + ":" + block2.getTypeId() + ":" + block2.getData() + ":" + block2.getType());

            TeamHandler oldTeam = arena.getTeam(bedOwner);
            if (!bedOwner.equals("")) {
                if (bedOwner.equalsIgnoreCase(playerTeam.getName())) {
                    e.setCancelled(true);
                    Utils.sendMessage(player, "cant_break_own_bed");
                } else if (oldTeam != null && oldTeam.getBedStatus()) {
                    block.getDrops().clear();

                    oldTeam.bedMessages(player);
                    new Cosmetics(player, player.getLocation(), player.getLocation(), true, "bed_destroys").start();
                    oldTeam.setBedStatus(false);

                    arena.checkWinner();
                }
            }

            return;
        }

        if (Utils.notBlock(block, arena.getName())) {
            e.setCancelled(true);
            Utils.sendMessage(player, "cant_break_block");
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer() == null) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItemInHand();
        if (item != null && !item.getType().equals(Material.AIR)) {
            NBTItem nbti = new NBTItem(item);

            String items = nbti.getString("BedwarsINVENTORYITEM");
            if (items != null && !items.equals(""))
                e.setCancelled(true);
        }

        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;

        Block block = e.getBlockPlaced();
        if (block == null) return;

        if (arena.getState().equals(Enums.ArenaState.ENDED) || arena.getState().equals(Enums.ArenaState.WAITING) || arena.getState().equals(Enums.ArenaState.STARTING)) {
            e.setCancelled(true);
            return;
        }

        for (TeamHandler team : arena.getTeams()) {
            if (team != null) {
                if (team.isInside(team.getFirstLocation(), team.getSecondLocation(), block.getLocation())) {
                    Utils.sendMessage(player, "land_protected");
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (Utils.isNearby(arena, block.getLocation())) {
            Utils.sendMessage(player, "land_protected");
            e.setCancelled(true);
            return;
        }

        if (block.getType() == Material.TNT) {
            block.setType(Material.AIR);
            TNTPrimed primed = block.getLocation().getWorld().spawn(e.getBlock().getLocation().add(0.5D, 0.0D, 0.5D), TNTPrimed.class);
            primed.setFuseTicks(50);
            primed.setYield(3.5F);
            primed.setMetadata("BedwarsOWNER", new FixedMetadataValue(DeluxeBedwars.getInstance(), e.getPlayer()));
            return;
        }

        block.setMetadata("BedwarsMATCH", new FixedMetadataValue(DeluxeBedwars.getInstance(), arena));
        arena.addPlacedBlock(block);
    }
}
