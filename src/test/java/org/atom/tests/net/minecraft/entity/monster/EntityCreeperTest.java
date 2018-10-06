package org.atom.tests.net.minecraft.entity.monster;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.atom.tests.net.minecraft.entity.EntityTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EntityCreeperTest extends EntityTest {

    @Test
    public void struckEntityCreeperByRealLightning() {
        MinecraftServer minecraftServer = getMinecraftServer();
        WorldServer worldServer = minecraftServer.getWorld(0);
        EntityCreeper entityCreeper = new EntityCreeper(worldServer);
        assertTrue(!entityCreeper.getPowered());
        EntityLightningBolt entityLightningBolt = new EntityLightningBolt(
                worldServer,
                entityCreeper.getPosition().getX(),
                entityCreeper.getPosition().getY(),
                entityCreeper.getPosition().getZ(),
                false
        );
        entityCreeper.hurtResistantTime = 0;
        entityCreeper.onStruckByLightning(entityLightningBolt);
        assertTrue(entityCreeper.getPowered());
    }

    @Test
    public void struckEntityCreeperByNullLightning() {
        MinecraftServer minecraftServer = getMinecraftServer();
        WorldServer worldServer = minecraftServer.getWorld(0);
        EntityCreeper entityCreeper = new EntityCreeper(worldServer);
        assertTrue(!entityCreeper.getPowered());
        entityCreeper.hurtResistantTime = 0;
        entityCreeper.onStruckByLightning(null);
        assertTrue(entityCreeper.getPowered());
    }
}
