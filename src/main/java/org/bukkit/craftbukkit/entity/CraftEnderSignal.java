package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;

public class CraftEnderSignal extends CraftEntity implements EnderSignal {
    public CraftEnderSignal(CraftServer server, EntityEnderEye entity) {
        super(server, entity);
    }

    @Override
    public EntityEnderEye getHandle() {
        return (EntityEnderEye) entity;
    }

    @Override
    public String toString() {
        return "CraftEnderSignal";
    }

    @Override
    public EntityType getType() {
        return EntityType.ENDER_SIGNAL;
    }

    @Override
    public Location getTargetLocation() {
        return new Location(getWorld(), getHandle().a, getHandle().b, getHandle().c, getHandle().rotationYaw, getHandle().rotationPitch); // PAIL rename targetX, targetY, targetZ
    }

    @Override
    public void setTargetLocation(Location location) {
        Preconditions.checkArgument(getWorld().equals(location.getWorld()), "Cannot target EnderSignal across worlds");
        getHandle().a(new BlockPos(location.getX(), location.getY(), location.getZ()));
    }

    @Override
    public boolean getDropItem() {
        return getHandle().e; // PAIL rename getDropItem
    }

    @Override
    public void setDropItem(boolean shouldDropItem) {
        getHandle().e = shouldDropItem; // PAIL rename getDropItem
    }

    @Override
    public int getDespawnTimer() {
        return getHandle().d; // PAIL rename despawnTimer
    }

    @Override
    public void setDespawnTimer(int time) {
        getHandle().d = time; // PAIL rename despawnTimer
    }
}
