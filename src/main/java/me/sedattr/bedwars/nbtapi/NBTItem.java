package me.sedattr.bedwars.nbtapi;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.sedattr.bedwars.nbtapi.utils.nmsmappings.ReflectionMethod;

public class NBTItem extends NBTCompound {
	private ItemStack bukkitItem;
	private final boolean directApply;
	private ItemStack originalSrcStack = null;

	public NBTItem(ItemStack item) {
		this(item, false);
	}

	public NBTItem(ItemStack item, boolean directApply) {
		super(null, null);
		if (item == null || item.getType() == Material.AIR) {
			throw new NullPointerException("ItemStack can't be null/Air!");
		}
		this.directApply = directApply;
		bukkitItem = item.clone();
		if(directApply) {
			this.originalSrcStack = item;
		}
	}

	@Override
	public Object getCompound() {
		return NBTReflectionUtil.getItemRootNBTTagCompound(ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, bukkitItem));
	}

	@Override
	protected void setCompound(Object compound) {
		Object stack = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, bukkitItem);
		ReflectionMethod.ITEMSTACK_SET_TAG.run(stack, compound);
		bukkitItem = (ItemStack) ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, stack);
	}

	public void applyNBT(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			throw new NullPointerException("ItemStack can't be null/Air!");
		}
		NBTItem nbti = new NBTItem(new ItemStack(item.getType()));
		nbti.mergeCompound(this);
		item.setItemMeta(nbti.getItem().getItemMeta());
	}

	public ItemStack getItem() {
		return bukkitItem;
	}

	@Override
	protected void saveCompound() {
		if(directApply) {
			applyNBT(originalSrcStack);
		}
	}
}
