package dev.zoenetic.outsider.survival.fire.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fire.HearthBoard
import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.work.Work
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt

// client only
public object HearthBoardRenderer {

    private val BOARD = Identifier.fromNamespaceAndPath(
        Survival.NAMESPACE,
        "hand_drill_hearth_board",
    )
    private val EMBER = Identifier.fromNamespaceAndPath(
        Survival.NAMESPACE,
        "hand_drill_hearth_board_ember",
    )

    private const val SCALE = 0.5F

    // keeps the ember's faces above the board so they don't z-fight
    private const val EMBER_LIFT = 0.002F

    private const val FULL_GLOW = 15
    private const val EMBER_FADE_TICKS = 20F

    private val board: ItemStack by lazy { stand(BOARD) }
    private val ember: ItemStack by lazy { stand(EMBER) }

    private fun stand(model: Identifier): ItemStack = ItemStack(OutsiderItems.HAND_DRILL).apply {
        set(DataComponents.ITEM_MODEL, model)
    }

    public fun submit(camera: Vec3, poseStack: PoseStack, collector: SubmitNodeCollector) {
        val minecraft = Minecraft.getInstance()
        val level = minecraft.level ?: return
        val partialTick = minecraft.deltaTracker.getGameTimeDeltaPartialTick(false)
        for (player in level.players()) {
            val state = Work.get(player) as? LightingFireState ?: continue
            val at = HearthBoard.position(player, partialTick)
            val blockPos = BlockPos.containing(at)
            val light = LightCoordsUtil.pack(
                level.getBrightness(LightLayer.BLOCK, blockPos),
                level.getBrightness(LightLayer.SKY, blockPos),
            )
            val yaw = player.getPreciseBodyRotation(partialTick)
            submit(board, player, at.subtract(camera), yaw, 0F, light, poseStack, collector)
            val phase = state.phase as? LightingFireState.NursingEmbers ?: continue
            val glow = LightCoordsUtil.lightCoordsWithEmission(
                light,
                emberGlow(phase, Time(level.gameTime)),
            )
            submit(ember, player, at.subtract(camera), yaw, EMBER_LIFT, glow, poseStack, collector)
        }
    }

    private fun submit(
        stack: ItemStack,
        player: Player,
        at: Vec3,
        yaw: Float,
        lift: Float,
        light: Int,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        offset: Vec2 = Vec2.ZERO,
        scale: Float = 1F,
    ) {
        val renderState = ItemStackRenderState()
        Minecraft.getInstance().itemModelResolver.updateForTopItem(
            renderState,
            stack,
            ItemDisplayContext.NONE,
            player.level(),
            player,
            player.id,
        )
        poseStack.pushPose()
        poseStack.translate(at.x, at.y, at.z)
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw))
        poseStack.translate(0F, SCALE / 32F + lift, 0F)
        poseStack.mulPose(Axis.YP.rotationDegrees(-45F))
        poseStack.mulPose(Axis.XP.rotationDegrees(-90F))
        poseStack.scale(SCALE, SCALE, SCALE)
        poseStack.translate(offset.x, offset.y, 0F)
        poseStack.scale(scale, scale, scale)
        renderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0)
        poseStack.popPose()
    }

    internal fun emberGlow(phase: LightingFireState.NursingEmbers, now: Time): Int {
        if (phase.held) return FULL_GLOW
        val left = (phase.diesAt - now).value / EMBER_FADE_TICKS
        return (FULL_GLOW * left.coerceIn(0F, 1F)).roundToInt()
    }
}
