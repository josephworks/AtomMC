package org.atom.tests.net.minecraft.entity.passive;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.atom.tests.net.minecraft.entity.EntityTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EntityVillagerTest extends EntityTest {

    @Test
    public void struckEntityVillagerByRealLightning() {
        MinecraftServer minecraftServer = getMinecraftServer();
        WorldServer worldServer = minecraftServer.getWorld(0);
        EntityVillager entityVillager = new EntityVillager(worldServer);
        EntityLightningBolt entityLightningBolt = new EntityLightningBolt(
                worldServer,
                entityVillager.getPosition().getX(),
                entityVillager.getPosition().getY(),
                entityVillager.getPosition().getZ(),
                false
        );
        entityVillager.hurtResistantTime = 0;
        assertTrue(!entityVillager.isDead);
        entityVillager.onStruckByLightning(entityLightningBolt);
        assertTrue(entityVillager.isDead);
    }

    @Test
    public void struckEntityVillagerByNullLightning() {
        MinecraftServer minecraftServer = getMinecraftServer();
        WorldServer worldServer = minecraftServer.getWorld(0);
        EntityVillager entityVillager = new EntityVillager(worldServer);
        entityVillager.hurtResistantTime = 0;
        assertTrue(!entityVillager.isDead);
        entityVillager.onStruckByLightning(null);
        assertTrue(entityVillager.isDead);
    }
}
