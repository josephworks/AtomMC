package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorldNameable;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;

public interface IInventory extends IWorldNameable {
    int getSizeInventory();

    boolean isEmpty();

    ItemStack getStackInSlot(int index);

    ItemStack decrStackSize(int index, int count);

    ItemStack removeStackFromSlot(int index);

    void setInventorySlotContents(int index, ItemStack stack);

    int getInventoryStackLimit();

    void markDirty();

    boolean isUsableByPlayer(EntityPlayer player);

    void openInventory(EntityPlayer player);

    void closeInventory(EntityPlayer player);

    boolean isItemValidForSlot(int index, ItemStack stack);

    int getField(int id);

    void setField(int id, int value);

    int getFieldCount();

    void clear();

    java.util.List<ItemStack> getContents();

    void onOpen(CraftHumanEntity who);

    void onClose(CraftHumanEntity who);

    java.util.List<org.bukkit.entity.HumanEntity> getViewers();

    org.bukkit.inventory.InventoryHolder getOwner();

    void setMaxStackSize(int size);

    org.bukkit.Location getLocation();

    int MAX_STACK = 64;
}