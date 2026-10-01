package dev.zoenetic.outsider.survival.campfire

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.outsider.survival.emission.EmittingBlock
import dev.zoenetic.outsider.survival.emission.LightTable
import dev.zoenetic.outsider.survival.fire.FireInteractions
import dev.zoenetic.outsider.survival.fuel.Burnout
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.fuel.FuelledBlock
import dev.zoenetic.outsider.survival.registry.OutsiderBlockStateProperties
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Light
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.BlockHitResult

public open class OutsiderCampfireBlock(
    private val spawnParticles: Boolean,
    private val fireDamage: Int,
    properties: Properties,
) : CampfireBlock(spawnParticles, fireDamage, properties), EmittingBlock, FuelledBlock {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(LIT, false)
                .setValue(SIGNAL_FIRE, false)
                .setValue(WATERLOGGED, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(FUEL_LEVEL, Fuel.MAX.level)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun codec(): MapCodec<CampfireBlock> = CODEC as MapCodec<CampfireBlock>

    override val maxHeat: Heat
        get() = Heat(20.0)

    override val maxLight: Light get() = Light(lightTable[Fuel.MAX.level])

    override val burnRate: Duration get() = Duration(800L)

    override val lightTable: LightTable get() = LightTable.IDENTITY

    override fun getHeat(state: BlockState): Heat = if (state.getValue(LIT)) maxHeat else Heat.ZERO

    override fun getLight(state: BlockState): Light {
        if (!state.getValue(LIT)) return Light.ZERO
        return Light(lightTable[getFuel(state).level])
    }

    override fun getFuel(state: BlockState): Fuel = Fuel(state.getValue(FUEL_LEVEL))

    override fun setFuel(state: BlockState, fuel: Fuel): BlockState {
        val newValue = fuel.coerceAtMost(Fuel.MAX)
        return state.setValue(FUEL_LEVEL, newValue.level)
    }

    override fun getBurnout(existingBurnout: Time?, now: Time, fuel: Fuel): Burnout =
        Burnout.forFuel(existingBurnout, now, fuel, Fuel.MAX, burnRate)

    override fun exhausted(state: BlockState): BlockState =
        OutsiderBlocks.DEAD_CAMPFIRE.defaultBlockState()
            .setValue(WATERLOGGED, state.getValue(WATERLOGGED))
            .setValue(FACING, state.getValue(FACING))

    override fun useItemOn(
        itemStack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        val refuel = FireInteractions.maybeRefuel(state, itemStack, level, pos, player)
        if (refuel != InteractionResult.PASS) return refuel
        val lightTorch =
            FireInteractions.maybeLightFromLitBlock(state, itemStack, level, pos, player)
        if (lightTorch != InteractionResult.PASS) return lightTorch
        return FireInteractions.maybeLightWithLitItem(state, itemStack, level, pos, player)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult = FireInteractions.maybeLightWithHandDrill(state, level, pos, player)

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val level = context.level
        val pos = context.clickedPos
        val replacedWater = level.getFluidState(pos).`is`(Fluids.WATER)
        return defaultBlockState()
            .setValue(WATERLOGGED, replacedWater)
            .setValue(
                SIGNAL_FIRE,
                isSmokeSource(level.getBlockState(pos.below()))
            )
            .setValue(LIT, false)
            .setValue(FACING, context.horizontalDirection)
    }

    public override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING, FUEL_LEVEL)
    }

    private fun isSmokeSource(blockState: BlockState): Boolean = blockState.`is`(Blocks.HAY_BLOCK)

    public companion object {
        public val CODEC: MapCodec<OutsiderCampfireBlock> =
            RecordCodecBuilder.mapCodec { i: RecordCodecBuilder.Instance<OutsiderCampfireBlock> ->
                i.group(
                    Codec.BOOL.fieldOf("spawn_particles").forGetter { b -> b.spawnParticles },
                    Codec.intRange(0, 1000).fieldOf("fire_damage").forGetter { b -> b.fireDamage },
                    propertiesCodec(),
                ).apply(i, ::OutsiderCampfireBlock)
            }

        public val FUEL_LEVEL: IntegerProperty = OutsiderBlockStateProperties.FUEL_LEVEL
        public val LIT: BooleanProperty = BlockStateProperties.LIT
        public val SIGNAL_FIRE: BooleanProperty = BlockStateProperties.SIGNAL_FIRE
        public val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
        public val FACING: EnumProperty<Direction> = BlockStateProperties.HORIZONTAL_FACING
        public const val SMOKE_DISTANCE: Int = 5
    }
}