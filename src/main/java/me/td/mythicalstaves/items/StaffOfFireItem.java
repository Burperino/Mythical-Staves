package me.td.mythicalstaves.items;

import me.td.mythicalstaves.MythicalStaves;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.concurrent.TimeUnit;

public class StaffOfFireItem extends Item {
    public StaffOfFireItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(target.getWorld().isClient) return super.postHit(stack, target, attacker);

        target.getWorld().playSound(
                null, // No specific player, so it's audible to all in range
                target.getBlockPos(), // The position at which to play the sound
                SoundEvents.BLOCK_FIRE_EXTINGUISH, // The sound event to play
                SoundCategory.BLOCKS, // Sound category for volume control
                1.0f, // Volume, between 0.0 and 1.0
                1.0f  // Pitch, where 1.0 is the normal pitch
        );

        Vec3d attackerDirection = attacker.getRotationVec(1f).normalize();
        Vec3d velocity = new Vec3d(attackerDirection.x, (double) -1 / 3, attackerDirection.z).multiply(-3);
        attacker.setVelocity(velocity);
        attacker.velocityModified = true; // Force sync packet

        sched(() -> {
            target.getWorld().playSound(
                    null, // No specific player, so it's audible to all in range
                    target.getBlockPos(), // The position at which to play the sound
                    SoundEvents.BLOCK_ANVIL_LAND, // The sound event to play
                    SoundCategory.BLOCKS, // Sound category for volume control
                    1.0f, // Volume, between 0.0 and 1.0
                    1.0f  // Pitch, where 1.0 is the normal pitch
            );
            putInLava(target);
        }, 1250);

        stack.damage(2, attacker, (entity) -> {
            entity.sendToolBreakStatus(attacker.getActiveHand());
        });

        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(world.isClient) return TypedActionResult.pass(user.getStackInHand(hand));

        shootFireball((ServerPlayerEntity) user);

        user.getItemCooldownManager().set(asItem(), 5 * 20);
        user.getStackInHand(hand).damage(2, user, (entity) -> {
            entity.sendToolBreakStatus(hand);
        });

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void shootFireball(ServerPlayerEntity player) {
        Vec3d playerPos = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0F).normalize();


        FireballEntity fireball = new FireballEntity(
                player.getWorld(),
                player,
                0, 0, 0,
                10
        );

        fireball.setPos(playerPos.x, playerPos.y, playerPos.z);

        // Set the fireball's direction and speed
        double speed = 7.5; // Adjust speed as needed
        fireball.setVelocity(direction.x * speed, direction.y * speed, direction.z * speed, (float)speed, 1.0F);

        // Spawn the fireball in the world
        player.getWorld().spawnEntity(fireball);
    }

    private void sched(Runnable runnable, long millis) {
        MythicalStaves.scheduler.schedule(runnable, millis, TimeUnit.MILLISECONDS);
    }

    private void putInLava(LivingEntity target) {
        World world = target.getWorld();
        BlockPos targetPos = target.getBlockPos();

        // Calculate radius based on bounding box, add +1 to ensure no suffocation
        int width = (int)Math.ceil(target.getBoundingBox().getXLength() / 2.0);
        int depth = (int)Math.ceil(target.getBoundingBox().getZLength() / 2.0);
        int radius = Math.max(width, depth) + 1;

        for(int x = -radius - 1; x < radius + 1; ++x) {
            for(int z = -radius - 1; z < radius + 1; ++z) {
                BlockPos pos = targetPos.add(x, 0, z);
                if((x == -radius - 1 || z == -radius - 1) || (x == radius || z == radius)) {
                    world.setBlockState(pos, Blocks.IRON_BARS.getDefaultState());
                    world.setBlockState(pos.add(0, 1, 0), Blocks.IRON_BARS.getDefaultState());
                } else {
                    world.setBlockState(pos, Blocks.LAVA.getDefaultState());
                }

                world.setBlockState(pos.add(0, -1, 0), Blocks.SOUL_SAND.getDefaultState());
            }
        }

        target.setVelocity(0, -10, 0);
        target.velocityModified = true;
    }
}
