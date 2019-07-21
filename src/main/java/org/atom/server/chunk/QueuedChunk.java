package org.atom.server.chunk;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

public class QueuedChunk
{
    final int coords;
    final AnvilChunkLoader loader;
    final World world;
    final ChunkProviderServer provider;
    NBTTagCompound compound;

    public QueuedChunk(int coords, AnvilChunkLoader loader, World world, ChunkProviderServer provider)
    {
        this.coords = coords;
        this.loader = loader;
        this.world = world;
        this.provider = provider;
    }

    @Override
    public int hashCode()
    {
        return coords ^ (world.hashCode() << 24);
    }

    @Override
    public boolean equals(Object object)
    {
        if(object instanceof QueuedChunk)
        {
            QueuedChunk other = (QueuedChunk) object;
            return coords == other.coords && world == other.world;
        }

        return false;
    }
}
