package me.td.mythicalstaves.mixin;

import me.td.mythicalstaves.effects.Effects;
import me.td.mythicalstaves.interfaces.KeyBindingDisabledAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class KeyBindingMixin implements KeyBindingDisabledAccessor {
    @Shadow private boolean pressed;

    @Unique
    public boolean disabled = false;

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        if(disabled) cir.setReturnValue(false);
    }

    @Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
    public void onSetPressed(boolean pressed, CallbackInfo ci) {
        if(disabled) {
            this.pressed = false;
            ci.cancel();
        }
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
