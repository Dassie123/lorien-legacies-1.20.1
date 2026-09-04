package net.scarab.lorienlegacies.entity;

import net.minecraft.block.BlockState;
import net.scarab.lorienlegacies.util.ExplosiveChainHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class KineticProjectileEntity extends ThrownItemEntity {
    public KineticProjectileEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public KineticProjectileEntity(LivingEntity livingEntity, World world) {
        super(ModEntities.THROWN_KINETIC_PROJECTILE, livingEntity, world);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);

        if (!this.getWorld().isClient()) {
            // Create explosion at the projectile's position, chaining into nearby victims
            ExplosiveChainHandler.detonate(
                    this.getWorld(),
                    this,
                    this.getX(), this.getY(), this.getZ(),
                    3.0f,
                    World.ExplosionSourceType.MOB
            );
            this.discard(); // Remove the projectile after the explosion
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        /* LOGIC THAT CENTERS EXPLOSION DAMAGE ON NEARBY ENTITY WHEN EXPLOSION OCCURS WITHIN A 5 BLOCK RADIUS
        EXPLOSION CENTERING CODE START
        DETERMINES BLOCK HIT POSITION (IDEAL FOR BLOCK HIT TRIGGERED EFFECTS) */
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        // Check for nearby entities within 5-block radius
        List<LivingEntity> nearbyEntities = this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                new Box(x - 5, y - 5, z - 5, x + 5, y + 5, z + 5),
                LivingEntity::isAlive
        );

        // If any entity nearby, re-center explosion on it
        if (!nearbyEntities.isEmpty()) {
            LivingEntity target = nearbyEntities.get(0);
            x = target.getX();
            y = target.getY();
            z = target.getZ();
        }
        // EXPLOSION CENTERING CODE END

        // Create explosion at determined location, chaining into nearby victims
        ExplosiveChainHandler.detonate(
                this.getWorld(),
                this, // cause
                x, y, z /* Coordinates supplied by above EXPLOSION CENTERING CODE */,
                3.0F,
                World.ExplosionSourceType.MOB
        );

        super.onBlockHit(blockHitResult);
        this.discard(); // Remove the projectile after the explosion
    }
}
