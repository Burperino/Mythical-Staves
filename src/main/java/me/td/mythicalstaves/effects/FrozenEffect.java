package me.td.mythicalstaves.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class FrozenEffect extends StatusEffect {
    public FrozenEffect() {
        super(StatusEffectCategory.HARMFUL, 0x6be4ff);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        super.applyUpdateEffect(entity, amplifier);

        if(entity.getWorld().isClient || !(entity instanceof MobEntity)) return;

        NbtCompound nbt = entity.writeNbt(new NbtCompound());
        if(!nbt.getBoolean("NoAI")) {
            nbt.putBoolean("NoAI", true);
            entity.readNbt(nbt);
        }
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);

        if (entity.getWorld().isClient || entity instanceof PlayerEntity) return;

        NbtCompound nbt = entity.writeNbt(new NbtCompound());
        nbt.putBoolean("NoAI", false);
        entity.readNbt(nbt);
    }
}
