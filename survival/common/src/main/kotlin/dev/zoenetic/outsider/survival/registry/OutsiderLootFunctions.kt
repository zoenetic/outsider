package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fuel.CopyFuelStateFunction

public object OutsiderLootFunctions {

    public fun init() {
        Survival.platform.register.lootFunction(
            "copy_fuel_state",
            CopyFuelStateFunction.MAP_CODEC,
        )
    }
}
