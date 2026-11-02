package dev.zoenetic.outsider.survival.work.client

import com.mojang.blaze3d.vertex.PoseStack
import dev.zoenetic.outsider.survival.animation.client.player.splitLegs
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.player.PlayerModel
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import org.joml.Vector3f
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KneelingAnimationTests {

    private fun kneelingModel(strength: Float): HumanoidModel<HumanoidRenderState> {
        val mesh = PlayerModel.createMesh(CubeDeformation.NONE, false)
        splitLegs(mesh.root, CubeDeformation.NONE)
        val model = HumanoidModel<HumanoidRenderState>(
            LayerDefinition.create(mesh, 64, 64).bakeRoot(),
        )
        KneelingAnimation().thirdPerson(strength, model, HumanoidRenderState())
        return model
    }

    // y grows downwards in model space, in pixels
    private fun skinCorners(leg: ModelPart): List<Vector3f> {
        val corners = mutableListOf<Vector3f>()
        leg.visit(PoseStack()) { pose, path, _, cube ->
            if ("pants" in path) return@visit
            for (polygon in cube.polygons) {
                for (vertex in polygon.vertices) {
                    corners += pose.pose()
                        .transformPosition(Vector3f(vertex.worldX(), vertex.worldY(), vertex.worldZ()))
                        .mul(16F)
                }
            }
        }
        return corners
    }

    private fun lowest(leg: ModelPart): Float = skinCorners(leg).maxOf { it.y }

    @Test
    fun `both legs rest on the ground when fully knelt`() {
        val model = kneelingModel(1F)
        assertEquals(GROUND, lowest(model.leftLeg), EPSILON, "trailing leg")
        assertEquals(GROUND, lowest(model.rightLeg), EPSILON, "front leg")
    }

    @Test
    fun `the trailing shin lies flat along the ground`() {
        val model = kneelingModel(1F)
        val onGround = skinCorners(model.leftLeg).count { kotlin.math.abs(it.y - GROUND) < EPSILON }
        assertTrue(onGround >= 4, "only $onGround corners of the trailing leg touch the ground")
    }

    @Test
    fun `the front foot is planted flat`() {
        val model = kneelingModel(1F)
        val sole = skinCorners(model.rightLeg).filter { kotlin.math.abs(it.y - GROUND) < EPSILON }
        assertTrue(sole.size >= 4, "the front foot touches the ground at ${sole.size} corners")
    }

    @Test
    fun `the trailing thigh stands straight up from the knee`() {
        assertEquals(0F, kneelingModel(1F).leftLeg.xRot, EPSILON)
    }

    @Test
    fun `the trailing shin reaches 4 px behind the thigh`() {
        val model = kneelingModel(1F)
        val back = skinCorners(model.leftLeg).maxOf { it.z }
        assertEquals(THIGH_BACK + 4F, back, EPSILON)
    }

    @Test
    fun `nothing pokes below the ground`() {
        val model = kneelingModel(1F)
        val corners = skinCorners(model.leftLeg) + skinCorners(model.rightLeg)
        assertTrue(corners.all { it.y <= GROUND + EPSILON })
    }

    @Test
    fun `legs without shins, as on armour, still kneel`() {
        val model = HumanoidModel<HumanoidRenderState>(
            LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0F), 64, 64)
                .bakeRoot(),
        )
        KneelingAnimation().thirdPerson(1F, model, HumanoidRenderState())
    }

    private companion object {
        const val GROUND = 24F
        const val THIGH_BACK = 2F
        const val EPSILON = 1e-3F

    }
}
