package org.atom.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.atom.asm.transformers.ModInventoryTransformer;

import javax.annotation.Nullable;
import java.util.Map;

public class AtomCorePlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                ModInventoryTransformer.class.getCanonicalName()
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
