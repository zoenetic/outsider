package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.campfire.OutsiderCampfireBlock
import dev.zoenetic.outsider.survival.campfire.OutsiderDeadCampfireBlock
import dev.zoenetic.outsider.survival.fuel.firewood.FirewoodBlock
import dev.zoenetic.outsider.survival.platform.getValue
import dev.zoenetic.outsider.survival.torch.OutsiderDeadTorchBlock
import dev.zoenetic.outsider.survival.torch.OutsiderDeadWallTorchBlock
import dev.zoenetic.outsider.survival.torch.OutsiderTorchBlock
import dev.zoenetic.outsider.survival.torch.OutsiderWallTorchBlock
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.block.Block

public object OutsiderBlocks {

    public val CAMPFIRE: Block by Survival.platform.register.block(
        "campfire",
        { props -> OutsiderCampfireBlock(true, 1, props) },
        { OutsiderBlockBehaviour.Properties.campfire() },
    )

    public val DEAD_CAMPFIRE: Block by Survival.platform.register.block(
        "dead_campfire",
        { props -> OutsiderDeadCampfireBlock(props) },
        { OutsiderBlockBehaviour.Properties.deadCampfire() },
    )

    public val FIREWOOD: Block by Survival.platform.register.block(
        "firewood",
        { props -> FirewoodBlock(props) },
        { OutsiderBlockBehaviour.Properties.firewood() },
    )

    public val TORCH: Block by Survival.platform.register.block(
        "torch",
        { props -> OutsiderTorchBlock(ParticleTypes.FLAME, props) },
        { OutsiderBlockBehaviour.Properties.torch() },
    )

    public val WALL_TORCH: Block by Survival.platform.register.block(
        "wall_torch",
        { props -> OutsiderWallTorchBlock(ParticleTypes.FLAME, props) },
        { OutsiderBlockBehaviour.Properties.wallTorch() },
    )

    public val DEAD_TORCH: Block by Survival.platform.register.block(
        "dead_torch",
        { props -> OutsiderDeadTorchBlock(props) },
        { OutsiderBlockBehaviour.Properties.deadTorch() },
    )

    public val DEAD_WALL_TORCH: Block by Survival.platform.register.block(
        "dead_wall_torch",
        { props -> OutsiderDeadWallTorchBlock(props) },
        { OutsiderBlockBehaviour.Properties.deadWallTorch() },
    )

    public val ALL: List<Block>
        get() = listOf(
            CAMPFIRE,
            FIREWOOD,
            TORCH,
            WALL_TORCH,
            DEAD_TORCH,
            DEAD_WALL_TORCH,
        )

    public fun init() {}
}
