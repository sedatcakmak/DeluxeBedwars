package me.sedattr.bedwars.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

@SuppressWarnings("unused")
public final class ReflectionUtils {
    private ReflectionUtils() {}

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (DataType.notCompared(DataType.getPrimitive(constructor.getParameterTypes()), primitiveTypes)) {
                continue;
            }
            return constructor;
        }
        throw new NoSuchMethodException("There is no such constructor in this class with the specified parameter types");
    }

    public static Constructor<?> getConstructor(String className, PackageType packageType, Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getConstructor(packageType.getClass(className), parameterTypes);
    }

    public static Object instantiateObject(Class<?> clazz, Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getConstructor(clazz, DataType.getPrimitive(arguments)).newInstance(arguments);
    }

    public static Object instantiateObject(String className, PackageType packageType, Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return instantiateObject(packageType.getClass(className), arguments);
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName) || DataType.notCompared(DataType.getPrimitive(method.getParameterTypes()), primitiveTypes)) {
                continue;
            }
            return method;
        }
        throw new NoSuchMethodException("There is no such method in this class with the specified name and parameter types");
    }

    public static Method getMethod(String className, PackageType packageType, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(packageType.getClass(className), methodName, parameterTypes);
    }

    public static Object invokeMethod(Object instance, String methodName, Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getMethod(instance.getClass(), methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    public static Object invokeMethod(Object instance, Class<?> clazz, String methodName, Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getMethod(clazz, methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    public static Object invokeMethod(Object instance, String className, PackageType packageType, String methodName, Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return invokeMethod(instance, packageType.getClass(className), methodName, arguments);
    }

    /**
     * Returns a field of the target class with the given name
     *
     * @param clazz Target class
     * @param declared Whether the desired field is declared or not
     * @param fieldName Name of the desired field
     * @return The field of the target class with the specified name
     * @throws NoSuchFieldException If the desired field of the given class cannot be found
     * @throws SecurityException If the desired field cannot be made accessible
     */
    public static Field getField(Class<?> clazz, boolean declared, String fieldName) throws NoSuchFieldException, SecurityException {
        Field field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * Returns a field of a desired class with the given name
     *
     * @param className Name of the desired target class
     * @param packageType Package where the desired target class is located
     * @param declared Whether the desired field is declared or not
     * @param fieldName Name of the desired field
     * @return The field of the desired target class with the specified name
     * @throws NoSuchFieldException If the desired field of the desired class cannot be found
     * @throws SecurityException If the desired field cannot be made accessible
     * @throws ClassNotFoundException If the desired target class with the specified name and package cannot be found
     * @see #getField(Class, boolean, String)
     */
    public static Field getField(String className, PackageType packageType, boolean declared, String fieldName) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
        return getField(packageType.getClass(className), declared, fieldName);
    }

    public static void setFieldValue(String paramString, Class<?> paramClass, Object paramObject1, Object paramObject2) {
        try {
            Field field = paramClass.getDeclaredField(paramString);
            field.setAccessible(true);
            field.set(paramObject1, paramObject2);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Returns the value of a field of the given class of an object
     *
     * @param instance Target object
     * @param clazz Target class
     * @param declared Whether the desired field is declared or not
     * @param fieldName Name of the desired field
     * @return The value of field of the target object
     * @throws IllegalArgumentException If the target object does not feature the desired field
     * @throws IllegalAccessException If the desired field cannot be accessed
     * @throws NoSuchFieldException If the desired field of the target class cannot be found
     * @throws SecurityException If the desired field cannot be made accessible
     * @see #getField(Class, boolean, String)
     */
    public static Object getValue(Object instance, Class<?> clazz, boolean declared, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getField(clazz, declared, fieldName).get(instance);
    }

    /**
     * Returns the value of a field of a desired class of an object
     *
     * @param instance Target object
     * @param className Name of the desired target class
     * @param packageType Package where the desired target class is located
     * @param declared Whether the desired field is declared or not
     * @param fieldName Name of the desired field
     * @return The value of field of the target object
     * @throws IllegalArgumentException If the target object does not feature the desired field
     * @throws IllegalAccessException If the desired field cannot be accessed
     * @throws NoSuchFieldException If the desired field of the desired class cannot be found
     * @throws SecurityException If the desired field cannot be made accessible
     * @throws ClassNotFoundException If the desired target class with the specified name and package cannot be found
     * @see #getValue(Object, Class, boolean, String)
     */
    public static Object getValue(Object instance, String className, PackageType packageType, boolean declared, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        return getValue(instance, packageType.getClass(className), declared, fieldName);
    }

    /**
     * Returns the value of a field with the given name of an object
     *
     * @param instance Target object
     * @param declared Whether the desired field is declared or not
     * @param fieldName Name of the desired field
     * @return The value of field of the target object
     * @throws IllegalArgumentException If the target object does not feature the desired field (should not occur since it searches for a field with the given name in the class of the object)
     * @throws IllegalAccessException If the desired field cannot be accessed
     * @throws NoSuchFieldException If the desired field of the target object cannot be found
     * @throws SecurityException If the desired field cannot be made accessible
     * @see #getValue(Object, Class, boolean, String)
     */
    public static Object getValue(Object instance, boolean declared, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getValue(instance, instance.getClass(), declared, fieldName);
    }

    /**
     * Sets the value of a field of the given class of an object
     *
     * @param instance Target object
     * @param clazz Target class
     * @param declared Whether the desired field is declared or not
     * @param fieldName Name of the desired field
     * @param value New value
     * @throws IllegalArgumentException If the type of the value does not match the type of the desired field
     * @throws IllegalAccessException If the desired field cannot be accessed
     * @throws NoSuchFieldException If the desired field of the target class cannot be found
     * @throws SecurityException If the desired field cannot be made accessible
     * @see #getField(Class, boolean, String)
     */
    public static void setValue(Object instance, Class<?> clazz, boolean declared, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        getField(clazz, declared, fieldName).set(instance, value);
    }

    /**
     * Sets the value of a field of a desired class of an object
     *
     * @param instance Target object
     * @param className Name of the desired target class
     * @param packageType Package where the desired target class is located
     * @param declared Whether the desired field is declared or not
     * @param fieldName Name of the desired field
     * @param value New value
     * @throws IllegalArgumentException If the type of the value does not match the type of the desired field
     * @throws IllegalAccessException If the desired field cannot be accessed
     * @throws NoSuchFieldException If the desired field of the desired class cannot be found
     * @throws SecurityException If the desired field cannot be made accessible
     * @throws ClassNotFoundException If the desired target class with the specified name and package cannot be found
     * @see #setValue(Object, Class, boolean, String, Object)
     */
    public static void setValue(Object instance, String className, PackageType packageType, boolean declared, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        setValue(instance, packageType.getClass(className), declared, fieldName, value);
    }

    /**
     * Sets the value of a field with the given name of an object
     *
     * @param instance Target object
     * @param declared Whether the desired field is declared or not
     * @param fieldName Name of the desired field
     * @param value New value
     * @throws IllegalArgumentException If the type of the value does not match the type of the desired field
     * @throws IllegalAccessException If the desired field cannot be accessed
     * @throws NoSuchFieldException If the desired field of the target object cannot be found
     * @throws SecurityException If the desired field cannot be made accessible
     * @see #setValue(Object, Class, boolean, String, Object)
     */
    public static void setValue(Object instance, boolean declared, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        setValue(instance, instance.getClass(), declared, fieldName, value);
    }

    public enum PackageType {
        MINECRAFT_SERVER("net.minecraft.server." + getServerVersion()),
        CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()),
        CRAFTBUKKIT_BLOCK(CRAFTBUKKIT, "block"),
        CRAFTBUKKIT_CHUNKIO(CRAFTBUKKIT, "chunkio"),
        CRAFTBUKKIT_COMMAND(CRAFTBUKKIT, "command"),
        CRAFTBUKKIT_CONVERSATIONS(CRAFTBUKKIT, "conversations"),
        CRAFTBUKKIT_ENCHANTMENS(CRAFTBUKKIT, "enchantments"),
        CRAFTBUKKIT_ENTITY(CRAFTBUKKIT, "entity"),
        CRAFTBUKKIT_EVENT(CRAFTBUKKIT, "event"),
        CRAFTBUKKIT_GENERATOR(CRAFTBUKKIT, "generator"),
        CRAFTBUKKIT_HELP(CRAFTBUKKIT, "help"),
        CRAFTBUKKIT_INVENTORY(CRAFTBUKKIT, "inventory"),
        CRAFTBUKKIT_MAP(CRAFTBUKKIT, "map"),
        CRAFTBUKKIT_METADATA(CRAFTBUKKIT, "metadata"),
        CRAFTBUKKIT_POTION(CRAFTBUKKIT, "potion"),
        CRAFTBUKKIT_PROJECTILES(CRAFTBUKKIT, "projectiles"),
        CRAFTBUKKIT_SCHEDULER(CRAFTBUKKIT, "scheduler"),
        CRAFTBUKKIT_SCOREBOARD(CRAFTBUKKIT, "scoreboard"),
        CRAFTBUKKIT_UPDATER(CRAFTBUKKIT, "updater"),
        CRAFTBUKKIT_UTIL(CRAFTBUKKIT, "util");

        private final String path;

        PackageType(String path) {
            this.path = path;
        }

        PackageType(PackageType parent, String path) {
            this(parent + "." + path);
        }

        public String getPath() {
            return path;
        }

        public Class<?> getClass(String className) throws ClassNotFoundException {
            return Class.forName(this + "." + className);
        }

        @Override
        public String toString() {
            return path;
        }

        public static String getServerVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().substring(23);
        }
    }

    public enum DataType {
        BYTE(byte.class, Byte.class),
        SHORT(short.class, Short.class),
        INTEGER(int.class, Integer.class),
        LONG(long.class, Long.class),
        CHARACTER(char.class, Character.class),
        FLOAT(float.class, Float.class),
        DOUBLE(double.class, Double.class),
        BOOLEAN(boolean.class, Boolean.class);

        private static final Map<Class<?>, DataType> CLASS_MAP = new HashMap<>();
        private final Class<?> primitive;
        private final Class<?> reference;

        static {
            for (DataType type : values()) {
                CLASS_MAP.put(type.primitive, type);
                CLASS_MAP.put(type.reference, type);
            }
        }

        DataType(Class<?> primitive, Class<?> reference) {
            this.primitive = primitive;
            this.reference = reference;
        }

        public Class<?> getPrimitive() {
            return primitive;
        }

        public Class<?> getReference() {
            return reference;
        }

        public static DataType fromClass(Class<?> clazz) {
            return CLASS_MAP.get(clazz);
        }

        public static Class<?> getPrimitive(Class<?> clazz) {
            DataType type = fromClass(clazz);
            return type == null ? clazz : type.getPrimitive();
        }

        public static Class<?> getReference(Class<?> clazz) {
            DataType type = fromClass(clazz);
            return type == null ? clazz : type.getReference();
        }

        public static Class<?>[] getPrimitive(Class<?>[] classes) {
            int length = classes == null ? 0 : classes.length;
            Class<?>[] types = new Class<?>[length];
            for (int index = 0; index < length; index++) {
                types[index] = getPrimitive(classes[index]);
            }
            return types;
        }

        public static Class<?>[] getReference(Class<?>[] classes) {
            int length = classes == null ? 0 : classes.length;
            Class<?>[] types = new Class<?>[length];
            for (int index = 0; index < length; index++) {
                types[index] = getReference(classes[index]);
            }
            return types;
        }

        public static Class<?>[] getPrimitive(Object[] objects) {
            int length = objects == null ? 0 : objects.length;
            Class<?>[] types = new Class<?>[length];
            for (int index = 0; index < length; index++) {
                types[index] = getPrimitive(objects[index].getClass());
            }
            return types;
        }

        public static Class<?>[] getReference(Object[] objects) {
            int length = objects == null ? 0 : objects.length;
            Class<?>[] types = new Class<?>[length];
            for (int index = 0; index < length; index++) {
                types[index] = getReference(objects[index].getClass());
            }
            return types;
        }

        public static boolean notCompared(Class<?>[] primary, Class<?>[] secondary) {
            if (primary == null || secondary == null || primary.length != secondary.length) {
                return true;
            }
            for (int index = 0; index < primary.length; index++) {
                Class<?> primaryClass = primary[index];
                Class<?> secondaryClass = secondary[index];
                if (primaryClass.equals(secondaryClass) || primaryClass.isAssignableFrom(secondaryClass)) {
                    continue;
                }
                return true;
            }
            return false;
        }
    }
}