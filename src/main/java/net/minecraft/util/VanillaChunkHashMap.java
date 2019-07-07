package net.minecraft.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.chunk.Chunk;
import org.atom.server.chunk.ChunkMap;

public class VanillaChunkHashMap<V> extends Long2ObjectOpenHashMap {

    private final ChunkMap chunkMap;

    public VanillaChunkHashMap(ChunkMap chunkMap)
    {
        this.chunkMap = chunkMap;
    }

    private static int v2x(long key)
    {
        return (int) (key & 0xFFFFFFFFL);
    }

    private static int v2z(long key)
    {
        return (int) (key >>> 32);
    }

    @Override
    public int size()
    {
        return chunkMap.size();
    }

    @Override
    public V get(long key)
    {
        return (V) chunkMap.get(v2x(key), v2z(key));
    }

    @Override
    public boolean containsKey(long key)
    {
        return chunkMap.contains(v2x(key), v2z(key));
    }

    @Override
    public Object put(long key, Object obj)
    {
        chunkMap.put(v2x(key), v2z(key), (Chunk)obj);
        return obj;
    }

    @Override
    public Object remove(long key)
    {
        return chunkMap.remove(v2x(key), v2z(key));
    }

}
