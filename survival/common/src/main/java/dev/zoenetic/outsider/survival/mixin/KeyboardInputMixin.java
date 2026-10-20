package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.work.WorkMode;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void outsider_survival$lockMovementWhileWorking(CallbackInfo ci) {
        if (!WorkMode.isEngaged()) return;
        keyPresses = WorkMode.lockShift(keyPresses);
    }
}
