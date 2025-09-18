package me.sedattr.bedwars.nms;

import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class v1_16_R3 implements NMS {
    public void hideShowArmor(Player hiddenPlayer, Player player, Boolean show) {
        PlayerInventory inventory = hiddenPlayer.getInventory();
        for (int i = 1; i<=4; i++) {
            ItemStack item = null;
            EnumItemSlot slot = null;

            if (i == 1) {
                item = inventory.getHelmet();
                slot = EnumItemSlot.HEAD;
            }
            if (i == 2) {
                item = inventory.getChestplate();
                slot = EnumItemSlot.CHEST;
            }
            if (i == 3) {
                item = inventory.getLeggings();
                slot = EnumItemSlot.LEGS;
            }
            if (i == 4) {
                item = inventory.getBoots();
                slot = EnumItemSlot.FEET;
            }

            if (!show) item = new ItemStack(Material.AIR);

            PacketPlayOutEntityEquipment packetPlayOutEntityEquipment = new PacketPlayOutEntityEquipment();
            ReflectionUtils.setFieldValue("a", packetPlayOutEntityEquipment.getClass(), packetPlayOutEntityEquipment, ((org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer) hiddenPlayer).getHandle().getBukkitEntity().getEntityId());
            ReflectionUtils.setFieldValue("b", packetPlayOutEntityEquipment.getClass(), packetPlayOutEntityEquipment, slot);
            ReflectionUtils.setFieldValue("c", packetPlayOutEntityEquipment.getClass(), packetPlayOutEntityEquipment, CraftItemStack.asNMSCopy(item));

            (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(packetPlayOutEntityEquipment);
        }
    }
}
