package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.work.WorkingModeInput;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick()V", at = @At("TAIL"))
    private void outsider_survival$lockMovementWhileWorking(CallbackInfo ci) {
        if (!WorkingModeInput.isLocked()) return;
        keyPresses = WorkingModeInput.lock(keyPresses);
        moveVector = Vec2.ZERO;
    }
}
