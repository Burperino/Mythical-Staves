package me.td.mythicalstaves.items;

import me.td.mythicalstaves.MythicalStaves;
import me.td.mythicalstaves.Utils;
import me.td.mythicalstaves.effects.Effects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class StaffOfTheDepthsItem extends Item {
    public StaffOfTheDepthsItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(target.getWorld().isClient) return super.postHit(stack, target, attacker);

        System.out.println(FabricLoader.getInstance().getEnvironmentType());

        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.DARKNESS,
                30*20
        ));

        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS,
                30*20,
                3
        ));

        stack.damage(1, attacker, (entity) -> {
            entity.sendToolBreakStatus(attacker.getActiveHand());
        });

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

        sculkExplosion(result.getBlockPos(), world, 15, user);

        user.getItemCooldownManager().set(asItem(), 10 * 20);
        user.getStackInHand(hand).damage(2, user, (entity) -> {
            entity.sendToolBreakStatus(hand);
        });

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void sched(Runnable runnable, long millis) {
        MythicalStaves.scheduler.schedule(runnable, millis, TimeUnit.MILLISECONDS);
    }

    private void sculkExplosion(BlockPos center, World world, int radius, LivingEntity caster) {
        ArrayList<BlockPos> posses = Utils.drawSphere(center, radius);
        Random rand = new Random();

        List<Entity> entitiesInRange = Utils.findEntitiesOfTypeInSphere(world, center, radius, caster);
        for(Entity e : entitiesInRange) {
            if(e instanceof LivingEntity livingEntity) {
                livingEntity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS,
                        30*20,// 30 seconds
                        3
                ), caster);
            }
        }

        for(BlockPos pos : posses) {
            BlockState state = world.getBlockState(pos);
            if(state.isAir() || state.getBlock() == Blocks.SCULK_CATALYST || state.getBlock() == Blocks.SCULK_SHRIEKER || state.getBlock() == Blocks.SCULK_SENSOR) continue;

            if(world.getBlockState(pos.add(0, 1, 0)).isAir()) {
                int random = rand.nextInt(100);

                if(random == 66 || random == 77) {
                    world.setBlockState(pos.add(0, 1, 0), Blocks.SCULK_SHRIEKER.getDefaultState().with(SculkShriekerBlock.CAN_SUMMON, true));
                }

                if(random == 25 || random == 89) {
                    world.setBlockState(pos.add(0, 1, 0), Blocks.SCULK_CATALYST.getDefaultState());
                }

                if(random == 41 || random == 78) {
                    world.setBlockState(pos.add(0, 1, 0), Blocks.SCULK_SENSOR.getDefaultState());
                }
            }
            world.setBlockState(pos, Blocks.SCULK.getDefaultState());
        }
    }
}
