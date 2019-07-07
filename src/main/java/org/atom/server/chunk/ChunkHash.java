package org.atom.server.chunk;

public class ChunkHash {

    public static int chunkToKey(int x, int z){
        return (x & 0xffff) << 16 | (z & 0xffff);
    }

    public static int keyToX(int k){
        return (short)((k >> 16) & 0xffff);
    }

    public static int keyToZ(int k){
        return (short)(k & 0xffff);
    }

    public static short chunkCoordToHash(int x, int y, int z){
        return (short)(((x&15)<<12)|((z&15)<<8)|(y&255));
    }
}
