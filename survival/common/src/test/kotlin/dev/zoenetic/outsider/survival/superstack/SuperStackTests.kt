package dev.zoenetic.outsider.survival.superstack

import dev.zoenetic.outsider.survival.CommonFixtures
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemInstance
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SuperStackTests {

    private val mostWornFirst: Comparator<ItemInstance> =
        compareByDescending { it.getOrDefault(DataComponents.REPAIR_COST, 0) }

    private fun stick(wear: Int, count: Int = 1): ItemStack =
        ItemStack(Items.STICK, count).apply { set(DataComponents.REPAIR_COST, wear) }

    private fun wearOf(item: ItemInstance): Int = item.getOrDefault(DataComponents.REPAIR_COST, 0)

    private class Container(rule: Comparator<ItemInstance>? = null) {
        val stack = ItemStack(Items.BUNDLE)
        val type = SuperStackType(Items.STICK, rule)
        fun view() = SuperStack(stack, type)
        fun fill(vararg stacks: ItemStack) = stacks.forEach { val _ = view().insert(it) }
        val groups: List<ItemStackTemplate> get() = stack.get(DataComponents.BUNDLE_CONTENTS)!!.items()
    }

    @Test
    fun `a non-base item is rejected and left untouched`() {
        val container = Container()
        val dirt = ItemStack(Items.DIRT, 5)

        assertEquals(0, container.view().insert(dirt))
        assertEquals(5, dirt.count)
        assertEquals(0, container.view().count)
    }

    @Test
    fun `inserting moves the items out of the source stack`() {
        val container = Container()
        val source = stick(wear = 0, count = 10)

        assertEquals(10, container.view().insert(source))
        assertTrue(source.isEmpty)
        assertEquals(10, container.view().count)
    }

    @Test
    fun `identical variants merge into one group`() {
        val container = Container()
        container.fill(stick(wear = 3, count = 4), stick(wear = 3, count = 6))

        assertEquals(1, container.groups.size)
        assertEquals(10, container.groups.single().count)
    }

    @Test
    fun `different variants form separate groups and count sums them`() {
        val container = Container()
        container.fill(stick(wear = 1, count = 4), stick(wear = 2, count = 6))

        assertEquals(2, container.groups.size)
        assertEquals(10, container.view().count)
    }

    @Test
    fun `capacity is one full stack of the base item`() {
        val container = Container()
        container.fill(stick(wear = 1, count = 64))
        val extra = stick(wear = 2)

        assertEquals(0, container.view().insert(extra))
        assertEquals(1, extra.count)
        assertEquals(64, container.view().count)
    }

    @Test
    fun `an empty container has no active variant`() {
        assertNull(Container().view().active)
    }

    @Test
    fun `without a rule the most recently inserted group is active`() {
        val container = Container()
        container.fill(stick(wear = 1), stick(wear = 2))

        assertEquals(2, wearOf(container.view().active!!))
    }

    @Test
    fun `with a rule the active group need not be first in contents order`() {
        val container = Container(mostWornFirst)
        container.fill(stick(wear = 5), stick(wear = 1))

        assertEquals(1, wearOf(container.groups.first()))
        assertEquals(5, wearOf(container.view().active!!))
    }

    @Test
    fun `rule ties keep contents order`() {
        val container = Container(compareBy { 0 })
        container.fill(stick(wear = 1), stick(wear = 2))

        assertEquals(2, wearOf(container.view().active!!))
    }

    @Test
    fun `split takes from the active group and leaves the others alone`() {
        val container = Container(mostWornFirst)
        container.fill(stick(wear = 5, count = 10), stick(wear = 1, count = 20))

        val taken = container.view().split(3)

        assertEquals(3, taken.count)
        assertEquals(5, wearOf(taken))
        val remaining = container.groups.associate { wearOf(it) to it.count }
        assertEquals(mapOf(5 to 7, 1 to 20), remaining)
    }

    @Test
    fun `splitting a whole group removes it`() {
        val container = Container(mostWornFirst)
        container.fill(stick(wear = 5, count = 4), stick(wear = 1, count = 20))

        val taken = container.view().split(4)

        assertEquals(4, taken.count)
        assertEquals(listOf(1), container.groups.map { wearOf(it) })
    }

    @Test
    fun `split is clamped to the active group`() {
        val container = Container(mostWornFirst)
        container.fill(stick(wear = 5, count = 4), stick(wear = 1, count = 20))

        val taken = container.view().split(100)

        assertEquals(4, taken.count)
        assertEquals(20, container.view().count)
    }

    @Test
    fun `splitting an empty container gives an empty stack`() {
        assertTrue(Container().view().split(1).isEmpty)
    }

    @Test
    fun `changes are written to the live stack`() {
        val container = Container()
        val writer = container.view()
        val reader = container.view()

        container.fill(stick(wear = 0, count = 8))
        val _ = writer.split(3)

        assertEquals(5, reader.count)
    }

    @Test
    fun `a stack that is not a container has no super stack view`() {
        assertNull(stick(wear = 0).asSuperStackOrNull())
        assertNull(ItemStack(Items.BUNDLE).asSuperStackOrNull())
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
            CommonFixtures.bindItemComponents()
        }
    }
}
