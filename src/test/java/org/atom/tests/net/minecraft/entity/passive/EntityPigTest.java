package org.atom.tests.net.minecraft.entity.passive;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.atom.tests.net.minecraft.entity.EntityTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntityPigTest extends EntityTest {

    @Test
    public void struckEntityPigByRealLightning() {
        MinecraftServer minecraftServer = getMinecraftServer();
        WorldServer worldServer = minecraftServer.getWorld(0);
        EntityPig entityPig = new EntityPig(worldServer);
        EntityLightningBolt entityLightningBolt = new EntityLightningBolt(
                worldServer,
                entityPig.getPosition().getX(),
                entityPig.getPosition().getY(),
                entityPig.getPosition().getZ(),
                false
        );
        entityPig.hurtResistantTime = 0;
        assertEquals(entityPig.getHealth(), 10.0, 0.1);
        entityPig.onStruckByLightning(entityLightningBolt);
        assertTrue(entityPig.isDead);
    }

    @Test
    public void struckEntityPigByNullLightning() {
        MinecraftServer minecraftServer = getMinecraftServer();
        WorldServer worldServer = minecraftServer.getWorld(0);
        EntityPig entityPig = new EntityPig(worldServer);
        entityPig.hurtResistantTime = 0;
        assertEquals(entityPig.getHealth(), 10.0, 0.1);
        entityPig.onStruckByLightning(null);
        assertTrue(entityPig.isDead);
    }
}
