package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.getValue
import net.minecraft.sounds.SoundEvent

public object OutsiderSounds {

    public val BREATH: SoundEvent by
        Survival.platform.register.sound("breath") {
            SoundEvent.createFixedRangeEvent(it, 0F)
        }

    public val FIRE_FAILURE: SoundEvent by
        Survival.platform.register.sound("fire_failure") {
            SoundEvent.createVariableRangeEvent(it)
        }

    public val FIRE_SUCCESS: SoundEvent by
        Survival.platform.register.sound("fire_success") {
            SoundEvent.createVariableRangeEvent(it)
        }

    public val HEARTBEAT: SoundEvent by
        Survival.platform.register.sound("heartbeat") {
            SoundEvent.createFixedRangeEvent(it, 0F)
        }

    public val REFUEL_FIRE: SoundEvent by
        Survival.platform.register.sound("refuel_fire") {
            SoundEvent.createVariableRangeEvent(it)
        }

    public fun init() {}
}
