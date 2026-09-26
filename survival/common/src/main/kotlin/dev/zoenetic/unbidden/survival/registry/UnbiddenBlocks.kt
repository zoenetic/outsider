package dev.zoenetic.unbidden.survival.registry

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.campfire.UnbiddenCampfireBlock
import dev.zoenetic.unbidden.survival.campfire.UnbiddenDeadCampfireBlock
import dev.zoenetic.unbidden.survival.fuel.firewood.FirewoodBlock
import dev.zoenetic.unbidden.survival.platform.getValue
import dev.zoenetic.unbidden.survival.torch.UnbiddenTorchBlock
import dev.zoenetic.unbidden.survival.torch.UnbiddenWallTorchBlock
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.block.Block

public object UnbiddenBlocks {

    public val CAMPFIRE: Block by Survival.platform.register.block(
        "campfire",
        { props -> UnbiddenCampfireBlock(true, 1, props) },
        { UnbiddenBlockBehaviour.Properties.campfire() }
    )

    public val DEAD_CAMPFIRE: Block by Survival.platform.register.block(
        "dead_campfire",
        { props -> UnbiddenDeadCampfireBlock(props) },
        { UnbiddenBlockBehaviour.Properties.deadCampfire() }
    )

    public val FIREWOOD: Block by Survival.platform.register.block(
        "firewood",
        { props -> FirewoodBlock(props) },
        { UnbiddenBlockBehaviour.Properties.firewood() }
    )

    public val TORCH: Block by Survival.platform.register.block(
        "torch",
        { props -> UnbiddenTorchBlock(ParticleTypes.FLAME, props) },
        { UnbiddenBlockBehaviour.Properties.torch() }
    )

    public val WALL_TORCH: Block by Survival.platform.register.block(
        "wall_torch",
        { props -> UnbiddenWallTorchBlock(ParticleTypes.FLAME, props) },
        { UnbiddenBlockBehaviour.Properties.wallTorch() }
    )

    public val ALL: List<Block>
        get() = listOf(
            CAMPFIRE, FIREWOOD, TORCH, WALL_TORCH
        )

    public fun init() {}
}