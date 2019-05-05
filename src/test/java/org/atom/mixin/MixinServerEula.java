package org.atom.mixin;

import net.minecraft.server.ServerEula;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;

@Mixin(ServerEula.class)
public class MixinServerEula {

    /**
     * @author AtomicInteger
     * @reason Automatically set eula flag to true, so the server can be launched properly.
     */
    @Overwrite(remap = false)
    private boolean loadEULAFile(File inFile) {
        return true;
    }
}
