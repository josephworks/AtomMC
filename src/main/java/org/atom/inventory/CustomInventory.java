package org.atom.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;

public class CustomInventory implements InventoryHolder {

    private final IInventory inventory;
    private final CraftInventory container;

    public CustomInventory(final IInventory inventory) {
        this.container = new CraftInventory(inventory);
        this.inventory = inventory;
    }

    public CustomInventory(final ItemStackHandler handler) {
        this.container = new CraftInventoryCustom(this, handler.getStacksList());
        this.inventory = this.container.getInventory();
    }

    @Override
    public Inventory getInventory() {
        return this.container;
    }

    @Nullable
    public static InventoryHolder holderFromForge(final IItemHandler handler) {
        if (handler == null) {
            return null;
        }
        if (handler instanceof ItemStackHandler) {
            return new CustomInventory((ItemStackHandler) handler);
        }
        if (handler instanceof SlotItemHandler) {
            return new CustomInventory(((SlotItemHandler) handler).inventory);
        }
        if (handler instanceof InvWrapper) {
            return new CustomInventory(((InvWrapper) handler).getInv());
        }
        if (handler instanceof SidedInvWrapper) {
            return new CustomInventory(((SidedInvWrapper) handler).getInv());
        }
        return null;
    }

    @Nullable
    public static Inventory inventoryFromForge(final IItemHandler handler) {
        final InventoryHolder holder = holderFromForge(handler);
        return (holder != null) ? holder.getInventory() : null;
    }
}
