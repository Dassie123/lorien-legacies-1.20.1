package net.scarab.lorienlegacies.entity;

import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import net.minecraft.world.explosion.Explosion;
import net.scarab.lorienlegacies.item.ModItems;
import net.scarab.lorienlegacies.util.ExplosiveChainHandler;

import java.util.List;

public class SpikyBlackBallEntity extends Entity implements FlyingItemEntity {

    private int ticksExisted = 0;
    private boolean exploded = false;

    public SpikyBlackBallEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        // No data to track currently
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.ticksExisted = nbt.getInt("TicksExisted");
        this.exploded = nbt.getBoolean("Exploded");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("TicksExisted", this.ticksExisted);
        nbt.putBoolean("Exploded", this.exploded);
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;

        if (!this.getWorld().isClient) {
            if (ticksExisted == 20 && !exploded) {
                /* LOGIC THAT CENTERS EXPLOSION DAMAGE ON NEARBY ENTITY WHEN EXPLOSION OCCURS WITHIN A 5 BLOCK RADIUS
                EXPLOSION CENTERING CODE START
                DETERMINES PROJECTILE POSITION (IDEAL FOR TIMED EFFECTS) */
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

                // Create explosion at determined location, chaining into nearby victims with a
                // weaker blast each generation - the centring above fixes the direct hit, the
                // chain is what reaches the rest of a tight pack
                ExplosiveChainHandler.detonate(
                        this.getWorld(),
                        this, // cause
                        x, y, z /* Coordinates supplied by above EXPLOSION CENTERING CODE */,
                        3.0F,
                        World.ExplosionSourceType.MOB
                );
                exploded = true;
            } else if (ticksExisted == 40) {
                spawnYellowBall();
                this.discard();
            }
        }
    }

    private void spawnYellowBall() {
        ItemStack yellowBallStack = new ItemStack(ModItems.SPIKY_YELLOW_BALL);
        this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), yellowBallStack));
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(ModItems.SPIKY_BLACK_BALL);
    }

    @Override
    public boolean shouldSave() {
        return true;
    }
}
