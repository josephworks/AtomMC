package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.VanillaChunkHashMap;
import net.minecraft.util.VanillaChunkHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.openhft.koloboke.collect.IntIterator;
import net.openhft.koloboke.collect.set.IntSet;
import net.openhft.koloboke.collect.set.hash.HashIntSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atom.server.chunk.ChunkHash;
import org.atom.server.chunk.ChunkIOExecutor;
import org.atom.server.chunk.ChunkMap;
import org.bukkit.event.world.ChunkUnloadEvent;

// TODO: This class needs serious testing.
public class ChunkProviderServer implements IChunkProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    public final IntSet unloadQueue = HashIntSets.newMutableSet();
    public final Set<Long> droppedChunksSet = new VanillaChunkHashSet(unloadQueue); // mods compatibility
    public final IChunkGenerator chunkGenerator;
    public final IChunkLoader chunkLoader;
    public final ChunkMap chunkMap = new ChunkMap();
    public final Long2ObjectMap<Chunk> id2ChunkMap = new VanillaChunkHashMap<Chunk>(chunkMap); // mods compatibility
    public final WorldServer world;
    private final Set<Long> loadingChunks = com.google.common.collect.Sets.newHashSet();

    public ChunkProviderServer(WorldServer worldObjIn, IChunkLoader chunkLoaderIn, IChunkGenerator chunkGeneratorIn) {
        this.world = worldObjIn;
        this.chunkLoader = chunkLoaderIn;
        this.chunkGenerator = chunkGeneratorIn;
    }

    public Collection<Chunk> getLoadedChunks() {
        return this.chunkMap.valueCollection();
    }

    public void queueUnload(Chunk chunkIn) {
        if (this.world.provider.canDropChunk(chunkIn.x, chunkIn.z)) {
            this.unloadQueue.add(ChunkHash.chunkToKey(chunkIn.x, chunkIn.z));
            chunkIn.unloadQueued = true;
        }
    }

    public void queueUnloadAll() {
        /*ObjectIterator objectiterator = this.id2ChunkMap.values().iterator();

        while (objectiterator.hasNext()) {
            Chunk chunk = (Chunk) objectiterator.next();
            this.queueUnload(chunk);
        }*/
    }

    @Nullable
    public Chunk getLoadedChunk(int x, int z) {
        Chunk chunk = (Chunk) this.chunkMap.get(x,z);

        if (chunk != null) {
            chunk.unloadQueued = false;
        }

        return chunk;
    }

    // Is it copy of method above?
    public Chunk getChunkIfLoaded(int x, int z) {
        return chunkMap.get(x, z);
    }

    @Nullable
    public Chunk loadChunk(int x, int z) {
        return loadChunk(x, z, null);
    }

    // CraftBukkit start - loadChunk method aliases
    public Chunk getChunkAt(int i, int j) {
        return getChunkAt(i, j, null);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable) {
        return getChunkAt(i, j, runnable, true);
    }

    public Chunk getChunkAt(int i, int j, Runnable runnable, boolean generate) {
        return loadChunk(i, j, runnable, generate);
    }
    // CraftBukkit end

    @Nullable
    public Chunk loadChunk(int x, int z, @Nullable Runnable runnable) {
        return loadChunk(x, z, runnable, false);
    }

    @Nullable
    public Chunk loadChunk(int x, int z, @Nullable Runnable runnable, boolean generate) {
        Chunk chunk = this.getLoadedChunk(x, z);
        if (chunk == null) {
            long pos = ChunkPos.asLong(x, z);
            chunk = net.minecraftforge.common.ForgeChunkManager.fetchDormantChunk(pos, this.world);
            if (chunk != null || !(this.chunkLoader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader)) {
                if (!loadingChunks.add(pos))
                    net.minecraftforge.fml.common.FMLLog.bigWarning("There is an attempt to load a chunk ({},{}) in dimension {} that is already being loaded. This will cause weird chunk breakages.", x, z, this.world.provider.getDimension());
                if (chunk == null) chunk = this.loadChunkFromFile(x, z);

                if (chunk != null) {
                    this.chunkMap.put(ChunkHash.chunkToKey(x, z), chunk);
                    chunk.onLoad();
                    chunk.populateCB(this, this.chunkGenerator, false);
                }

                loadingChunks.remove(pos);
            } else {
                net.minecraft.world.chunk.storage.AnvilChunkLoader loader = (net.minecraft.world.chunk.storage.AnvilChunkLoader) this.chunkLoader;
                if (runnable == null || !net.minecraftforge.common.ForgeChunkManager.asyncChunkLoading)
                    chunk = net.minecraftforge.common.chunkio.ChunkIOExecutor.syncChunkLoad(this.world, loader, this, x, z);
                else if (loader.isChunkGeneratedAt(x, z)) {
                    // We can only use the async queue for already generated chunks
                    net.minecraftforge.common.chunkio.ChunkIOExecutor.queueChunkLoad(this.world, loader, this, x, z, runnable);
                    return null;
                } else if (generate) {
                    chunk = provideChunk(x, z);
                }
            }
        }

        // If we didn't load the chunk async and have a callback run it now
        if (runnable != null) runnable.run();
        return chunk;
    }

    public Chunk provideChunk(int x, int z) {
        Chunk chunk = this.loadChunk(x, z);

        if (chunk == null) {
            world.timings.syncChunkLoadTimer.startTiming(); // Spigot
            int i = ChunkHash.chunkToKey(x, z);

            try {
                chunk = this.chunkGenerator.generateChunk(x, z);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception generating new chunk");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                crashreportcategory.addCrashSection("Location", String.format("%d,%d", x, z));
                crashreportcategory.addCrashSection("Position hash", i);
                crashreportcategory.addCrashSection("Generator", this.chunkGenerator);
                throw new ReportedException(crashreport);
            }

            this.chunkMap.put(i, chunk);
            chunk.onLoad();
            chunk.populateCB(this, this.chunkGenerator, true);
            world.timings.syncChunkLoadTimer.stopTiming(); // Spigot
            chunk.onTick(false);
        }

        return chunk;
    }

    @Nullable
    private Chunk loadChunkFromFile(int x, int z) {
        try {
            Chunk chunk = this.chunkLoader.loadChunk(this.world, x, z);

            if (chunk != null) {
                chunk.setLastSaveTime(this.world.getTotalWorldTime());
                this.chunkGenerator.recreateStructures(chunk, x, z);
            }

            return chunk;
        } catch (Exception exception) {
            LOGGER.error("Couldn't load chunk", (Throwable) exception);
            return null;
        }
    }

    private void saveChunkExtraData(Chunk chunkIn) {
        try {
            this.chunkLoader.saveExtraChunkData(this.world, chunkIn);
        } catch (Exception exception) {
            LOGGER.error("Couldn't save entities", (Throwable) exception);
        }
    }

    private void saveChunkData(Chunk chunkIn) {
        try {
            chunkIn.setLastSaveTime(this.world.getTotalWorldTime());
            this.chunkLoader.saveChunk(this.world, chunkIn);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save chunk", (Throwable) ioexception);
        } catch (MinecraftException minecraftexception) {
            LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", (Throwable) minecraftexception);
        }
    }

    public boolean saveChunks(boolean all) {
        int i = 0;
        List<Chunk> list = Lists.newArrayList(this.chunkMap.valueCollection());

        for (int j = 0; j < list.size(); ++j) {
            Chunk chunk = list.get(j);

            if (all) {
                this.saveChunkExtraData(chunk);
            }

            if (chunk.needsSaving(all)) {
                this.saveChunkData(chunk);
                chunk.setModified(false);
                ++i;

                if (i == 24 && !all) {
                    return false;
                }
            }
        }

        return true;
    }

    public void flushToDisk() {
        this.chunkLoader.flush();
    }

    public boolean tick() {
        if (!this.world.disableLevelSaving) {
            if (!this.unloadQueue.isEmpty()) {
                for (ChunkPos forced : this.world.getPersistentChunks().keySet()) {
                    this.unloadQueue.remove(ChunkHash.chunkToKey(forced.x, forced.z));
                }

                IntIterator iterator = this.unloadQueue.iterator();

                for (int i = 0; i < 100 && iterator.hasNext(); iterator.remove()) {
                    int olong = iterator.next();
                    Chunk chunk = (Chunk) this.chunkMap.get(olong);

                    if (chunk != null && chunk.unloadQueued) {
//                        chunk.onUnload();
//                        net.minecraftforge.common.ForgeChunkManager.putDormantChunk(ChunkHash.chunkToKey(chunk.x, chunk.z), chunk);
//                        this.saveChunkData(chunk);
//                        this.saveChunkExtraData(chunk);
//                        this.id2ChunkMap.remove(olong);
                        if (!unloadChunk(chunk, true)) {
                            continue;
                        }
                        ++i;
                    }
                }
            }

            /*
                MinecraftForge unloads worlds if there are no loaded chunks by default. CraftBukkit does not do this, so
                we have to disable it as well for plugin compatibility, because such plugins as MultiVerse rely on this
                mechanic.
             */
            // if (this.id2ChunkMap.isEmpty()) net.minecraftforge.common.DimensionManager.unloadWorld(this.world.provider.getDimension());

            this.chunkLoader.chunkTick();
        }

        return false;
    }

    public boolean unloadChunk(Chunk chunk, boolean save) {
        ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk, save);
        this.world.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        save = event.isSaveChunk();

        // Update neighbor counts
        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                Chunk neighbor = this.getChunkIfLoaded(chunk.x + x, chunk.z + z);
                if (neighbor != null) {
                    neighbor.setNeighborUnloaded(-x, -z);
                    chunk.setNeighborUnloaded(x, z);
                }
            }
        }
        // Moved from unloadChunks above
        chunk.onUnload();
        net.minecraftforge.common.ForgeChunkManager.putDormantChunk(ChunkHash.chunkToKey(chunk.x, chunk.z), chunk);
        this.saveChunkData(chunk);
        this.saveChunkExtraData(chunk);
        this.chunkMap.remove(ChunkHash.chunkToKey(chunk.x, chunk.z));
        return true;
    }

    public boolean canSave() {
        return !this.world.disableLevelSaving;
    }

    public String makeString() {
        return "ServerChunkCache: " + this.chunkMap.size() + " Drop: " + this.unloadQueue.size();
    }

    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.chunkGenerator.getPossibleCreatures(creatureType, pos);
    }

    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return this.chunkGenerator.getNearestStructurePos(worldIn, structureName, position, findUnexplored);
    }

    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return this.chunkGenerator.isInsideStructure(worldIn, structureName, pos);
    }

    public int getLoadedChunkCount() {
        return this.chunkMap.size();
    }

    public boolean chunkExists(int x, int z) {
        return this.chunkMap.contains(x, z);
    }

    public boolean isChunkGeneratedAt(int x, int z) {
        return this.chunkMap.contains(x, z) || this.chunkLoader.isChunkGeneratedAt(x, z);
    }

    /* ======================================== ATOMMC START =====================================*/

    public void loadAsync(int x, int z, Runnable callback) //XXX
    {
        if(chunkMap.contains(x, z))
        {
            callback.run();
            return;
        }
        else
        {
            ChunkIOExecutor.queueChunkLoad(this.world, (AnvilChunkLoader)chunkLoader, this, x, z, callback);
        }
    }
}