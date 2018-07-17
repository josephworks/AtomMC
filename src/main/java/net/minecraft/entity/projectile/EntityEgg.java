package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class EntityEgg extends EntityThrowable
{
    public EntityEgg(World worldIn)
    {
        super(worldIn);
    }

    public EntityEgg(World worldIn, EntityLivingBase throwerIn)
    {
        super(worldIn, throwerIn);
    }

    public EntityEgg(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    public static void registerFixesEgg(DataFixer fixer)
    {
        EntityThrowable.registerFixesThrowable(fixer, "ThrownEgg");
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id)
    {
        if (id == 3)
        {
            double d0 = 0.08D;

            for (int i = 0; i < 8; ++i)
            {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, Item.getIdFromItem(Items.EGG));
            }
        }
    }

    protected void onImpact(RayTraceResult result)
    {
        if (result.entityHit != null)
        {
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
        }

        if (!this.world.isRemote)
        {
            boolean hatching = this.rand.nextInt(8) == 0;
            if (/*this.rand.nextInt(8) == 0*/ true)
            {
                byte i = 1;

                if (this.rand.nextInt(32) == 0)
                {
                    i = 4;
                }

//                for (int j = 0; j < i; ++j) {
//                    EntityChicken entitychicken = new EntityChicken(this.world);
                if (!hatching) {
                    i = 0;
                }
                EntityType hatchingType = EntityType.CHICKEN;

                Entity shooter = this.getThrower();
                if (shooter instanceof EntityPlayer) {
                    PlayerEggThrowEvent event = new PlayerEggThrowEvent((Player) shooter.getBukkitEntity(), (org.bukkit.entity.Egg) this.getBukkitEntity(), hatching, i, hatchingType);
                    this.world.getServer().getPluginManager().callEvent(event);

                    i = event.getNumHatches();
                    hatching = event.isHatching();
                    hatchingType = event.getHatchingType();
                }
//                    entitychicken.setGrowingAge(-24000);
//                    entitychicken.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
//                    this.world.spawnEntity(entitychicken);
                if (hatching) {
                    for (int k = 0; k < i; ++k) {
                        Entity entity = world.getWorld().createEntity(new org.bukkit.Location(world.getWorld(), this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F), hatchingType.getEntityClass());
                        if (entity.getBukkitEntity() instanceof Ageable) {
                            ((Ageable) entity.getBukkitEntity()).setBaby();
                        }
                        world.getWorld().addEntity(entity, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.EGG);
                    }
                }
            }

            this.world.setEntityState(this, (byte)3);
            this.setDead();
        }
    }
}