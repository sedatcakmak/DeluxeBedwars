package me.sedattr.bedwars.events.entity;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.Bed;
import org.bukkit.material.MaterialData;

import java.util.List;

public class ExplodeEvent implements Listener {
    private final DeluxeBedwars plugin;

    public ExplodeEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        Entity entity = e.getEntity();

        if (entity.hasMetadata("BedwarsCOSMETIC")) {
            Player player = (Player) entity.getMetadata("BedwarsCOSMETIC").get(0).value();
            ArenaHandler arena = this.plugin.getGameManager().getArena(player);
            if (arena == null || arena.getState() == Enums.ArenaState.ENDED) {
                e.blockList().clear();
                e.setCancelled(true);

                if (arena == null)
                    entity.remove();
                return;
            }

            List<Block> list = e.blockList();
            if (list.isEmpty())
                return;

            for (Block block : list) {
                if (block == null || block.getType() == Material.AIR) continue;

                MaterialData data = block.getState().getData();
                if (data instanceof Bed) {
                    Bed bed = (Bed) data;
                    Block block1 = block.getRelative(bed.getFacing());
                    Block block2 = block.getRelative(bed.getFacing().getOppositeFace());

                    arena.addBrokenBlock(block1.getX() + ":" + block1.getY() + ":" + block1.getZ() + ":" + block1.getTypeId() + ":" + block1.getData() + ":" + block1.getType());
                    arena.addBrokenBlock(block2.getX() + ":" + block2.getY() + ":" + block2.getZ() + ":" + block2.getTypeId() + ":" + block2.getData() + ":" + block2.getType());
                }

                arena.addBrokenBlock(block.getX() + ":" + block.getY() + ":" + block.getZ() + ":" + block.getTypeId() + ":" + block.getData() + ":" + block.getType());
            }
            return;
        }

        if (entity.hasMetadata("BedwarsDRAGONARENA")) {
            ArenaHandler arena = (ArenaHandler) entity.getMetadata("BedwarsDRAGONARENA").get(0).value();
            if (arena == null) return;

            List<Block> list = e.blockList();
            if (list.isEmpty())
                return;

            for (Block block : list) {
                if (block == null || block.getType() == Material.AIR) continue;

                arena.addBrokenBlock(block.getX() + ":" + block.getY() + ":" + block.getZ() + ":" + block.getTypeId() + ":" + block.getData() + ":" + block.getType());
            }
            return;
        }

        if (entity.hasMetadata("BedwarsOWNER")) {
            Player damager = (Player) e.getEntity().getMetadata("BedwarsOWNER").get(0).value();
            if (damager == null) return;
            ArenaHandler arena = this.plugin.getGameManager().getArena(damager);
            if (arena == null) return;

            List<Block> list = e.blockList();
            list.removeIf(block -> !arena.getPlacedBlocks().contains(block) ||
                    block.getType().name().contains("GLASS") ||
                    block.getType().name().contains("BED") ||
                    Utils.notBlock(block, arena.getName()));

            for (Block block : list)
                block.breakNaturally();
        }
    }
}
