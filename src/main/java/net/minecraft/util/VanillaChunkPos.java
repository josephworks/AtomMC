package net.minecraft.util;

import com.google.common.collect.Iterators;
import net.minecraft.util.math.ChunkPos;
import net.openhft.koloboke.collect.set.IntSet;
import org.atom.server.chunk.ChunkHash;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class VanillaChunkPos implements Set<ChunkPos> {

    private final IntSet intset;

    public VanillaChunkPos(IntSet intset)
    {
        this.intset = intset;
    }

    private static int v2um(ChunkPos coord)
    {
        return ChunkHash.chunkToKey(coord.x, coord.z);
    }

    private static ChunkPos um2v(int key)
    {
        return new ChunkPos(ChunkHash.keyToX(key), ChunkHash.keyToZ(key));
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
        return o instanceof ChunkPos && contains((ChunkPos)o);
    }

    public boolean contains(ChunkPos coord)
    {
        return intset.contains(v2um(coord));
    }

    @Override
    @SuppressWarnings("deprecation")
    public Iterator<ChunkPos> iterator()
    {
        return Iterators.transform(intset.iterator(), (Integer key) -> um2v(key));
    }

    @Override
    public Object[] toArray()
    {
        return toArray(new ChunkPos[size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a)
    {
        int size = size();
        ChunkPos[] r = a.length >= size() ? (ChunkPos[]) a : (ChunkPos[]) Array.newInstance(a.getClass().getComponentType(), size);
        int i = 0;
        for(ChunkPos coord : this)
            r[i++] = coord;
        return (T[]) r;
    }

    @Override
    public boolean add(ChunkPos e)
    {
        return intset.add(v2um(e));
    }

    @Override
    public boolean remove(Object o)
    {
        return o instanceof ChunkPos && remove((ChunkPos)o);
    }

    public boolean remove(ChunkPos coord)
    {
        return intset.removeInt(v2um(coord));
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
    public boolean addAll(Collection<? extends ChunkPos> c)
    {
        boolean modified = false;
        for(Object o : c)
            modified |= add((ChunkPos)o);
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean modified = false;
        for(Iterator<ChunkPos> it = iterator(); it.hasNext();)
        {
            ChunkPos coord = it.next();
            if(!c.contains(coord))
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
