package me.td.mythicalstaves.items;

import me.td.mythicalstaves.MythicalStaves;
import me.td.mythicalstaves.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.TimeUnit;

public class StaffOfZeusItem extends Item {
    public StaffOfZeusItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(target.getWorld().isClient) return super.postHit(stack, target, attacker);

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 10*20, 3), attacker);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40*20, 3), attacker);

        for(int i = 0; i < 10; ++i) {
            sched(() -> lightning(target.getWorld(), target.getBlockPos()), (i*1000)+1000);
        }

        stack.damage(1, attacker, (entity) -> {
            entity.sendToolBreakStatus(attacker.getActiveHand());
        });

        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(world.isClient) return TypedActionResult.pass(user.getStackInHand(hand));

        BlockHitResult result = Utils.raycastForBlock(user, 35d);

        if(result == null) return TypedActionResult.pass(user.getStackInHand(hand));
        BlockState block = world.getBlockState(result.getBlockPos());
        if(block.isOf(Blocks.AIR)) return TypedActionResult.pass(user.getStackInHand(hand));

        BlockPos pos = result.getBlockPos();
        int radius = 10;

        for(int i = 1; i < radius; ++i) {
            int finalI = i;
            sched(() -> {
                for (int x = -finalI; x < finalI; ++x) {
                    for (int z = -finalI; z < finalI; ++z) {
                        int finalX = x;
                        int finalZ = z;
                        lightning(world, pos.add(finalX, -1, finalZ));
                        world.setBlockState(pos.add(finalX, 0, finalZ), Blocks.AIR.getDefaultState());
                        world.setBlockState(pos.add(0, -1, 0).add(finalX, 0, finalZ), Blocks.AIR.getDefaultState());
                    }
                }
            }, (i * 500) - 500);
        }

        user.getItemCooldownManager().set(asItem(), 10 * 20);
        user.getStackInHand(hand).damage(2, user, (entity) -> {
            entity.sendToolBreakStatus(hand);
        });

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void creeper(World world, BlockPos pos) {
        CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, world);
        creeper.setPos(pos.getX(), pos.getY(), pos.getZ());
        world.spawnEntity(creeper);
    }

    private void lightning(World world, BlockPos pos) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.setPosition(pos.getX(), pos.getY(), pos.getZ());
        world.spawnEntity(lightning);
    }

    private void sched(Runnable runnable, long millis) {
        MythicalStaves.scheduler.schedule(runnable, millis, TimeUnit.MILLISECONDS);
    }
}
