package dev.zoenetic.outsider.survival.work.client

import dev.zoenetic.outsider.survival.SurvivalClient
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.WorkClosePacketPayload
import net.minecraft.client.player.LocalPlayer

public fun maybeCloseWork(player: LocalPlayer): Boolean {
    val _ = Work.get(player) ?: return false
    SurvivalClient.platform.sendToServer(WorkClosePacketPayload)
    return true
}
