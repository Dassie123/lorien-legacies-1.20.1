package net.scarab.lorienlegacies.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
Sympathetic detonation for the mod's explosives.

A single vanilla explosion falls off steeply with distance, so a blast in a tight pack of mobs
killed whoever it was centred on and left the ones a couple of blocks away standing. That is
correct vanilla behaviour, just a poor fit for an explosive weapon.

This spreads the blast the way a real chain detonation does: the initial explosion can set off
secondary blasts centred on nearby victims, each weaker than the one that triggered it, until the
yield is too small to matter. It is deliberately bounded so it cannot cascade across a field:

- Each generation carries YIELD_FALLOFF of the previous one's strength, so the chain dies out on
  its own after a few steps rather than needing an arbitrary cutoff.
- A blast only reaches CHAIN_RADIUS_PER_STRENGTH blocks per point of strength, so a weakened
  blast also reaches less far - distance and yield weaken together.
- MAX_CHAIN_DEPTH caps how many generations deep it can go, and MAX_TARGETS_PER_BLAST caps how
  many victims one blast can set off, so a hundred mobs cannot produce a hundred explosions.
- Line of sight is required, so a wall genuinely stops the chain.
- Only the first explosion damages terrain. Secondaries use NONE, so chaining spreads damage
  without turning every firefight into a crater field.
*/
public final class ExplosiveChainHandler {

    // Fraction of the triggering blast's strength that a secondary blast carries
    private static final float YIELD_FALLOFF = 0.55F;

    // A blast weaker than this is not worth propagating - the chain stops here
    private static final float MIN_CHAIN_STRENGTH = 1.0F;

    // How far a blast can reach per point of strength when looking for things to set off
    private static final double CHAIN_RADIUS_PER_STRENGTH = 2.0;

    // How many generations deep the chain may run, counting the initial blast as generation 0
    private static final int MAX_CHAIN_DEPTH = 3;

    // How many victims a single blast may set off
    private static final int MAX_TARGETS_PER_BLAST = 3;

    private ExplosiveChainHandler() {
    }

    /*
    Detonates at a position and lets the blast chain through nearby living entities.

    The cause and source type of the initial blast are passed through unchanged, so each weapon
    keeps its own explosion behaviour - what it damages, and who gets blamed for it
    */
    public static void detonate(World world, net.minecraft.entity.Entity cause, double x, double y, double z,
                                float strength, World.ExplosionSourceType sourceType) {
        // The initial blast is the only one allowed to damage terrain
        world.createExplosion(cause, x, y, z, strength, sourceType);
        chain(world, cause, new Vec3d(x, y, z), strength, 0, new HashSet<>());
    }

    /*
    Propagates one generation of the chain outward from a blast
    */
    private static void chain(World world, net.minecraft.entity.Entity cause, Vec3d origin, float strength, int depth, Set<Integer> alreadyDetonated) {
        if (depth >= MAX_CHAIN_DEPTH) {
            return;
        }

        float nextStrength = strength * YIELD_FALLOFF;
        if (nextStrength < MIN_CHAIN_STRENGTH) {
            return;
        }

        // Reach shrinks with yield, so a weakened blast also spreads less far
        double reach = strength * CHAIN_RADIUS_PER_STRENGTH;
        Box searchBox = new Box(origin.x - reach, origin.y - reach, origin.z - reach,
                origin.x + reach, origin.y + reach, origin.z + reach);

        List<LivingEntity> candidates = new ArrayList<>(world.getEntitiesByClass(LivingEntity.class, searchBox,
                entity -> entity.isAlive()
                        && entity != cause
                        && !alreadyDetonated.contains(entity.getId())
                        && centreOf(entity).distanceTo(origin) <= reach
                        && hasLineOfSight(world, origin, entity)));

        // Nearest first, so the blast sets off what it is actually adjacent to
        candidates.sort(Comparator.comparingDouble(entity -> centreOf(entity).squaredDistanceTo(origin)));

        int detonations = 0;
        for (LivingEntity victim : candidates) {
            if (detonations >= MAX_TARGETS_PER_BLAST) {
                break;
            }
            alreadyDetonated.add(victim.getId());
            detonations++;

            // Centre the secondary on the victim's body, not its feet. An explosion at foot level
            // sits half inside the ground, so the blast is partly absorbed and often fails to kill
            // the very entity it detonated on - the same reason the primary blast is centred
            Vec3d at = centreOf(victim);
            // Secondaries leave terrain alone - chaining should spread damage, not craters
            world.createExplosion(cause, at.x, at.y, at.z, nextStrength, World.ExplosionSourceType.NONE);
            chain(world, cause, at, nextStrength, depth + 1, alreadyDetonated);
        }
    }

    /*
    The point to centre a blast on for an entity: the middle of its body rather than its feet
    */
    public static double centreY(net.minecraft.entity.Entity entity) {
        return entity.getY() + entity.getHeight() * 0.5;
    }

    private static Vec3d centreOf(LivingEntity entity) {
        return new Vec3d(entity.getX(), entity.getY() + entity.getHeight() * 0.5, entity.getZ());
    }

    /*
    True when nothing solid stands between the blast and the entity, so walls stop the chain
    */
    private static boolean hasLineOfSight(World world, Vec3d origin, LivingEntity entity) {
        Vec3d target = entity.getPos().add(0.0, entity.getHeight() * 0.5, 0.0);
        return world.raycast(new RaycastContext(origin, target,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity))
                .getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }
}
