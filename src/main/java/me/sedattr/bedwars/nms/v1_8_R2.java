package me.sedattr.bedwars.nms;

import net.minecraft.server.v1_8_R2.PacketPlayOutEntityEquipment;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class v1_8_R2 implements NMS {
    public void hideShowArmor(Player hiddenPlayer, Player player, Boolean show) {
        PlayerInventory inventory = hiddenPlayer.getInventory();
        for (int i = 1; i<=4; i++) {
            ItemStack item = null;
            if (i == 4) item = inventory.getHelmet();
            if (i == 3) item = inventory.getChestplate();
            if (i == 2) item = inventory.getLeggings();
            if (i == 1) item = inventory.getBoots();

            if (!show) item = new ItemStack(Material.AIR);

            PacketPlayOutEntityEquipment packetPlayOutEntityEquipment = new PacketPlayOutEntityEquipment();
            ReflectionUtils.setFieldValue("a", packetPlayOutEntityEquipment.getClass(), packetPlayOutEntityEquipment, ((CraftPlayer) hiddenPlayer).getHandle().getBukkitEntity().getEntityId());
            ReflectionUtils.setFieldValue("b", packetPlayOutEntityEquipment.getClass(), packetPlayOutEntityEquipment, i);
            ReflectionUtils.setFieldValue("c", packetPlayOutEntityEquipment.getClass(), packetPlayOutEntityEquipment, CraftItemStack.asNMSCopy(item));

            (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(packetPlayOutEntityEquipment);
        }
    }
}
