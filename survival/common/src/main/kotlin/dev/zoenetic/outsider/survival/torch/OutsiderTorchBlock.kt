package dev.zoenetic.outsider.survival.torch

import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.outsider.survival.emission.EmittingBlock
import dev.zoenetic.outsider.survival.emission.LightTable
import dev.zoenetic.outsider.survival.fire.FireInteractions
import dev.zoenetic.outsider.survival.fuel.Burnout
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.fuel.FuelledBlock
import dev.zoenetic.outsider.survival.registry.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Light
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.TorchBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT
import net.minecraft.world.phys.BlockHitResult

public open class OutsiderTorchBlock(flameParticle: SimpleParticleType, properties: Properties) :
    TorchBlock(flameParticle, properties),
    EmittingBlock,
    FuelledBlock {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(LIT, false)
                .setValue(FUEL_LEVEL, 15),
        )
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (!state.getValue(LIT)) return
        super.animateTick(state, level, pos, random)
    }

    protected override fun createBlockStateDefinition(
        builder: StateDefinition.Builder<Block, BlockState>,
    ) {
        builder
            .add(LIT)
            .add(FUEL_LEVEL)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? =
        super.getStateForPlacement(context)?.setValue(
            LIT,
            context.itemInHand.has(
                OutsiderComponents.LIT,
            ),
        )?.setValue(
            FUEL_LEVEL,
            context.itemInHand.getOrDefault(OutsiderComponents.FUEL_LEVEL, Fuel.MAX).level,
        )

    override fun setPlacedBy(
        level: Level,
        pos: BlockPos,
        state: BlockState,
        by: LivingEntity?,
        itemStack: ItemStack,
    ) {
        itemStack.remove(OutsiderComponents.LIT)
        super.setPlacedBy(level, pos, state, by, itemStack)
    }

    override fun useItemOn(
        itemStack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult,
    ): InteractionResult {
        val lightWithItem =
            FireInteractions.maybeLightWithLitItem(state, itemStack, level, pos, player)
        if (lightWithItem != InteractionResult.PASS) return lightWithItem
        return FireInteractions.maybeLightFromLitBlock(state, itemStack, level, pos, player)
    }

    override val lightTable: LightTable = LightTable.IDENTITY

    override val maxLight: Light get() = Light(lightTable[Fuel.MAX.level])

    override val burnRate: Duration get() = Duration(200L)

    internal val props: Properties get() = properties
    internal val flame: SimpleParticleType get() = flameParticle

    override val maxHeat: Heat = Heat(6.0) // TODO: fix

    override fun getHeat(state: BlockState): Heat = if (state.getValue(LIT)) maxHeat else Heat(0.0)

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
        OutsiderBlocks.DEAD_TORCH.defaultBlockState()

    public companion object {
        public val PARTICLE_OPTIONS_FIELD: MapCodec<SimpleParticleType> =
            BuiltInRegistries.PARTICLE_TYPE.byNameCodec().comapFlatMap(
                { type ->
                    if (type is SimpleParticleType) {
                        DataResult.success(type)
                    } else {
                        DataResult.error { "Not a SimpleParticleType: $type" }
                    }
                },
                { type -> type },
            ).fieldOf("particle_options")

        public val CODEC: MapCodec<OutsiderTorchBlock> =
            RecordCodecBuilder.mapCodec { i: RecordCodecBuilder.Instance<OutsiderTorchBlock> ->
                i.group(
                    PARTICLE_OPTIONS_FIELD.forGetter { b -> b.flame },
                    Properties.CODEC.fieldOf("properties").forGetter { b -> b.props },
                ).apply(i, ::OutsiderTorchBlock)
            }
    }
}
