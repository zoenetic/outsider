package dev.zoenetic.outsider.survival.mixin;

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.zoenetic.outsider.survival.work.MaybeMoveOutOfWorkModeKt.maybeMoveOutOfWorkMode;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Inject(method = "handlePlayerInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setLastClientInput(Lnet/minecraft/world/entity/player/Input;)V"))
    private void outsider_survival$maybeMoveOutOfWorkMode(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        ServerPlayer player = ((ServerGamePacketListenerImpl) (Object) this).player;
        maybeMoveOutOfWorkMode(player, player.getLastClientInput(), packet.input());
    }
}
