package net.minecraft.network.handshake.client;

import java.io.IOException;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class C00Handshake implements Packet<INetHandlerHandshakeServer> {
    private int protocolVersion;
    public String ip;
    public int port;
    private EnumConnectionState requestedState;
    private boolean hasFMLMarker = false;

    public C00Handshake() {
    }

    @SideOnly(Side.CLIENT)
    public C00Handshake(String p_i47613_1_, int p_i47613_2_, EnumConnectionState p_i47613_3_) {
        this.protocolVersion = 340;
        this.ip = p_i47613_1_;
        this.port = p_i47613_2_;
        this.requestedState = p_i47613_3_;
    }

    public C00Handshake(String address, int port, EnumConnectionState state, boolean addFMLMarker) {
        this(address, port, state);
        this.hasFMLMarker = addFMLMarker;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        this.protocolVersion = buf.readVarInt();

        //Sponge start
        if (!org.spigotmc.SpigotConfig.bungee) {
            this.ip = buf.readString(255);  // Spigot
        } else {
            this.ip = buf.readString(Short.MAX_VALUE);
            String[] split = this.ip.split("\0\\|", 2);
            this.ip = split[0];
            // If we have extra data, check to see if it is telling us we have a
            // FML marker
            if (split.length == 2) {
                this.hasFMLMarker = split[1].contains("\0FML\0");
            }
        }

        // Check for FML marker and strip if found, but only if it wasn't
        // already in the extra data.
        if (!this.hasFMLMarker) {
            this.hasFMLMarker = this.ip.contains("\0FML\0");
            if (this.hasFMLMarker) {
                this.ip = this.ip.split("\0")[0];
            }
        }
        //Sponge end

        this.port = buf.readUnsignedShort();
        this.requestedState = EnumConnectionState.getById(buf.readVarInt());
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(this.protocolVersion);
        buf.writeString(this.ip + "\0FML\0");
        buf.writeShort(this.port);
        buf.writeVarInt(this.requestedState.getId());
    }

    public void processPacket(INetHandlerHandshakeServer handler) {
        handler.processHandshake(this);
    }

    public EnumConnectionState getRequestedState() {
        return this.requestedState;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    public boolean hasFMLMarker() {
        return this.hasFMLMarker;
    }
}