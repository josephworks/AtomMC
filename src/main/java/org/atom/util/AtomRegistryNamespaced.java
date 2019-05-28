package org.atom.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.IObjectIntIterable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.registries.GameData;

import javax.annotation.ParametersAreNonnullByDefault;

public class AtomRegistryNamespaced extends RegistryNamespaced<ResourceLocation,Class<? extends Entity>> implements IObjectIntIterable<Class<? extends Entity>> {
    @Override
    @ParametersAreNonnullByDefault
    public void register(int id, ResourceLocation key, Class<? extends Entity> value) {
        super.register(id, key, value);
        GameData.registerEntity(id,key,value,value.getSimpleName().toLowerCase());
    }
}
