package org.atom.runner;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.WorldServer;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(MinecraftServerRunner.class)
public class BaseTest {

    private MinecraftServer minecraftServer = MinecraftServer.getMinecraftServer();

    protected MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

    protected EntityPlayerMP createNewPlayer(WorldServer worldServer) {
        GameProfile playerGameProfile = new GameProfile(UUID.randomUUID(), "SomePlayer");
        PlayerInteractionManager playerInteractionManager = new PlayerInteractionManager(worldServer);
        EntityPlayerMP entityPlayerMP = new EntityPlayerMP(minecraftServer, worldServer, playerGameProfile, playerInteractionManager);
        NetworkManager serverNetworkManager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
        entityPlayerMP.connection = new NetHandlerPlayServer(minecraftServer, serverNetworkManager, entityPlayerMP);
        return entityPlayerMP;
    }

    @Test
    public void test() {
        System.out.println("It works!");
    }
}
