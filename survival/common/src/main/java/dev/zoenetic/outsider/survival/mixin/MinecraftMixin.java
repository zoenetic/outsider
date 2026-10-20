package dev.zoenetic.outsider.survival.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static dev.zoenetic.outsider.survival.work.client.MaybeCloseWorkKt.maybeCloseWork;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @WrapWithCondition(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private boolean outsider_survival$shouldShowInventory(Gui instance, Screen screen) {
        @Nullable LocalPlayer player = ((Minecraft) (Object) this).player;
        if (player == null) return true;
        boolean didCloseWork = maybeCloseWork(player);
        return !didCloseWork;
    }
}
