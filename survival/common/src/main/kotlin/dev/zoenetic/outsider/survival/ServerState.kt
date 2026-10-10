package dev.zoenetic.outsider.survival

import dev.zoenetic.outsider.survival.fire.FireAttempts
import dev.zoenetic.outsider.survival.fuel.DropSchedule
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

public object ServerState {
    private var fireAttempts: FireAttempts? = null
    private val dropSchedules: MutableMap<ResourceKey<Level>, DropSchedule> = mutableMapOf()

    public fun onServerStarting() {
        fireAttempts = FireAttempts()
    }

    public fun onServerStopped() {
        fireAttempts = null
        dropSchedules.clear()
    }

    public fun fireAttempts(): FireAttempts =
        fireAttempts ?: error("accessed fire attempts state outside a running server")

    public fun dropSchedule(level: Level): DropSchedule = dropSchedules
        .getOrPut(level.dimension()) { DropSchedule() }

    public fun init(): Unit = Unit
}
