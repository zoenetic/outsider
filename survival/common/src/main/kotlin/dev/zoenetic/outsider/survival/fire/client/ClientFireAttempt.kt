package dev.zoenetic.outsider.survival.fire.client

import dev.zoenetic.outsider.survival.fire.LIGHTING_FIRE_GRACE_PERIOD
import net.minecraft.client.player.LocalPlayer

// client only
public object ClientFireAttempt {

    public fun LocalPlayer.isAttemptingToLightAFire(): Boolean {
        val lastAttempt = LAST_ATTEMPT ?: return false
        return level().gameTime - lastAttempt in 0..LIGHTING_FIRE_GRACE_PERIOD
    }

    public fun record(gameTime: Long) {
        LAST_ATTEMPT = gameTime
    }

    internal var LAST_ATTEMPT: Long? = null
}
