package me.td.mythicalstaves.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.Optional;

public class StaffOfGamblingItem extends Item {
    public Random RANDOM = Random.create();
    public java.util.Random JRANDOM = new java.util.Random();

    public StaffOfGamblingItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(target.getWorld().isClient) return super.postHit(stack, target, attacker);

        Optional<RegistryEntry.Reference<StatusEffect>> effectOption = Registries.STATUS_EFFECT.getRandom(RANDOM);
        if(!effectOption.isPresent()) {
            attacker.sendMessage(Text.literal("Something went wrong while getting a random effect."));
            return super.postHit(stack, target, attacker);
        }

        int amp = JRANDOM.nextInt(0, 10); // 1 to 10 (because - 1)
        int ticks = JRANDOM.nextInt(5, 60) * 20;

        StatusEffect statusEffect = effectOption.get().value();

        target.addStatusEffect(new StatusEffectInstance(statusEffect, ticks, amp), attacker);


        target.getWorld().playSound(
                null, // No specific player, so it's audible to all in range
                target.getBlockPos(), // The position at which to play the sound
                SoundEvents.BLOCK_BELL_USE, // The sound event to play
                SoundCategory.AMBIENT, // Sound category for volume control
                1.0f, // Volume, between 0.0 and 1.0
                1.0f  // Pitch, where 1.0 is the normal pitch
        );

        attacker.sendMessage(Text.literal(String.format("%s%s %d for %d seconds",
                (statusEffect.isBeneficial() ? Formatting.GREEN : Formatting.RED), statusEffect.getName().getString(), amp + 1, ticks / 20)));

        stack.damage(1, attacker, (entity) -> {
            entity.sendToolBreakStatus(attacker.getActiveHand());
        });

        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(world.isClient) return TypedActionResult.pass(user.getStackInHand(hand));

        Optional<RegistryEntry.Reference<Item>> itemOption = Registries.ITEM.getRandom(RANDOM);
        if(!itemOption.isPresent()) {
            user.sendMessage(Text.literal("Something went wrong while getting a random item."));
            return TypedActionResult.fail(user.getStackInHand(hand));
        }

        Item item = itemOption.get().value();
        ItemStack stack = new ItemStack(item, JRANDOM.nextInt(1, Math.min(item.getMaxCount(), 16) + 1));

        user.getItemCooldownManager().set(asItem(), 20);
        user.getStackInHand(hand).damage(2, user, (entity) -> {
            entity.sendToolBreakStatus(hand);
        });

        user.getWorld().playSound(
                null, // No specific player, so it's audible to all in range
                user.getBlockPos(), // The position at which to play the sound
                SoundEvents.ENTITY_VILLAGER_YES, // The sound event to play
                SoundCategory.AMBIENT, // Sound category for volume control
                1.0f, // Volume, between 0.0 and 1.0
                1.0f  // Pitch, where 1.0 is the normal pitch
        );

        user.getInventory().insertStack(stack);

        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
