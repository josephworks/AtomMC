package net.minecraft.network.play.client;

import java.io.IOException;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketChatMessage implements Packet<INetHandlerPlayServer> {
    private String message;

    public CPacketChatMessage() {
    }

    public CPacketChatMessage(String messageIn) {
        if (messageIn.length() > 256) {
            messageIn = messageIn.substring(0, 256);
        }

        this.message = messageIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException {
        this.message = buf.readString(256);
    }

    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.message);
    }


    // Spigot Start
    private static final java.util.concurrent.ExecutorService executors = java.util.concurrent.Executors.newCachedThreadPool(
                new com.google.common.util.concurrent.ThreadFactoryBuilder().setDaemon( true ).setNameFormat( "Async Chat Thread - #%d" ).build() );
        public void processPacket(final INetHandlerPlayServer handler) {
            if ( !message.startsWith("/") )
            {
                executors.submit(() -> handler.processChatMessage( CPacketChatMessage.this ));
                return;
            }
            // Spigot End
        handler.processChatMessage(this);
    }

    public String getMessage() {
        return this.message;
    }
}