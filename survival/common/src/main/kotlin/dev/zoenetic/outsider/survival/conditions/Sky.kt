package dev.zoenetic.outsider.survival.conditions

import dev.zoenetic.outsider.survival.units.Sky
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.lighting.LightEngine

public fun Player.getSky(): Sky {
    val level = level()
    val pos = blockPosition()
    val brightness = level.getBrightness(LightLayer.SKY, pos)
    val maxBrightness = LightEngine.MAX_LEVEL
    return Sky.fromBrightness(brightness, maxBrightness)
}

public fun Player.isUnderOpenSky(): Boolean {
    val level = level()
    val pos = blockPosition()
    return level.canSeeSky(pos)
}