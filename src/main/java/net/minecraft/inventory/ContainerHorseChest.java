package net.minecraft.inventory;

import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerHorseChest extends InventoryBasic
{
    public ContainerHorseChest(String inventoryTitle, int slotCount, AbstractHorse owner)
    {
        super(inventoryTitle, false, slotCount, (org.bukkit.entity.AbstractHorse) owner.getBukkitEntity());
    }

    @SideOnly(Side.CLIENT)
    public ContainerHorseChest(ITextComponent inventoryTitle, int slotCount)
    {
        super(inventoryTitle, slotCount);
    }
}