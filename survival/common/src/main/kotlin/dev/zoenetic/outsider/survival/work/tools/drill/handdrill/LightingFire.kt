package dev.zoenetic.outsider.survival.work.tools.drill.handdrill

import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockTags.LIGHTABLE_WITH_DRILL
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.WorkContext
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState.Drilling
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState.NursingEmbers
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.properties.BlockStateProperties

public object LightingFire {

    public fun start(
        level: ServerLevel,
        player: ServerPlayer,
        hand: InteractionHand,
        blockPos: BlockPos,
    ) {
        val state = LightingFireState(
            blockPos,
            phase = drilling(hand, blockPos, WorkContext.of(level, player)),
        )
        Work.start(player, state)
    }

    public fun blowingRequired(): Duration = Duration(20) // TODO: Make this real

    public fun conditions(level: Level, blockPos: BlockPos): DrillingConditions =
        // TODO: Make this real
        DrillingConditions(
            ambient = Heat(20.0),
            risePerTick = Heat(4.0),
            halfLife = Duration(40),
            ignition = Heat(400.0),
        )

    public fun dustRequired(context: WorkContext): Duration = Duration(
        context.level.random.triangle(
            HandDrillItem.BASE_DUST_TICKS.toDouble(),
            HandDrillItem.DUST_JITTER_TICKS.toDouble(),
        ).toLong(),
    )

    public fun drilling(hand: InteractionHand, blockPos: BlockPos, context: WorkContext): Drilling {
        val conditions = conditions(context.level, blockPos)
        return Drilling(
            hand = hand,
            dust = Duration.NONE,
            dustRequired = dustRequired(context),
            heat = conditions.ambient,
            held = false,
            timestamp = context.now,
        )
    }

    public fun embersLifeTime(): Duration = Duration(20) // TODO: Make this real

    public fun Level.isLightable(blockPos: BlockPos): Boolean {
        val blockState = getBlockState(blockPos)
        return blockState.`is`(
            LIGHTABLE_WITH_DRILL,
        ) && !blockState.getValueOrElse(BlockStateProperties.LIT, true)
    }

    public fun nursingEmbers(context: WorkContext): NursingEmbers = NursingEmbers(
        diesAt = context.now + embersLifeTime(),
        blown = Duration.NONE,
        blownRequired = blowingRequired(),
        held = false,
        timestamp = context.now,
    )
}
