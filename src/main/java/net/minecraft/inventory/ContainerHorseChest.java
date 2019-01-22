package net.minecraft.inventory;

import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerHorseChest extends InventoryBasic
{
    private EntityAnimal entityAnimal;

    public ContainerHorseChest(String inventoryTitle, int slotCount, AbstractHorse owner)
    {
        super(inventoryTitle, false, slotCount, (org.bukkit.entity.AbstractHorse) owner.getBukkitEntity());
        this.entityAnimal = owner;
    }

    @SideOnly(Side.CLIENT)
    public ContainerHorseChest(ITextComponent inventoryTitle, int slotCount)
    {
        super(inventoryTitle, slotCount);
    }

    public EntityAnimal getAnimal()
    {
        return this.entityAnimal;
    }
}