package org.atom.tests.net.minecraft.entity;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.atom.runner.BaseTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityTest extends BaseTest {

    @Test
    public void struckByRealLightning() {
        MinecraftServer minecraftServer = getMinecraftServer();
        WorldServer worldServer = minecraftServer.getWorld(0);
        EntityPlayerMP entityPlayerMP = createNewPlayer(worldServer);
        assertEquals(entityPlayerMP.getHealth(), 20.0, 0.1);
        EntityLightningBolt entityLightningBolt = new EntityLightningBolt(
                worldServer,
                entityPlayerMP.getPosition().getX(),
                entityPlayerMP.getPosition().getY(),
                entityPlayerMP.getPosition().getZ(),
                false
        );
        entityPlayerMP.hurtResistantTime = 0;
        entityPlayerMP.respawnInvulnerabilityTicks = 0;
        entityPlayerMP.onStruckByLightning(entityLightningBolt);
        assertEquals(entityPlayerMP.getHealth(), 15.0, 0.1);
    }

    @Test
    public void struckByNullLightning() {
        MinecraftServer minecraftServer = getMinecraftServer();
        WorldServer worldServer = minecraftServer.getWorld(0);
        EntityPlayerMP entityPlayerMP = createNewPlayer(worldServer);
        assertEquals(entityPlayerMP.getHealth(), 20.0, 0.1);
        entityPlayerMP.hurtResistantTime = 0;
        entityPlayerMP.respawnInvulnerabilityTicks = 0;
        entityPlayerMP.onStruckByLightning(null);
        assertEquals(entityPlayerMP.getHealth(), 15.0, 0.1);
    }
}
