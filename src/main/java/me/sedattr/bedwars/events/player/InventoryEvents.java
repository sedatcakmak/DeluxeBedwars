package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.managers.Menus;
import me.sedattr.bedwars.managers.PlayerManager;
import me.sedattr.bedwars.nbtapi.NBTItem;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryEvents implements Listener {
    private final DeluxeBedwars plugin;

    public InventoryEvents(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent e) {
        ArenaHandler arena = this.plugin.getGameManager().getArena(e.getClicker());
        if (arena == null) return;

        if (e.getNPC().getFullName().equalsIgnoreCase("shop"))
            new Menus(e.getClicker()).openShopMenu("blocks");

        if (e.getNPC().getFullName().equalsIgnoreCase("upgrades"))
            new Menus(e.getClicker()).openUpgradesMenu();
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getClickedInventory() == null)
            return;
        if (e.getView() == null)
            return;
        if (e.getView().getBottomInventory() == null)
            return;
        if (e.getWhoClicked() == null)
            return;

        Player player = (Player) e.getWhoClicked();
        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena != null) {
            if (e.getSlotType() == InventoryType.SlotType.ARMOR)
                e.setCancelled(true);
            return;
        }

        if (e.getClick() != ClickType.NUMBER_KEY) return;
        ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
        if (item == null || item.getType().equals(Material.AIR)) return;
        NBTItem nbti = new NBTItem(item);

        String items = nbti.getString("BedwarsINVENTORYITEM");
        if (items != null && !items.equals(""))
            e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;

        Player player = (Player) e.getPlayer();
        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;
        if (player.getOpenInventory() == null) return;
        if (player.getOpenInventory().getTopInventory() != null) return;

        new PlayerManager(player).checkSword();
    }
}
