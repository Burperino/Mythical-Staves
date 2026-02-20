package me.td.mythicalstaves.effects;

import me.td.mythicalstaves.MythicalStaves;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Effects {
    public static final FrozenEffect FROZEN_EFFECT = register("frozen", new FrozenEffect());

    public static <T extends StatusEffect> T register(String path, T statusEffect) {
        return Registry.register(Registries.STATUS_EFFECT, Identifier.of(MythicalStaves.MOD_ID, path), statusEffect);
    }

    public static void initialize() { // Hack to statically initialize the class. This gets called in the mod entry
    }
}
