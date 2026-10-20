package dev.zoenetic.outsider.survival.gametest

import net.minecraft.server.level.ServerPlayer

// NeoForge drops modded payloads on a connection that never negotiated channels, which a mock
// player's never does; its test bootstrap fills this in. Fabric needs nothing.
object MockConnections {
    var prepareForPayloads: (ServerPlayer) -> Unit = {}
}
