package org.atom;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.Map;

@IFMLLoadingPlugin.Name("AtomServerCore")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class AtomServerCore implements IFMLLoadingPlugin {


    public AtomServerCore(){
        System.setProperty("mixin.debug.export", "true");
        MixinBootstrap.init();
        LogManager.getLogger().warn(" ");
        LogManager.getLogger().warn("[AtomMC] Trying to apply class transformers.");
        LogManager.getLogger().warn("    - If you got any errors contact the developer immediately.");
        LogManager.getLogger().warn(" ");

        try{
            //loader.loadPatches();
        }catch (Exception e) {
            /*LogManager.getLogger().warn(" ");
            LogManager.getLogger().warn("[AtomMC] An error occurred while trying to load class transformers.");
            LogManager.getLogger().warn(" ");
            e.printStackTrace();*/
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

    public static String getNativeVersion() {
        return "v1_12_R1";
    }
}
