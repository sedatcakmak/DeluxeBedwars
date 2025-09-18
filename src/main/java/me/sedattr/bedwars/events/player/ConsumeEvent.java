package me.sedattr.bedwars.events.player;

import me.sedattr.bedwars.DeluxeBedwars;
import me.sedattr.bedwars.handlers.ArenaHandler;
import me.sedattr.bedwars.handlers.PlayerHandler;
import me.sedattr.bedwars.helpers.Variables;
import me.sedattr.bedwars.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class ConsumeEvent implements Listener {
    private DeluxeBedwars plugin;

    public ConsumeEvent(DeluxeBedwars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ArenaHandler arena = this.plugin.getGameManager().getArena(player);
        if (arena == null) return;

        String potion = new NBTItem(e.getItem()).getString("BedwarsPOTION");
        if (potion == null || potion.equals("")) return;

        List<String> effects = Variables.items.getStringList("items." + potion + ".effects");
        if (effects == null || effects.size() <= 0) return;

        for (String effect : effects) {
            String[] args = effect.split("[:]", 3);
            if (args.length < 3) continue;

            if (args[0].equalsIgnoreCase("invisibility")) {
                PlayerHandler playerHandler = this.plugin.getGameManager().getPlayer(player);
                if (playerHandler == null) continue;

                playerHandler.setInvis(true, Integer.parseInt(args[2])*20);
            }

            if (player.hasPotionEffect(PotionEffectType.getByName(args[0])))
                player.removePotionEffect(PotionEffectType.getByName(args[0]));

            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(args[0]), Integer.parseInt(args[2])*20, Integer.parseInt(args[1])-1));
        }

        e.setCancelled(true);
        int i = e.getItem().getAmount();
        if (i > 1) {
            e.getItem().setAmount(i - 1);
            return;
        }
        player.getInventory().setItemInHand(null);
    }
}
