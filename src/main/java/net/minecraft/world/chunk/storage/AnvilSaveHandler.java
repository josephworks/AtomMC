package net.minecraft.world.chunk.storage;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraft.world.storage.WorldInfo;

public class AnvilSaveHandler extends SaveHandler
{
    public AnvilSaveHandler(File p_i46650_1_, String saveDirectoryName, boolean p_i46650_3_, DataFixer dataFixerIn)
    {
        super(p_i46650_1_, saveDirectoryName, p_i46650_3_, dataFixerIn);
    }

    public IChunkLoader getChunkLoader(WorldProvider provider)
    {
        return new AnvilChunkLoader(this.getWorldDirectory(), this.dataFixer);
    }

    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, @Nullable NBTTagCompound tagCompound)
    {
        worldInformation.setSaveVersion(19133);
        super.saveWorldInfoWithPlayer(worldInformation, tagCompound);
    }

    public void flush()
    {
        try
        {
            ThreadedFileIOBase.getThreadedIOInstance().waitForFinish();
        }
        catch (InterruptedException interruptedexception)
        {
            interruptedexception.printStackTrace();
        }

        RegionFileCache.clearRegionFileReferences();
    }
}