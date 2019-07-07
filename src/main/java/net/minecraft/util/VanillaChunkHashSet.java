package net.minecraft.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.util.math.ChunkPos;
import org.atom.server.chunk.ChunkHash;

import com.google.common.collect.Iterators;

import net.openhft.koloboke.collect.set.IntSet;

public class VanillaChunkHashSet implements Set<Long> {

    private final IntSet intset;

    public VanillaChunkHashSet(IntSet intset) {
        this.intset = intset;
    }
    private static int v2um(long key)
    {
        return ChunkHash.chunkToKey((int) (key & 0xFFFFFFFFL), (int) (key >>> 32));
    }

    private static long um2v(int key)
    {
        return ChunkPos.asLong(ChunkHash.keyToX(key), ChunkHash.keyToZ(key));
    }
    @Override
    public int size()
    {
        return intset.size();
    }

    @Override
    public boolean isEmpty()
    {
        return intset.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return o instanceof Long && contains((long)o);
    }

    public boolean contains(long l)
    {
        return intset.contains(v2um(l));
    }

    @Override
    @SuppressWarnings("deprecation")
    public Iterator<Long> iterator()
    {
        return Iterators.transform(intset.iterator(), (Integer key) -> um2v(key));
    }

    @Override
    public Object[] toArray()
    {
        return toArray(new Long[size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a)
    {
        int size = size();
        Long[] r = a.length >= size() ? (Long[]) a : (Long[]) Array.newInstance(a.getClass().getComponentType(), size);
        int i = 0;
        for(Long l : this)
            r[i++] = l;
        return (T[]) r;
    }

    @Override
    public boolean add(Long e)
    {
        return intset.add(v2um(e));
    }

    @Override
    public boolean remove(Object o)
    {
        return o instanceof Long && remove((long)o);
    }

    public boolean remove(long l)
    {
        return intset.removeInt(v2um(l));
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for(Object o : c)
            if(!contains(o))
                return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Long> c)
    {
        boolean modified = false;
        for(Object o : c)
            modified |= add((Long)o);
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean modified = false;
        for(Iterator<Long> it = iterator(); it.hasNext();)
        {
            Long l = it.next();
            if(!c.contains(l))
            {
                it.remove();
                modified = true;
            }
        }

        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        boolean modified = false;
        for(Object o : c)
            modified |= remove(o);
        return modified;
    }

    @Override
    public void clear()
    {
        intset.clear();
    }
}
