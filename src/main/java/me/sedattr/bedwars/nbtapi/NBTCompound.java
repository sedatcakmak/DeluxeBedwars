package me.sedattr.bedwars.nbtapi;

import me.sedattr.bedwars.nbtapi.utils.MinecraftVersion;
import me.sedattr.bedwars.nbtapi.utils.nmsmappings.ReflectionMethod;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NBTCompound {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private final String compundName;
    private final NBTCompound parent;

    protected NBTCompound(NBTCompound owner, String name) {
        this.compundName = name;
        this.parent = owner;
    }

    protected static boolean isEqual(NBTCompound compA, NBTCompound compB, String key) {
        if (compA.getType(key) != compB.getType(key)) return false;
        switch (compA.getType(key)) {
            case NBTTagByte:
                return compA.getByte(key).equals(compB.getByte(key));
            case NBTTagByteArray:
                return Arrays.equals(compA.getByteArray(key), compB.getByteArray(key));
            case NBTTagCompound:
                return compA.getCompound(key).equals(compB.getCompound(key));
            case NBTTagDouble:
                return compA.getDouble(key).equals(compB.getDouble(key));
            case NBTTagEnd:
                return true; //??
            case NBTTagFloat:
                return compA.getFloat(key).equals(compB.getFloat(key));
            case NBTTagInt:
                return compA.getInteger(key).equals(compB.getInteger(key));
            case NBTTagIntArray:
                return Arrays.equals(compA.getIntArray(key), compB.getIntArray(key));
            case NBTTagList:
                return NBTReflectionUtil.getEntry(compA, key).toString().equals(NBTReflectionUtil.getEntry(compB, key).toString()); // Just string compare the 2 lists
            case NBTTagLong:
                return compA.getLong(key).equals(compB.getLong(key));
            case NBTTagShort:
                return compA.getShort(key).equals(compB.getShort(key));
            case NBTTagString:
                return compA.getString(key).equals(compB.getString(key));
        }
        return false;
    }

    protected Lock getReadLock() {
        return readLock;
    }

    protected Lock getWriteLock() {
        return writeLock;
    }

    protected void saveCompound() {
        if (parent != null)
            parent.saveCompound();
    }

    public String getName() {
        return compundName;
    }

    public Object getCompound() {
        return parent.getCompound();
    }

    protected void setCompound(Object compound) {
        parent.setCompound(compound);
    }

    public NBTCompound getParent() {
        return parent;
    }

    public void mergeCompound(NBTCompound comp) {
        try {
            writeLock.lock();
            NBTReflectionUtil.mergeOtherNBTCompound(this, comp);
            saveCompound();
        } finally {
            writeLock.unlock();
        }
    }

    public void setString(String key, String value) {
        try {
            writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_STRING, key, value);
            saveCompound();
        } finally {
            writeLock.unlock();
        }
    }

    public String getString(String key) {
        try {
            readLock.lock();
            return (String) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_STRING, key);
        } finally {
            readLock.unlock();
        }
    }

    public Integer getInteger(String key) {
        try {
            readLock.lock();
            return (Integer) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_INT, key);
        } finally {
            readLock.unlock();
        }
    }

    public Double getDouble(String key) {
        try {
            readLock.lock();
            return (Double) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_DOUBLE, key);
        } finally {
            readLock.unlock();
        }
    }

    public Byte getByte(String key) {
        try {
            readLock.lock();
            return (Byte) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BYTE, key);
        } finally {
            readLock.unlock();
        }
    }

    public Short getShort(String key) {
        try {
            readLock.lock();
            return (Short) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_SHORT, key);
        } finally {
            readLock.unlock();
        }
    }

    public Long getLong(String key) {
        try {
            readLock.lock();
            return (Long) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_LONG, key);
        } finally {
            readLock.unlock();
        }
    }

    public Float getFloat(String key) {
        try {
            readLock.lock();
            return (Float) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_FLOAT, key);
        } finally {
            readLock.unlock();
        }
    }

    public byte[] getByteArray(String key) {
        try {
            readLock.lock();
            return (byte[]) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BYTEARRAY, key);
        } finally {
            readLock.unlock();
        }
    }

    public int[] getIntArray(String key) {
        try {
            readLock.lock();
            return (int[]) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_INTARRAY, key);
        } finally {
            readLock.unlock();
        }
    }

    protected void set(String key, Object val) {
        NBTReflectionUtil.set(this, key, val);
        saveCompound();
    }

    public Boolean getBoolean(String key) {
        try {
            readLock.lock();
            return (Boolean) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BOOLEAN, key);
        } finally {
            readLock.unlock();
        }
    }

    public UUID getUUID(String key) {
        try {
            readLock.lock();
            return (UUID) NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_UUID, key);
        } finally {
            readLock.unlock();
        }
    }

    public Set<String> getKeys() {
        try {
            readLock.lock();
            return NBTReflectionUtil.getKeys(this);
        } finally {
            readLock.unlock();
        }
    }

    public NBTCompound getCompound(String name) {
        try {
            readLock.lock();
            if (getType(name) != NBTType.NBTTagCompound)
                return null;
            NBTCompound next = new NBTCompound(this, name);
            if (NBTReflectionUtil.valideCompound(next))
                return next;
            return null;
        } finally {
            readLock.unlock();
        }
    }

    public NBTList<String> getStringList() {
        try {
            writeLock.lock();
            NBTList<String> list = NBTReflectionUtil.getList(this, NBTType.NBTTagString);
            saveCompound();
            return list;
        } finally {
            writeLock.unlock();
        }
    }

    public NBTList<Double> getDoubleList() {
        try {
            writeLock.lock();
            NBTList<Double> list = NBTReflectionUtil.getList(this, NBTType.NBTTagDouble);
            saveCompound();
            return list;
        } finally {
            writeLock.unlock();
        }
    }

    public NBTType getListType(String name) {
        try {
            readLock.lock();
            if (getType(name) != NBTType.NBTTagList)
                return null;
            return NBTReflectionUtil.getListType(this, name);
        } finally {
            readLock.unlock();
        }
    }

    public NBTType getType(String name) {
        try {
            readLock.lock();
            if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
                Object nbtbase = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET, name);
                if (nbtbase == null)
                    return null;
                return NBTType.valueOf((byte) ReflectionMethod.COMPOUND_OWN_TYPE.run(nbtbase));
            }
            Object o = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_TYPE, name);
            if (o == null)
                return null;
            return NBTType.valueOf((byte) o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        return asNBTString();
    }

    @Deprecated
    public String asNBTString() {
        try {
            readLock.lock();
            Object comp = NBTReflectionUtil.gettoCompount(getCompound(), this);
            if (comp == null)
                return "{}";
            return comp.toString();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof NBTCompound) {
            NBTCompound other = (NBTCompound) obj;
            if (getKeys().equals(other.getKeys())) {
                for (String key : getKeys()) {
                    return isEqual(this, other, key);
                }
            }
        }
        return false;
    }
}
