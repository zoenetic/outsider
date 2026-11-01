package dev.zoenetic.outsider.survival.fire.client

import com.mojang.serialization.MapCodec
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.fire.emberProgress
import dev.zoenetic.outsider.survival.work.Work
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.ItemOwner
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

public object EmberHeat : RangeSelectItemModelProperty {
    public val ID: Identifier = Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "ember_heat")
    public val MAP_CODEC: MapCodec<EmberHeat> = MapCodec.unit(EmberHeat)

    override fun get(
        itemStack: ItemStack,
        level: ClientLevel?,
        owner: ItemOwner?,
        seed: Int,
    ): Float {
        val player = owner?.asLivingEntity() as? Player ?: return 0F
        val state = Work.get(player) as? LightingFireState ?: return 0F
        val phase = state.phase as? LightingFireState.NursingEmbers ?: return 0F
        return emberProgress(phase)
    }

    override fun type(): MapCodec<out RangeSelectItemModelProperty> = MAP_CODEC
}
