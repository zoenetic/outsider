package dev.zoenetic.unbidden.survival.campfire

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.unbidden.survival.emission.EmittingBlock
import dev.zoenetic.unbidden.survival.emission.LightTable
import dev.zoenetic.unbidden.survival.fire.CampfireInteractions
import dev.zoenetic.unbidden.survival.fuel.Burnout
import dev.zoenetic.unbidden.survival.fuel.Fuel
import dev.zoenetic.unbidden.survival.fuel.FuelledBlock
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlockStateProperties
import dev.zoenetic.unbidden.survival.units.Duration
import dev.zoenetic.unbidden.survival.units.Heat
import dev.zoenetic.unbidden.survival.units.Light
import dev.zoenetic.unbidden.survival.units.Time
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult

public open class UnbiddenCampfireBlock(
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
                .setValue(FUEL_LEVEL, fuelCapacity.level)
        )
    }

    override val fuelCapacity: Fuel
        get() = Fuel.MAX

    override val maxHeat: Heat
        get() = Heat.ZERO // TODO: Placeholder

    override val maxLight: Light get() = Light(lightTable[fuelCapacity.level])

    override val burnRate: Duration get() = Duration(200L)

    override val lightTable: LightTable = LightTable.IDENTITY

    override fun getHeat(state: BlockState): Heat = if (state.getValue(LIT)) maxHeat else Heat.ZERO

    override fun getLight(state: BlockState): Light {
        if (!state.getValue(LIT)) return Light.ZERO
        return Light(lightTable[getFuel(state).level])
    }

    override fun getFuel(state: BlockState): Fuel = Fuel(state.getValue(FUEL_LEVEL))

    override fun setFuel(state: BlockState, fuel: Fuel): BlockState {
        val newValue = fuel.coerceAtMost(fuelCapacity)
        return state.setValue(FUEL_LEVEL, newValue.level)
    }

    override fun getBurnout(existingBurnout: Time?, now: Time, fuel: Fuel): Burnout =
        Burnout.forFuel(existingBurnout, now, fuel, fuelCapacity, burnRate)

    override fun exhausted(state: BlockState): BlockState = state.setValue(LIT, false)

    override fun useItemOn(
        itemStack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (CampfireInteractions.maybeRefuel(state, itemStack, level, pos, player)) {
            return InteractionResult.SUCCESS
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        return CampfireInteractions.maybeLight(state, level, pos, player)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? =
        this.defaultBlockState().setValue(LIT, false)

    public override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING, FUEL_LEVEL)
    }

    public companion object {
        public val CODEC: MapCodec<UnbiddenCampfireBlock> =
            RecordCodecBuilder.mapCodec { i: RecordCodecBuilder.Instance<UnbiddenCampfireBlock> ->
                i.group(
                    Codec.BOOL.fieldOf("spawn_particles").forGetter { b -> b.spawnParticles },
                    Codec.intRange(0, 1000).fieldOf("fire_damage").forGetter { b -> b.fireDamage },
                    propertiesCodec(),
                ).apply(i, ::UnbiddenCampfireBlock)
            }

        public val FUEL_LEVEL: IntegerProperty = UnbiddenBlockStateProperties.FUEL_LEVEL
        public val LIT: BooleanProperty = BlockStateProperties.LIT
        public val SIGNAL_FIRE: BooleanProperty = BlockStateProperties.SIGNAL_FIRE
        public val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
        public val FACING: EnumProperty<Direction> = BlockStateProperties.HORIZONTAL_FACING
        public const val SMOKE_DISTANCE: Int = 5
    }
}