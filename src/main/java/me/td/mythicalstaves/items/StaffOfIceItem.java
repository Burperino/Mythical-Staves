package me.td.mythicalstaves.items;

import me.td.mythicalstaves.MythicalStaves;
import me.td.mythicalstaves.Utils;
import me.td.mythicalstaves.effects.Effects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class
StaffOfIceItem extends Item {
    public StaffOfIceItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(target.getWorld().isClient) return super.postHit(stack, target, attacker);

        target.getWorld().playSound(
                null, // No specific player, so it's audible to all in range
                target.getBlockPos(), // The position at which to play the sound
                SoundEvents.BLOCK_ANVIL_LAND, // The sound event to play
                SoundCategory.BLOCKS, // Sound category for volume control
                1.0f, // Volume, between 0.0 and 1.0
                1.0f  // Pitch, where 1.0 is the normal pitch
        );

        Vec3d attackerDirection = attacker.getRotationVec(1f).normalize();
        Vec3d velocity = new Vec3d(attackerDirection.x, (double) -1 / 3, attackerDirection.z).multiply(-3);
        attacker.setVelocity(velocity);
        attacker.velocityModified = true; // Force sync packet

        stack.damage(1, attacker, (entity) -> {
            entity.sendToolBreakStatus(attacker.getActiveHand());
        });

        sched(() -> {
            target.setVelocity(0, 0, 0);
            target.velocityModified = true;

            Box boundingBox = target.getBoundingBox();

            Vec3d _center = Utils.vec3dAbs(new Vec3d(
                    boundingBox.maxX - boundingBox.minX,
                    boundingBox.maxY - boundingBox.minY,
                    boundingBox.maxZ - boundingBox.minZ
            ));

            int radius = (int) Math.ceil(Math.max(Math.max(
                    _center.getX(), _center.getY()
            ), _center.getZ()));

            ArrayList<BlockPos> blocks = Utils.drawHollowSphere(target.getBlockPos(), radius + 1);
            for (BlockPos block : blocks) {
                target.getWorld().setBlockState(block, Blocks.ICE.getDefaultState());
            }

            target.addStatusEffect(new StatusEffectInstance(Effects.FROZEN_EFFECT, 15 * 20), attacker);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 15 * 20, 255), attacker);

            target.getWorld().playSound(
                    null, // No specific player, so it's audible to all in range
                    target.getBlockPos(), // The position at which to play the sound
                    SoundEvents.BLOCK_GLASS_BREAK, // The sound event to play
                    SoundCategory.BLOCKS, // Sound category for volume control
                    1.0f, // Volume, between 0.0 and 1.0
                    1.0f  // Pitch, where 1.0 is the normal pitch
            );
        }, 1250);

        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        BlockHitResult result = Utils.raycastForBlock(user, 35d);

        if(result == null) return TypedActionResult.pass(user.getStackInHand(hand));
        BlockState block = world.getBlockState(result.getBlockPos());
        if(block.isOf(Blocks.AIR)) return TypedActionResult.pass(user.getStackInHand(hand));

        sched(() -> {
            iceExplosion(result.getBlockPos(), world, 4, user);
        }, 0);

        sched(() -> {
            iceExplosion(result.getBlockPos(), world, 7, user);
        }, 500);

        sched(() -> {
            iceExplosion(result.getBlockPos(), world, 10, user);
        }, 1000);

        user.getItemCooldownManager().set(asItem(), 5 * 20);
        user.getStackInHand(hand).damage(2, user, (entity) -> {
            entity.sendToolBreakStatus(hand);
        });

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void iceExplosion(BlockPos blockPos, World world, int radius, Entity caster) {
        world.playSound(
                null, // No specific player, so it's audible to all in range
                blockPos, // The position at which to play the sound
                SoundEvents.BLOCK_ANVIL_LAND, // The sound event to play
                SoundCategory.BLOCKS, // Sound category for volume control
                1.0f, // Volume, between 0.0 and 1.0
                1.0f  // Pitch, where 1.0 is the normal pitch
        );

        List<Entity> entitiesInRange = Utils.findEntitiesOfTypeInSphere(world, blockPos, radius, caster);
        for(Entity e : entitiesInRange) {
            if(e instanceof LivingEntity livingEntity) {
                e.setVelocity(0, -10, 0);
                e.velocityModified = true;
                livingEntity.addStatusEffect(new StatusEffectInstance(
                        Effects.FROZEN_EFFECT,
                        10*20// 10 seconds
                ), caster);
                livingEntity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS,
                        20*20
                ), caster);
            }
        }

        ArrayList<BlockPos> posses = Utils.drawSphere(blockPos, radius);
        for(BlockPos pos : posses) {
            BlockState state = world.getBlockState(pos);
            if(state.isAir()) continue;

            world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState());
        }
    }

    private void sched(Runnable runnable, long millis) {
        MythicalStaves.scheduler.schedule(runnable, millis, TimeUnit.MILLISECONDS);
    }
}
