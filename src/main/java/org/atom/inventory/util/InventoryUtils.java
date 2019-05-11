package org.atom.inventory.util;

import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.tileentity.TileEntity;
import org.atom.inventory.CustomInventory;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;

public class InventoryUtils {

    @Nullable
    public static InventoryHolder getInventoryOwner(IInventory inventory) {
        if (inventory instanceof TileEntity) {
            TileEntity te = (TileEntity) inventory;
            BlockState state = te.getWorld().getWorld().getBlockAt(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()).getState();
            if (state instanceof InventoryHolder)
                return (InventoryHolder) state;
        } else if (inventory instanceof InventoryBasic) {
            InventoryBasic inventoryBasic = (InventoryBasic) inventory;
            if (inventoryBasic instanceof ContainerHorseChest)
                return (InventoryHolder) ((ContainerHorseChest) inventoryBasic).getAnimal().getBukkitEntity();
            if (inventoryBasic instanceof InventoryEnderChest)
                return ((InventoryEnderChest) inventoryBasic).getBukkitOwner();
        } else if (inventory instanceof EntityMinecartContainer) {
            Entity cart = ((EntityMinecartContainer) inventory).getBukkitEntity();
            if (cart instanceof InventoryHolder)
                return (InventoryHolder) cart;
        } else if (inventory instanceof InventoryPlayer) {
            InventoryPlayer inventoryPlayer = (InventoryPlayer) inventory;
            return inventoryPlayer.player.getBukkitEntity();
        } else if (inventory instanceof InventoryCrafting) {
            return inventory.getOwner();
        } else if (inventory instanceof InventoryMerchant) {
            return ((InventoryMerchant) inventory).getPlayer().getBukkitEntity();
        } else {
            return new CustomInventory(inventory).getInventory().getHolder();
        }
        return null;
    }
}
