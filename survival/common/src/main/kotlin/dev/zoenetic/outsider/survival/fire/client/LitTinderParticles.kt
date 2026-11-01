package dev.zoenetic.outsider.survival.fire.client

import dev.zoenetic.outsider.survival.tools.TinderItem
import dev.zoenetic.outsider.survival.tools.TinderItem.Companion.isLitTinder
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

private const val FIRST_PERSON_SIDE: Double = 0.56
private const val FIRST_PERSON_DROP: Double = 0.45
private const val FIRST_PERSON_FORWARD: Double = 0.72

// client only
public fun litTinderParticles(minecraft: Minecraft, level: ClientLevel) {
    for (player in level.players()) {
        if (player === minecraft.cameraEntity &&
            minecraft.options.cameraType.isFirstPerson
        ) {
            continue
        }
        for (hand in InteractionHand.entries) {
            if (!player.getItemInHand(hand).isLitTinder()) continue
            val at = TinderItem.handPosition(player, hand)
            level.addParticle(ParticleTypes.SMOKE, at.x, at.y, at.z, 0.0, 0.02, 0.0)
        }
    }
}

private fun firstPersonHand(player: Player, hand: InteractionHand): Vec3 {
    val rightHand = (hand == InteractionHand.MAIN_HAND) == (player.mainArm == HumanoidArm.RIGHT)
    val forward = player.getViewVector(1F)
    val up = player.getUpVector(1F)
    val right = forward.cross(up)
    return player.eyePosition
        .add(right.scale(if (rightHand) FIRST_PERSON_SIDE else -FIRST_PERSON_SIDE))
        .add(up.scale(-FIRST_PERSON_DROP))
        .add(forward.scale(FIRST_PERSON_FORWARD))
}
