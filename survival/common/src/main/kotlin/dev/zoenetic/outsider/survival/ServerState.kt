package dev.zoenetic.outsider.survival

import dev.zoenetic.outsider.survival.fire.FireAttempts
import dev.zoenetic.outsider.survival.fuel.DropSchedule
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

public object ServerState {
    private var FIRE_ATTEMPTS: FireAttempts? = null
    private var DROP_SCHEDULES: MutableMap<ResourceKey<Level>, DropSchedule>? = null

    public fun onServerStarting() {
        FIRE_ATTEMPTS = FireAttempts()
        DROP_SCHEDULES = mutableMapOf()
    }

    public fun onServerStopped() {
        FIRE_ATTEMPTS = null
        DROP_SCHEDULES = null
    }

    public fun fireAttempts(): FireAttempts =
        FIRE_ATTEMPTS ?: error("accessed fire attempts state outside a running server")

    public fun dropSchedule(level: Level): DropSchedule =
        (DROP_SCHEDULES ?: error("accessed drop schedule state outside a running server"))
            .getOrPut(level.dimension()) { DropSchedule() }


    public fun init() {}

}