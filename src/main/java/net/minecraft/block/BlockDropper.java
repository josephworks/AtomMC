package net.minecraft.block;

import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.atom.inventory.util.InventoryUtils;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class BlockDropper extends BlockDispenser
{
    private final IBehaviorDispenseItem dropBehavior = new BehaviorDefaultDispenseItem();

    protected IBehaviorDispenseItem getBehavior(ItemStack stack)
    {
        return this.dropBehavior;
    }

    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityDropper();
    }

    public void dispense(World worldIn, BlockPos pos)
    {
        BlockSourceImpl blocksourceimpl = new BlockSourceImpl(worldIn, pos);
        TileEntityDispenser tileentitydispenser = (TileEntityDispenser)blocksourceimpl.getBlockTileEntity();

        if (tileentitydispenser != null)
        {
            int i = tileentitydispenser.getDispenseSlot();

            if (i < 0)
            {
                worldIn.playEvent(1001, pos, 0);
            }
            else
            {
                ItemStack itemstack = tileentitydispenser.getStackInSlot(i);

                if (!itemstack.isEmpty() && net.minecraftforge.items.VanillaInventoryCodeHooks.dropperInsertHook(worldIn, pos, tileentitydispenser, i, itemstack))
                {
                    EnumFacing enumfacing = (EnumFacing)worldIn.getBlockState(pos).getValue(FACING);
                    BlockPos blockpos = pos.offset(enumfacing);
                    IInventory iinventory = TileEntityHopper.getInventoryAtPosition(worldIn, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                    ItemStack itemstack1;

                    if (iinventory == null)
                    {
                        itemstack1 = this.dropBehavior.dispense(blocksourceimpl, itemstack);
                    }
                    else
                    {
                        CraftItemStack oitemstack = CraftItemStack.asCraftMirror(itemstack.copy().splitStack(1));
                        org.bukkit.inventory.Inventory destinationInventory;
                        // Have to special case large chests as they work oddly
                        if (iinventory instanceof InventoryLargeChest) {
                            destinationInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) iinventory);
                        } else {
                            destinationInventory = InventoryUtils.getInventoryOwner(iinventory).getInventory();
                        }

                        InventoryMoveItemEvent event = new InventoryMoveItemEvent(InventoryUtils.getInventoryOwner(tileentitydispenser).getInventory(), oitemstack.clone(), destinationInventory, true);
                        worldIn.getServer().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            return;
                        }
                        itemstack1 = TileEntityHopper.putStackInInventoryAllSlots(tileentitydispenser, iinventory, CraftItemStack.asNMSCopy(event.getItem()), enumfacing.getOpposite());
                        if (event.getItem().equals(oitemstack) && itemstack1.isEmpty())
                        {
                            itemstack1 = itemstack.copy();
                            itemstack1.shrink(1);
                        }
                        else
                        {
                            itemstack1 = itemstack.copy();
                        }
                    }

                    tileentitydispenser.setInventorySlotContents(i, itemstack1);
                }
            }
        }
    }
}