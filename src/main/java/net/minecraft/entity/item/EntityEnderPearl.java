package net.minecraft.entity.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EntityEnderPearl extends EntityThrowable
{
    private EntityLivingBase perlThrower;

    public EntityEnderPearl(World worldIn)
    {
        super(worldIn);
    }

    public EntityEnderPearl(World worldIn, EntityLivingBase throwerIn)
    {
        super(worldIn, throwerIn);
        this.perlThrower = throwerIn;
    }

    @SideOnly(Side.CLIENT)
    public EntityEnderPearl(World worldIn, double x, double y, double z)
    {
        super(worldIn, x, y, z);
    }

    public static void registerFixesEnderPearl(DataFixer fixer)
    {
        EntityThrowable.registerFixesThrowable(fixer, "ThrownEnderpearl");
    }

    protected void onImpact(RayTraceResult result)
    {
        EntityLivingBase entitylivingbase = this.getThrower();

        if (result.entityHit != null)
        {
            if (result.entityHit == this.perlThrower)
            {
                return;
            }

            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, entitylivingbase), 0.0F);
        }

        if (result.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos blockpos = result.getBlockPos();
            TileEntity tileentity = this.world.getTileEntity(blockpos);

            if (tileentity instanceof TileEntityEndGateway)
            {
                TileEntityEndGateway tileentityendgateway = (TileEntityEndGateway)tileentity;

                if (entitylivingbase != null)
                {
                    if (entitylivingbase instanceof EntityPlayerMP)
                    {
                        CriteriaTriggers.ENTER_BLOCK.trigger((EntityPlayerMP)entitylivingbase, this.world.getBlockState(blockpos));
                    }

                    tileentityendgateway.teleportEntity(entitylivingbase);
                    this.setDead();
                    return;
                }

                tileentityendgateway.teleportEntity(this);
                return;
            }
        }

        for (int i = 0; i < 32; ++i)
        {
            this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + this.rand.nextDouble() * 2.0D, this.posZ, this.rand.nextGaussian(), 0.0D, this.rand.nextGaussian());
        }

        if (!this.world.isRemote)
        {
            if (entitylivingbase instanceof EntityPlayerMP)
            {
                EntityPlayerMP entityplayermp = (EntityPlayerMP)entitylivingbase;

                if (entityplayermp.connection.getNetworkManager().isChannelOpen() && entityplayermp.world == this.world && !entityplayermp.isPlayerSleeping())
                {
                    net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(entityplayermp, this.posX, this.posY, this.posZ, 5.0F);
                    if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) { // Don't indent to lower patch size
                        // CraftBukkit start - Fire PlayerTeleportEvent
                        org.bukkit.craftbukkit.entity.CraftPlayer player = (CraftPlayer) entityplayermp.getBukkitEntity();
                        org.bukkit.Location location = getBukkitEntity().getLocation();
                        location.setPitch(player.getLocation().getPitch());
                        location.setYaw(player.getLocation().getYaw());

                        PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
                        Bukkit.getPluginManager().callEvent(teleEvent);

                        if (!teleEvent.isCancelled() && !entityplayermp.connection.isDisconnected()) {
                            if (this.rand.nextFloat() < 0.05F && this.world.getGameRules().getBoolean("doMobSpawning")) {
                                EntityEndermite entityendermite = new EntityEndermite(this.world);
                                entityendermite.setSpawnedByPlayer(true);
                                entityendermite.setLocationAndAngles(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, entitylivingbase.rotationYaw, entitylivingbase.rotationPitch);
                                this.world.spawnEntity(entityendermite, CreatureSpawnEvent.SpawnReason.ENDER_PEARL);
                            }

                            if (entitylivingbase.isRiding()) {
                                entitylivingbase.dismountRidingEntity();
                            }

                            entityplayermp.connection.teleport(teleEvent.getTo());
                            entitylivingbase.fallDistance = 0.0F;
                            CraftEventFactory.entityDamage = this;
                            entitylivingbase.attackEntityFrom(DamageSource.FALL, 5.0F);
                            CraftEventFactory.entityDamage = null;
                        }
//                            entitylivingbase.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
//                            entitylivingbase.fallDistance = 0.0F;
//                            entitylivingbase.attackEntityFrom(DamageSource.FALL, event.getAttackDamage());
                        }
                }
            }
            else if (entitylivingbase != null)
            {
                entitylivingbase.setPositionAndUpdate(this.posX, this.posY, this.posZ);
                entitylivingbase.fallDistance = 0.0F;
            }

            this.setDead();
        }
    }

    public void onUpdate()
    {
        EntityLivingBase entitylivingbase = this.getThrower();

        if (entitylivingbase != null && entitylivingbase instanceof EntityPlayer && !entitylivingbase.isEntityAlive())
        {
            this.setDead();
        }
        else
        {
            super.onUpdate();
        }
    }

    @Nullable
    public Entity changeDimension(int dimensionIn, net.minecraftforge.common.util.ITeleporter teleporter)
    {
        if (this.thrower.dimension != dimensionIn)
        {
            this.thrower = null;
        }

        return super.changeDimension(dimensionIn, teleporter);
    }
}