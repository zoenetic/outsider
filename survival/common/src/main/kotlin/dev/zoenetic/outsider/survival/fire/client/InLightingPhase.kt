package dev.zoenetic.outsider.survival.fire.client

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.work.Work
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

public data class InLightingPhase(val phase: LightingFireState.Phase.Kind) :
    ConditionalItemModelProperty {

    override fun get(
        itemStack: ItemStack,
        level: ClientLevel?,
        owner: LivingEntity?,
        seed: Int,
        displayContext: ItemDisplayContext,
    ): Boolean {
        if (displayContext !in IN_HAND) return false
        val state =
            (owner as? Player)?.let(Work.Companion::get) as? LightingFireState ?: return false
        return state.phase.kind == phase
    }

    override fun type(): MapCodec<out ConditionalItemModelProperty> = MAP_CODEC

    public companion object {
        public val ID: Identifier = Identifier.fromNamespaceAndPath(
            Survival.NAMESPACE,
            "lighting_phase",
        )

        private val PHASE_CODEC: Codec<LightingFireState.Phase.Kind> = Codec.STRING.comapFlatMap(
            { name ->
                LightingFireState.Phase.Kind.entries.firstOrNull { it.name.lowercase() == name }
                    ?.let { DataResult.success(it) }
                    ?: DataResult.error { "Unknown lighting phase: $name" }
            },
            {
                it.name.lowercase()
            },
        )

        public val MAP_CODEC: MapCodec<InLightingPhase> = RecordCodecBuilder.mapCodec {
            it.group(PHASE_CODEC.fieldOf("phase").forGetter(InLightingPhase::phase))
                .apply(it, ::InLightingPhase)
        }

        private val IN_HAND = setOf(
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
            ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
        )
    }
}
