package net.minecraft.network.play.client;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketSeenAdvancements implements Packet<INetHandlerPlayServer>
{
    private Action action;
    private ResourceLocation tab;

    public CPacketSeenAdvancements()
    {
    }

    @SideOnly(Side.CLIENT)
    public CPacketSeenAdvancements(Action p_i47595_1_, @Nullable ResourceLocation p_i47595_2_)
    {
        this.action = p_i47595_1_;
        this.tab = p_i47595_2_;
    }

    @SideOnly(Side.CLIENT)
    public static CPacketSeenAdvancements openedTab(Advancement p_194163_0_)
    {
        return new CPacketSeenAdvancements(Action.OPENED_TAB, p_194163_0_.getId());
    }

    @SideOnly(Side.CLIENT)
    public static CPacketSeenAdvancements closedScreen()
    {
        return new CPacketSeenAdvancements(Action.CLOSED_SCREEN, (ResourceLocation)null);
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.action = (Action)buf.readEnumValue(Action.class);

        if (this.action == Action.OPENED_TAB)
        {
            this.tab = buf.readResourceLocation();
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeEnumValue(this.action);

        if (this.action == Action.OPENED_TAB)
        {
            buf.writeResourceLocation(this.tab);
        }
    }

    public void processPacket(INetHandlerPlayServer handler)
    {
        handler.handleSeenAdvancements(this);
    }

    public Action getAction()
    {
        return this.action;
    }

    public ResourceLocation getTab()
    {
        return this.tab;
    }

    public static enum Action
    {
        OPENED_TAB,
        CLOSED_SCREEN;
    }
}