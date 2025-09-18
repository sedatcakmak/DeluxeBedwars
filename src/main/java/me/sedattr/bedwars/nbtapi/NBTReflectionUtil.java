package me.sedattr.bedwars.nbtapi;

import me.sedattr.bedwars.nbtapi.utils.MinecraftVersion;
import me.sedattr.bedwars.nbtapi.utils.nmsmappings.ClassWrapper;
import me.sedattr.bedwars.nbtapi.utils.nmsmappings.ObjectCreator;
import me.sedattr.bedwars.nbtapi.utils.nmsmappings.ReflectionMethod;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

public class NBTReflectionUtil {
    private static Field field_unhandledTags = null;

    static {
        try {
            field_unhandledTags = ClassWrapper.CRAFT_METAITEM.getClazz().getDeclaredField("unhandledTags");
            field_unhandledTags.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
    }

    private NBTReflectionUtil() {
    }

    public static Object getItemRootNBTTagCompound(Object nmsitem) {
        try {
            Object answer = ReflectionMethod.NMSITEM_GETTAG.run(nmsitem);
            return answer != null ? answer : ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        } catch (Exception e) {
            throw new NbtApiException("Exception while getting an Itemstack's NBTCompound!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getUnhandledNBTTags(ItemMeta meta) {
        try {
            return (Map<String, Object>) field_unhandledTags.get(meta);
        } catch (Exception e) {
            throw new NbtApiException("Exception while getting unhandled tags from ItemMeta!", e);
        }
    }

    public static Object getEntityNBTTagCompound(Object nmsEntity) {
        try {
            Object nbt = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            Object answer = ReflectionMethod.NMS_ENTITY_GET_NBT.run(nmsEntity, nbt);
            if (answer == null)
                answer = nbt;
            return answer;
        } catch (Exception e) {
            throw new NbtApiException("Exception while getting NBTCompound from NMS Entity!", e);
        }
    }

    public static Object getTileEntityNBTTagCompound(BlockState tile) {
        try {
            Object cworld = ClassWrapper.CRAFT_WORLD.getClazz().cast(tile.getWorld());
            Object nmsworld = ReflectionMethod.CRAFT_WORLD_GET_HANDLE.run(cworld);
            Object o;
            if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
                o = ReflectionMethod.NMS_WORLD_GET_TILEENTITY_1_7_10.run(nmsworld, tile.getX(), tile.getY(), tile.getZ());
            } else {
                Object pos = ObjectCreator.NMS_BLOCKPOSITION.getInstance(tile.getX(), tile.getY(), tile.getZ());
                o = ReflectionMethod.NMS_WORLD_GET_TILEENTITY.run(nmsworld, pos);
            }
            Object tag = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            Object answer = ReflectionMethod.TILEENTITY_GET_NBT.run(o, tag);
            if (answer == null)
                answer = tag;
            return answer;
        } catch (Exception e) {
            throw new NbtApiException("Exception while getting NBTCompound from TileEntity!", e);
        }
    }

    public static Object getSubNBTTagCompound(Object compound, String name) {
        try {
            if ((boolean) ReflectionMethod.COMPOUND_HAS_KEY.run(compound, name)) {
                return ReflectionMethod.COMPOUND_GET_COMPOUND.run(compound, name);
            } else {
                throw new NbtApiException("Tried getting invalide compound '" + name + "' from '" + compound + "'!");
            }
        } catch (Exception e) {
            throw new NbtApiException("Exception while getting NBT subcompounds!", e);
        }
    }

    public static Boolean valideCompound(NBTCompound comp) {
        Object root = comp.getCompound();
        if (root == null) {
            root = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        return (gettoCompount(root, comp)) != null;
    }

    protected static Object gettoCompount(Object nbttag, NBTCompound comp) {
        Deque<String> structure = new ArrayDeque<>();
        while (comp.getParent() != null) {
            structure.add(comp.getName());
            comp = comp.getParent();
        }
        while (!structure.isEmpty()) {
            String target = structure.pollLast();
            nbttag = getSubNBTTagCompound(nbttag, target);
            if (nbttag == null) {
                throw new NbtApiException("Unable to find tag: " + target);
            }
        }
        return nbttag;
    }

    public static void mergeOtherNBTCompound(NBTCompound comp, NBTCompound nbtcompoundSrc) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        if (!valideCompound(comp))
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        Object workingtag = gettoCompount(rootnbttag, comp);
        Object rootnbttagSrc = nbtcompoundSrc.getCompound();
        if (rootnbttagSrc == null) {
            rootnbttagSrc = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        if (!valideCompound(nbtcompoundSrc))
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        Object workingtagSrc = gettoCompount(rootnbttagSrc, nbtcompoundSrc);
        try {
            ReflectionMethod.COMPOUND_MERGE.run(workingtag, workingtagSrc);
            comp.setCompound(rootnbttag);
        } catch (Exception e) {
            throw new NbtApiException("Exception while merging two NBTCompounds!", e);
        }
    }

    public static void set(NBTCompound comp, String key, Object val) {
        if (val == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        if (!valideCompound(comp)) {
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        }
        Object workingtag = gettoCompount(rootnbttag, comp);
        try {
            ReflectionMethod.COMPOUND_SET.run(workingtag, key, val);
            comp.setCompound(rootnbttag);
        } catch (Exception e) {
            throw new NbtApiException("Exception while setting key '" + key + "' to '" + val + "'!", e);
        }
    }

    public static <T> NBTList<T> getList(NBTCompound comp, NBTType type) {
        if (!valideCompound(comp))
            return null;
		try {
            return null;
        } catch (Exception ex) {
            throw new NbtApiException("Exception while getting a list with the type '" + type + "'!", ex);
        }
    }

    public static NBTType getListType(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        if (!valideCompound(comp))
            return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        try {
            Object nbt = ReflectionMethod.COMPOUND_GET.run(workingtag, key);
            Field f = nbt.getClass().getDeclaredField("type");
            f.setAccessible(true);
            return NBTType.valueOf(f.getByte(nbt));
        } catch (Exception ex) {
            throw new NbtApiException("Exception while getting the list type!", ex);
        }
    }

    public static Object getEntry(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        if (!valideCompound(comp))
            return null;
        Object workingtag = gettoCompount(rootnbttag, comp);
        try {
			return ReflectionMethod.COMPOUND_GET.run(workingtag, key);
        } catch (Exception ex) {
            throw new NbtApiException("Exception while getting an Entry!", ex);
        }
    }

    public static void remove(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        if (!valideCompound(comp))
            return;
        Object workingtag = gettoCompount(rootnbttag, comp);
        ReflectionMethod.COMPOUND_REMOVE_KEY.run(workingtag, key);
        comp.setCompound(rootnbttag);
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getKeys(NBTCompound comp) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        if (!valideCompound(comp))
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        Object workingtag = gettoCompount(rootnbttag, comp);
        return (Set<String>) ReflectionMethod.COMPOUND_GET_KEYS.run(workingtag);
    }

    public static void setData(NBTCompound comp, ReflectionMethod type, String key, Object data) {
        if (data == null) {
            remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        }
        if (!valideCompound(comp))
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        Object workingtag = gettoCompount(rootnbttag, comp);
        type.run(workingtag, key, data);
        comp.setCompound(rootnbttag);
    }

    public static Object getData(NBTCompound comp, ReflectionMethod type, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            return null;
        }
        if (!valideCompound(comp))
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        Object workingtag = gettoCompount(rootnbttag, comp);
        return type.run(workingtag, key);
    }
}
