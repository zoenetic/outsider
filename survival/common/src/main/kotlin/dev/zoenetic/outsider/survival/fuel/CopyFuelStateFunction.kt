package dev.zoenetic.outsider.survival.fuel

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.outsider.survival.registry.OutsiderBlockStateProperties
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import net.minecraft.util.Unit
import net.minecraft.util.context.ContextKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import kotlin.jvm.optionals.getOrNull

public class CopyFuelStateFunction(predicates: List<LootItemCondition>) :
    LootItemConditionalFunction(predicates) {

    override fun codec(): MapCodec<CopyFuelStateFunction> = MAP_CODEC

    override fun getReferencedContextParams(): Set<ContextKey<*>> =
        setOf(LootContextParams.BLOCK_STATE)

    override fun run(itemStack: ItemStack, context: LootContext): ItemStack {
        val state = context.getOptionalParameter(LootContextParams.BLOCK_STATE) ?: return itemStack
        if (state.getValueOrElse(BlockStateProperties.LIT, false)) {
            itemStack.set(OutsiderComponents.LIT, Unit.INSTANCE)
        }
        state.getOptionalValue(OutsiderBlockStateProperties.FUEL_LEVEL).getOrNull()?.let { level ->
            itemStack.set(OutsiderComponents.FUEL_LEVEL, Fuel(level))
        }
        return itemStack
    }

    public companion object {
        public val MAP_CODEC: MapCodec<CopyFuelStateFunction> = RecordCodecBuilder
            .mapCodec { i: RecordCodecBuilder.Instance<CopyFuelStateFunction> ->
                commonFields(i).apply(i, ::CopyFuelStateFunction)
            }

        public fun builder(): Builder<*> = simpleBuilder(::CopyFuelStateFunction)
    }
}
