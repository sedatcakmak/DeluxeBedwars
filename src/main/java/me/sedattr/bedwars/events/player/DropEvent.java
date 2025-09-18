package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.helpers.Enums;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.managers.PlayerManager;
import me.sedattr.bedwars.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropEvent implements Listener {
    private final DeluxeBedwars plugin;

    public DropEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        NBTItem nbti = new NBTItem(e.getItemDrop().getItemStack());
        String items = nbti.getString("BedwarsINVENTORYITEM");
        if (items != null && !items.equals(""))
            e.setCancelled(true);

        ArenaHandler arena = this.plugin.getGameManager().getArena(e.getPlayer());
        if (arena == null) return;

        if (arena.getState() == Enums.ArenaState.WAITING || arena.getState() == Enums.ArenaState.STARTING) {
            e.setCancelled(true);
            return;
        }

        if (e.getItemDrop() == null || e.getItemDrop().getItemStack() == null || e.getItemDrop().getItemStack().getType().equals(Material.AIR)) return;
        String type = e.getItemDrop().getItemStack().getType().name();
        if (type.equals("WOODEN_SWORD")
                || type.equals("WOOD_SWORD")
                || type.contains("PICKAXE")
                || type.contains("AXE")
                || type.contains("SHEAR")) e.setCancelled(true);

        if (type.contains("SWORD") && !type.equals("WOOD_SWORD") && !type.equals("WOODEN_SWORD")) {
            if (e.getItemDrop().getItemStack().getEnchantmentLevel(Enchantment.DAMAGE_ALL) > 0)
                e.getItemDrop().getItemStack().removeEnchantment(Enchantment.DAMAGE_ALL);

            new PlayerManager(e.getPlayer()).checkSword();
            return;
        }

        if (type.equals("WOOD") || type.equals("LOG") || type.equals("LOG_2")) {
            Material mat = Variables.cosmetics.getString("wood_skins.default.material") != null ? Material.getMaterial(Variables.cosmetics.getString("wood_skins.default.material")) : Material.getMaterial("WOOD");
            if (mat == null) mat = Material.getMaterial("OAK_WOOD");

            short data = (short) (Variables.cosmetics.getString("wood_skins.default.data") != null ? Variables.cosmetics.getInt("wood_skins.default.data") : 0);

            e.getItemDrop().getItemStack().setDurability(data);
            e.getItemDrop().getItemStack().setType(mat);
        }
    }
}
