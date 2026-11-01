package dev.zoenetic.outsider.survival.fire.client

import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.tools.TinderItem
import dev.zoenetic.outsider.survival.work.Work
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand

// client only
public fun carryHoldIntoBlowing(minecraft: Minecraft) {
    val player = minecraft.player ?: return
    val gameMode = minecraft.gameMode ?: return
    if (!minecraft.options.keyUse.isDown) return
    val state = Work.get(player) as? LightingFireState ?: return
    if (state.phase !is LightingFireState.NursingEmbers) return
    if (player.isUsingItem && player.useItem.item is TinderItem) return
    val tinderHand = InteractionHand.entries.firstOrNull {
        player.getItemInHand(it).item is TinderItem
    } ?: return
    if (player.isUsingItem) gameMode.releaseUsingItem(player)
    gameMode.useItem(player, tinderHand)
}
