package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static dev.zoenetic.outsider.survival.work.client.MaybeCloseWorkKt.maybeCloseWork;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @WrapWithCondition(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"))
    private boolean outsider_survival$shouldPauseGame(Minecraft instance, boolean suppressPauseMenuIfWeReallyArePausing) {
        @Nullable LocalPlayer player = instance.player;
        if (player == null) return true;
        boolean didCloseWork = maybeCloseWork(player);
        return !didCloseWork;
    }
}
