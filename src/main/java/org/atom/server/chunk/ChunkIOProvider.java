package org.atom.server.chunk;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkIOProvider  implements AsynchronousExecutor.CallBackProvider<QueuedChunk, Chunk, Runnable, RuntimeException> {
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    // async stuff
    public Chunk callStage1(QueuedChunk queuedChunk) throws RuntimeException
    {
        AnvilChunkLoader loader = queuedChunk.loader;
        Object[] data = new Object[0];
        try {
            data = loader.loadChunk__Async(queuedChunk.world, ChunkHash.keyToX(queuedChunk.coords), ChunkHash.keyToZ(queuedChunk.coords));
        } catch (IOException e) {
            throw new RuntimeException(e); // Allow exception to bubble up to afterExecute
        }

        if(data != null)
        {
            queuedChunk.compound = (net.minecraft.nbt.NBTTagCompound) data[1];
            return (Chunk) data[0];
        }

        return null;
    }

    // sync stuff
    public void callStage2(QueuedChunk queuedChunk, Chunk chunk) throws RuntimeException
    {
        if(chunk == null)
        {
            // If the chunk loading failed just do it synchronously (may
            // generate)
            queuedChunk.provider.loadChunk(ChunkHash.keyToX(queuedChunk.coords), ChunkHash.keyToZ(queuedChunk.coords));
            return;
        }

        int x = ChunkHash.keyToX(queuedChunk.coords);
        int z = ChunkHash.keyToZ(queuedChunk.coords);

        // See if someone already loaded this chunk while we were working on it
        // (API, etc)
        if(queuedChunk.provider.id2ChunkMap.contains(x,z))
        {
            // Make sure it isn't queued for unload, we need it
            queuedChunk.provider.droppedChunksSet.remove(queuedChunk.coords); // Spigot
            return;
        }

        queuedChunk.loader.loadEntities(queuedChunk.world, queuedChunk.compound.getCompoundTag("Level"), chunk);
        MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, queuedChunk.compound)); // MCPC+
        // -
        // Don't
        // call
        // ChunkDataEvent.Load
        // async
        chunk.setLastSaveTime(queuedChunk.provider.world.getTotalWorldTime());
        queuedChunk.provider.id2ChunkMap.put(queuedChunk.coords, chunk);
        chunk.onLoad();

        if(queuedChunk.provider.chunkGenerator != null)
        {
            queuedChunk.provider.chunkGenerator.recreateStructures(chunk, x, z);
        }

        chunk.populateCB(queuedChunk.provider, queuedChunk.provider.chunkGenerator, false);
        chunk.onTick(false);
    }

    public void callStage3(QueuedChunk queuedChunk, Chunk chunk, Runnable runnable) throws RuntimeException
    {
        runnable.run();
    }

    public Thread newThread(Runnable runnable)
    {
        Thread thread = new Thread(runnable, "Chunk I/O Executor Thread-" + threadNumber.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
