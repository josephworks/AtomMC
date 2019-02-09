package net.minecraft.world.chunk.storage;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RegionFileCache
{
    private static final Map<File, RegionFile> REGIONS_BY_FILE = Maps.<File, RegionFile>newHashMap();

    public static synchronized RegionFile createOrLoadRegionFile(File worldDir, int chunkX, int chunkZ)
    {
        File file1 = new File(worldDir, "region");
        File file2 = new File(file1, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
        RegionFile regionfile = REGIONS_BY_FILE.get(file2);

        if (regionfile != null)
        {
            return regionfile;
        }
        else
        {
            if (!file1.exists())
            {
                file1.mkdirs();
            }

            if (REGIONS_BY_FILE.size() >= 256)
            {
                clearRegionFileReferences();
            }

            RegionFile regionfile1 = new RegionFile(file2);
            REGIONS_BY_FILE.put(file2, regionfile1);
            return regionfile1;
        }
    }

    public static synchronized RegionFile getRegionFileIfExists(File worldDir, int chunkX, int chunkZ)
    {
        File file1 = new File(worldDir, "region");
        File file2 = new File(file1, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca");
        RegionFile regionfile = REGIONS_BY_FILE.get(file2);

        if (regionfile != null)
        {
            return regionfile;
        }
        else if (file1.exists() && file2.exists())
        {
            if (REGIONS_BY_FILE.size() >= 256)
            {
                clearRegionFileReferences();
            }

            RegionFile regionfile1 = new RegionFile(file2);
            REGIONS_BY_FILE.put(file2, regionfile1);
            return regionfile1;
        }
        else
        {
            return null;
        }
    }

    public static synchronized void clearRegionFileReferences()
    {
        for (RegionFile regionfile : REGIONS_BY_FILE.values())
        {
            try
            {
                if (regionfile != null)
                {
                    regionfile.close();
                }
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        }

        REGIONS_BY_FILE.clear();
    }

    public static DataInputStream getChunkInputStream(File worldDir, int chunkX, int chunkZ)
    {
        RegionFile regionfile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
        return regionfile.getChunkDataInputStream(chunkX & 31, chunkZ & 31);
    }

    @Nullable
    public static synchronized NBTTagCompound getChunkInputStreamCB(File worldDir, int chunkX, int chunkZ) throws IOException
    {
        RegionFile regionfile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
        DataInputStream datainputstream = regionfile.getChunkDataInputStream(chunkX & 31, chunkZ & 31);

        if (datainputstream == null) {
            return null;
        }

        return CompressedStreamTools.read(datainputstream);
    }

    public static DataOutputStream getChunkOutputStream(File worldDir, int chunkX, int chunkZ)
    {
        RegionFile regionfile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
        return regionfile.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
    }

    public static synchronized void getChunkOutputStream(File worldDir, int chunkX, int chunkZ, NBTTagCompound nbttagcompound) throws IOException
    {
        RegionFile regionfile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
        DataOutputStream dataoutputstream = regionfile.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
        CompressedStreamTools.write(nbttagcompound, dataoutputstream);
        dataoutputstream.close();
    }

    public static boolean chunkExists(File worldDir, int chunkX, int chunkZ)
    {
        RegionFile regionfile = getRegionFileIfExists(worldDir, chunkX, chunkZ);
        return regionfile != null ? regionfile.isChunkSaved(chunkX & 31, chunkZ & 31) : false;
    }
}