package me.sedattr.bedwars.helpers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class SkullTexture {
    private Method GET_PROPERTIES;
    private Method INSERT_PROPERTY;
    private Constructor<?> GAME_PROFILE_CONSTRUCTOR;
    private Constructor<?> PROPERTY_CONSTRUCTOR;

    {
        try {
            final Class<?> gameProfile = Class.forName("com.mojang.authlib.GameProfile");
            final Class<?> property = Class.forName("com.mojang.authlib.properties.Property");
            final Class<?> propertyMap = Class.forName("com.mojang.authlib.properties.PropertyMap");
            GAME_PROFILE_CONSTRUCTOR = getConstructor(gameProfile, 2);
            PROPERTY_CONSTRUCTOR = getConstructor(property, 2);
            GET_PROPERTIES = getMethod(gameProfile, "getProperties");
            INSERT_PROPERTY = getMethod(propertyMap, "put");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Method getMethod(final Class<?> clazz, final String name) {
        for (final Method m : clazz.getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    private Field getField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
        return clazz.getDeclaredField(fieldName);
    }

    public void setFieldValue(final Object object, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        final Field f = getField(object.getClass(), fieldName);
        f.setAccessible(true);
        f.set(object, value);
    }

    public Constructor<?> getConstructor(final Class<?> clazz, final int numParams) {
        for (final Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterTypes().length == numParams) {
                return constructor;
            }
        }
        return null;
    }

    public ItemStack getSkull(String texture) {
        texture=texture.replace(" ", "");
        if(texture.length()>16) {
            try {
                ItemStack skull;
                if (!Variables.legacy)
                    skull = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
                else
                    skull = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
                final ItemMeta meta = skull.getItemMeta();
                try {
                    final Object profile = GAME_PROFILE_CONSTRUCTOR.newInstance(UUID.randomUUID(), UUID.randomUUID().toString().substring(17).replace("-", ""));
                    final Object properties = GET_PROPERTIES.invoke(profile);
                    INSERT_PROPERTY.invoke(properties, "textures", PROPERTY_CONSTRUCTOR.newInstance("textures", texture));
                    setFieldValue(meta, "profile", profile);
                } catch (Exception e) {
                    System.err.println("Failed to create fake GameProfile for custom player head:");
                    e.printStackTrace();
                }
                skull.setItemMeta(meta);
                return skull;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            /*
            if (texture.contains("hdb-")) {
                try {
                    //return Variables.HDBApi.getItemHead(texture.replace("hdb-", ""));
                } catch (Exception e) {
                    texture = "mhf_question";
                }
            }

             */
            ItemStack playerHead;
            if (!Variables.legacy)
                playerHead = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
            else
                playerHead = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
            SkullMeta sm = (SkullMeta) playerHead.getItemMeta();
            sm.setOwner(texture);
            playerHead.setItemMeta(sm);
            return playerHead;
        }
        return null;
    }

}