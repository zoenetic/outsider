package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.emission.EmitterIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void outsider_survival$afterSetBlockState(BlockPos pos, BlockState state, int flags,
                                                   CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() == null) return;
        var oldState = cir.getReturnValue();
        var chunk = (LevelChunk) (Object) this;
        if (chunk.getLevel().isClientSide()) return;
        EmitterIndex.onBlockChanged(chunk, pos, oldState, state);
    }
}

