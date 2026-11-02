package dev.zoenetic.outsider.survival.animation.client.player

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.player.PlayerModel
import net.minecraft.core.Direction
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class SplitLegsTests {

    private class Face(val corners: List<Vector3f>, val uvs: List<Pair<Float, Float>>) {
        val normal: Vector3f = Vector3f(corners[1]).sub(corners[0])
            .cross(Vector3f(corners[3]).sub(corners[0])).normalize()

        fun uvAt(point: Vector3f): Pair<Float, Float>? {
            val along = Vector3f(corners[1]).sub(corners[0])
            val across = Vector3f(corners[3]).sub(corners[0])
            val offset = Vector3f(point).sub(corners[0])
            if (abs(offset.dot(normal)) > PLANE_EPSILON) return null
            val s = offset.dot(along) / along.lengthSquared()
            val t = offset.dot(across) / across.lengthSquared()
            if (s !in 0F..1F || t !in 0F..1F) return null
            val u = uvs[0].first + s * (uvs[1].first - uvs[0].first) +
                t * (uvs[3].first - uvs[0].first)
            val v = uvs[0].second + s * (uvs[1].second - uvs[0].second) +
                t * (uvs[3].second - uvs[0].second)
            return u to v
        }
    }

    private fun root(split: Boolean): ModelPart {
        val mesh = PlayerModel.createMesh(CubeDeformation.NONE, false)
        if (split) splitLegs(mesh.root, CubeDeformation.NONE)
        return LayerDefinition.create(mesh, 64, 64).bakeRoot()
    }

    private fun faces(leg: ModelPart, pants: Boolean): List<Face> {
        val faces = mutableListOf<Face>()
        leg.x = 0F
        leg.y = 0F
        leg.visit(PoseStack()) { pose, path, _, cube ->
            if (("pants" in path) != pants) return@visit
            for (polygon in cube.polygons) {
                val corners = polygon.vertices.map {
                    pose.pose().transformPosition(Vector3f(it.worldX(), it.worldY(), it.worldZ()))
                }
                faces += Face(corners, polygon.vertices.map { it.u to it.v })
            }
        }
        return faces
    }

    // points on each outer face of a 4 x 12 x 4 leg box, in leg space (y down: UP is the hip end)
    private fun surfacePoints(directions: List<Direction>, growth: Float): List<Vector3f> {
        val lo = -2F - growth
        val hi = 2F + growth
        val top = -growth
        val bottom = 12F + growth
        val across = listOf(-1.5F, -0.5F, 0.5F, 1.5F)
        val down = (0..11).map { it + 0.5F }
        return directions.flatMap { direction ->
            when (direction) {
                Direction.NORTH -> across.flatMap { x -> down.map { y -> Vector3f(x, y, lo) } }
                Direction.SOUTH -> across.flatMap { x -> down.map { y -> Vector3f(x, y, hi) } }
                Direction.WEST -> across.flatMap { z -> down.map { y -> Vector3f(lo, y, z) } }
                Direction.EAST -> across.flatMap { z -> down.map { y -> Vector3f(hi, y, z) } }
                Direction.DOWN -> across.flatMap { x -> across.map { z -> Vector3f(x, top, z) } }
                Direction.UP -> across.flatMap { x -> across.map { z -> Vector3f(x, bottom, z) } }
            }
        }
    }

    private fun assertSameSurface(legName: String, pants: Boolean, directions: List<Direction>) {
        // a grown box stretches its texels over the extra size, so split pants drift by
        // up to the growth at the knee: allow a third of a texel there
        val tolerance = if (pants) PANTS_UV_EPSILON else UV_EPSILON
        val vanilla = faces(root(split = false).getChild(legName), pants)
        val split = faces(root(split = true).getChild(legName), pants)
        val growth = if (pants) 0.25F else 0F
        for (point in surfacePoints(directions, growth).map { it.div(16F) }) {
            val expected = vanilla.firstNotNullOfOrNull { it.uvAt(point) }
                ?: fail("vanilla $legName has no face at $point")
            val found = split.mapNotNull { it.uvAt(point) }
            assertTrue(found.isNotEmpty(), "split $legName has no face at $point")
            for (uv in found) {
                assertEquals(expected.first, uv.first, tolerance, "u at $point on $legName")
                assertEquals(expected.second, uv.second, tolerance, "v at $point on $legName")
            }
        }
    }

    @Test
    fun `a split leg's skin looks like vanilla's, sole included`() {
        for (leg in listOf("left_leg", "right_leg")) {
            assertSameSurface(leg, pants = false, directions = Direction.entries)
        }
    }

    @Test
    fun `split pants look like vanilla's, sole included`() {
        for (leg in listOf("left_leg", "right_leg")) {
            assertSameSurface(leg, pants = true, directions = Direction.entries)
        }
    }

    @Test
    fun `pants without a shin, as on armour, are left alone`() {
        val vanilla = PlayerModel.createMesh(CubeDeformation.NONE, false)
        val model = PlayerModel(LayerDefinition.create(vanilla, 64, 64).bakeRoot(), false)
        syncShinPants(model)
    }

    @Test
    fun `the shin hangs from the knee`() {
        val shin = shinOf(root(split = true).getChild("left_leg"))!!
        assertEquals(KNEE_Y, shin.y)
    }

    private companion object {
        // the shin's sides sit 0.01 px inside the thigh's, so they never z-fight
        const val PLANE_EPSILON = 0.011F / 16F
        // the inset squeezes the shin's texels by 0.01 px across
        const val UV_EPSILON = 0.01F / 64F
        const val PANTS_UV_EPSILON = 0.33F / 64F
    }
}

