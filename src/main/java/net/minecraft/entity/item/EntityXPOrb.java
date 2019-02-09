package net.minecraft.entity.item;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityXPOrb extends Entity
{
    public int xpColor;
    public int xpOrbAge;
    public int delayBeforeCanPickup;
    private int xpOrbHealth = 5;
    public int xpValue;
    private EntityPlayer closestPlayer;
    private int xpTargetColor;

    public EntityXPOrb(World worldIn, double x, double y, double z, int expValue)
    {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
        this.setPosition(x, y, z);
        this.rotationYaw = (float)(Math.random() * 360.0D);
        this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
        this.motionY = (double)((float)(Math.random() * 0.2D) * 2.0F);
        this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
        this.xpValue = expValue;
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    public EntityXPOrb(World worldIn)
    {
        super(worldIn);
        this.setSize(0.25F, 0.25F);
    }

    protected void entityInit()
    {
    }

    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender()
    {
        float f = 0.5F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        int i = super.getBrightnessForRender();
        int j = i & 255;
        int k = i >> 16 & 255;
        j = j + (int)(f * 15.0F * 16.0F);

        if (j > 240)
        {
            j = 240;
        }

        return j | k << 16;
    }

    public void onUpdate()
    {
        super.onUpdate();
        EntityPlayer prevTarget = this.closestPlayer;// CraftBukkit - store old target

        if (this.delayBeforeCanPickup > 0)
        {
            --this.delayBeforeCanPickup;
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (!this.hasNoGravity())
        {
            this.motionY -= 0.029999999329447746D;
        }

        if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA)
        {
            this.motionY = 0.20000000298023224D;
            this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
            this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
            this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
        }

        this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        double d0 = 8.0D;

        if (this.xpTargetColor < this.xpColor - 20 + this.getEntityId() % 100)
        {
            if (this.closestPlayer == null || this.closestPlayer.getDistanceSq(this) > 64.0D)
            {
                this.closestPlayer = this.world.getClosestPlayerToEntity(this, 8.0D);
            }

            this.xpTargetColor = this.xpColor;
        }

        if (this.closestPlayer != null && this.closestPlayer.isSpectator())
        {
            this.closestPlayer = null;
        }

        if (this.closestPlayer != null)
        {
            boolean cancelled = false;
            if (this.closestPlayer != prevTarget) {
                EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this, closestPlayer, EntityTargetEvent.TargetReason.CLOSEST_PLAYER);
                EntityLivingBase target = event.getTarget() == null ? null : ((org.bukkit.craftbukkit.entity.CraftLivingEntity) event.getTarget()).getHandle();
                closestPlayer = target instanceof EntityPlayer ? (EntityPlayer) target : null;
                cancelled = event.isCancelled();
            }

            if (!cancelled && closestPlayer != null) {
                double d1 = (this.closestPlayer.posX - this.posX) / 8.0D;
                double d2 = (this.closestPlayer.posY + (double) this.closestPlayer.getEyeHeight() / 2.0D - this.posY) / 8.0D;
                double d3 = (this.closestPlayer.posZ - this.posZ) / 8.0D;
                double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                double d5 = 1.0D - d4;

                if (d5 > 0.0D) {
                    d5 = d5 * d5;
                    this.motionX += d1 / d4 * d5 * 0.1D;
                    this.motionY += d2 / d4 * d5 * 0.1D;
                    this.motionZ += d3 / d4 * d5 * 0.1D;
                }
            }
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        float f = 0.98F;

        if (this.onGround)
        {
            BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
            net.minecraft.block.state.IBlockState underState = this.world.getBlockState(underPos);
            f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;
        }

        this.motionX *= (double)f;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= (double)f;

        if (this.onGround)
        {
            this.motionY *= -0.8999999761581421D;
        }

        ++this.xpColor;
        ++this.xpOrbAge;

        if (this.xpOrbAge >= 6000)
        {
            this.setDead();
        }
    }

    public boolean handleWaterMovement()
    {
        return this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this);
    }

    protected void dealFireDamage(int amount)
    {
        this.attackEntityFrom(DamageSource.IN_FIRE, (float)amount);
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.world.isRemote || this.isDead) return false; //Forge: Fixes MC-53850
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            this.markVelocityChanged();
            this.xpOrbHealth = (int)((float)this.xpOrbHealth - amount);

            if (this.xpOrbHealth <= 0)
            {
                this.setDead();
            }

            return false;
        }
    }

    public void writeEntityToNBT(NBTTagCompound compound)
    {
        compound.setShort("Health", (short)this.xpOrbHealth);
        compound.setShort("Age", (short)this.xpOrbAge);
        compound.setShort("Value", (short)this.xpValue);
    }

    public void readEntityFromNBT(NBTTagCompound compound)
    {
        this.xpOrbHealth = compound.getShort("Health");
        this.xpOrbAge = compound.getShort("Age");
        this.xpValue = compound.getShort("Value");
    }

    public void onCollideWithPlayer(EntityPlayer entityIn)
    {
        if (!this.world.isRemote)
        {
            if (this.delayBeforeCanPickup == 0 && entityIn.xpCooldown == 0)
            {
                if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerPickupXpEvent(entityIn, this))) return;
                entityIn.xpCooldown = 2;
                entityIn.onItemPickup(this, 1);
                ItemStack itemstack = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, entityIn);

                if (!itemstack.isEmpty() && itemstack.isItemDamaged())
                {
                    int i = Math.min(this.xpToDurability(this.xpValue), itemstack.getItemDamage());
//                    this.xpValue -= this.durabilityToXp(i);
//                    itemstack.setItemDamage(itemstack.getItemDamage() - i);
                    org.bukkit.event.player.PlayerItemMendEvent event = CraftEventFactory.callPlayerItemMendEvent(entityIn, this, itemstack, i);
                    i = event.getRepairAmount();
                    if (!event.isCancelled()) {
                        this.xpValue -= this.durabilityToXp(i);
                        itemstack.setItemDamage(itemstack.getItemDamage() - i);
                    }
                }

                if (this.xpValue > 0)
                {
                    entityIn.addExperience(CraftEventFactory.callPlayerExpChangeEvent(entityIn, this.xpValue).getAmount()); // CraftBukkit - this.value -> event.getAmount()
                }

                this.setDead();
            }
        }
    }

    private int durabilityToXp(int durability)
    {
        return durability / 2;
    }

    private int xpToDurability(int xp)
    {
        return xp * 2;
    }

    public int getXpValue()
    {
        return this.xpValue;
    }

    @SideOnly(Side.CLIENT)
    public int getTextureByXP()
    {
        if (this.xpValue >= 2477)
        {
            return 10;
        }
        else if (this.xpValue >= 1237)
        {
            return 9;
        }
        else if (this.xpValue >= 617)
        {
            return 8;
        }
        else if (this.xpValue >= 307)
        {
            return 7;
        }
        else if (this.xpValue >= 149)
        {
            return 6;
        }
        else if (this.xpValue >= 73)
        {
            return 5;
        }
        else if (this.xpValue >= 37)
        {
            return 4;
        }
        else if (this.xpValue >= 17)
        {
            return 3;
        }
        else if (this.xpValue >= 7)
        {
            return 2;
        }
        else
        {
            return this.xpValue >= 3 ? 1 : 0;
        }
    }

    public static int getXPSplit(int expValue)
    {
        // CraftBukkit start
        if (expValue > 162670129) return expValue - 100000;
        if (expValue > 81335063) return 81335063;
        if (expValue > 40667527) return 40667527;
        if (expValue > 20333759) return 20333759;
        if (expValue > 10166857) return 10166857;
        if (expValue > 5083423) return 5083423;
        if (expValue > 2541701) return 2541701;
        if (expValue > 1270849) return 1270849;
        if (expValue > 635413) return 635413;
        if (expValue > 317701) return 317701;
        if (expValue > 158849) return 158849;
        if (expValue > 79423) return 79423;
        if (expValue > 39709) return 39709;
        if (expValue > 19853) return 19853;
        if (expValue > 9923) return 9923;
        if (expValue > 4957) return 4957;
        // CraftBukkit end
        if (expValue >= 2477)
        {
            return 2477;
        }
        else if (expValue >= 1237)
        {
            return 1237;
        }
        else if (expValue >= 617)
        {
            return 617;
        }
        else if (expValue >= 307)
        {
            return 307;
        }
        else if (expValue >= 149)
        {
            return 149;
        }
        else if (expValue >= 73)
        {
            return 73;
        }
        else if (expValue >= 37)
        {
            return 37;
        }
        else if (expValue >= 17)
        {
            return 17;
        }
        else if (expValue >= 7)
        {
            return 7;
        }
        else
        {
            return expValue >= 3 ? 3 : 1;
        }
    }

    public boolean canBeAttackedWithItem()
    {
        return false;
    }
}