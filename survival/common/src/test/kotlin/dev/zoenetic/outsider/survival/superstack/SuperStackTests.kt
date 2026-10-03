package dev.zoenetic.outsider.survival.superstack

import dev.zoenetic.outsider.survival.CommonFixtures
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemInstance
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SuperStackTests {

    private val mostWornFirst: Comparator<ItemInstance> =
        compareByDescending { it.getOrDefault(DataComponents.REPAIR_COST, 0) }

    private fun stick(wear: Int, count: Int = 1, name: String? = null): ItemStack =
        ItemStack(Items.STICK, count).apply {
            set(DataComponents.REPAIR_COST, wear)
            if (name != null) set(DataComponents.CUSTOM_NAME, Component.literal(name))
        }

    private fun wearOf(item: ItemInstance): Int = item.getOrDefault(DataComponents.REPAIR_COST, 0)

    private class Rules(
        override val sort: Comparator<ItemInstance>? = null,
        private val enter: (ItemStackTemplate) -> DataComponentPatch = { DataComponentPatch.EMPTY },
    ) : SuperStackRules {
        override fun onEnter(entering: ItemStackTemplate) = enter(entering)
    }

    private class Container(
        rules: SuperStackRules = SuperStackRules.NONE,
        mirrored: List<DataComponentType<*>> = emptyList(),
    ) {
        val stack = ItemStack(Items.BUNDLE)
        val type = SuperStackType(Items.STICK, rules, mirroredComponents = mirrored)
        fun view() = SuperStack(stack, type)
        fun fill(vararg stacks: ItemStack) = stacks.forEach { val _ = view().insert(it) }
        val groups: List<ItemStackTemplate>
            get() = stack.get(DataComponents.BUNDLE_CONTENTS)!!.items()
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
        val container = Container(Rules(sort = mostWornFirst))
        container.fill(stick(wear = 5), stick(wear = 1))

        assertEquals(1, wearOf(container.groups.first()))
        assertEquals(5, wearOf(container.view().active!!))
    }

    @Test
    fun `rule ties keep contents order`() {
        val container = Container(Rules(sort = compareBy { 0 }))
        container.fill(stick(wear = 1), stick(wear = 2))

        assertEquals(2, wearOf(container.view().active!!))
    }

    @Test
    fun `split takes from the active group and leaves the others alone`() {
        val container = Container(Rules(sort = mostWornFirst))
        container.fill(stick(wear = 5, count = 10), stick(wear = 1, count = 20))

        val taken = container.view().split(3)

        assertEquals(3, taken.count)
        assertEquals(5, wearOf(taken))
        val remaining = container.groups.associate { wearOf(it) to it.count }
        assertEquals(mapOf(5 to 7, 1 to 20), remaining)
    }

    @Test
    fun `splitting a whole group removes it`() {
        val container = Container(Rules(sort = mostWornFirst))
        container.fill(stick(wear = 5, count = 4), stick(wear = 1, count = 20))

        val taken = container.view().split(4)

        assertEquals(4, taken.count)
        assertEquals(listOf(1), container.groups.map { wearOf(it) })
    }

    @Test
    fun `splitting more than the active group holds takes nothing`() {
        val container = Container(Rules(sort = mostWornFirst))
        container.fill(stick(wear = 5, count = 4), stick(wear = 1, count = 20))

        val taken = container.view().split(5)

        assertTrue(taken.isEmpty)
        assertEquals(24, container.view().count)
        assertEquals(4, container.view().active!!.count)
    }

    @Test
    fun `splitting less than one takes nothing`() {
        val container = Container()
        container.fill(stick(wear = 0, count = 4))

        assertTrue(container.view().split(0).isEmpty)
        assertTrue(container.view().split(-1).isEmpty)
        assertEquals(4, container.view().count)
    }

    @Test
    fun `drain returns every group as plain stacks and empties the container`() {
        val container = Container()
        container.fill(stick(wear = 1, count = 4), stick(wear = 2, count = 6))

        val drained = container.view().drain()

        assertEquals(mapOf(1 to 4, 2 to 6), drained.associate { wearOf(it) to it.count })
        assertTrue(drained.all { it.item === Items.STICK })
        assertEquals(0, container.view().count)
        assertNull(container.view().active)
    }

    @Test
    fun `draining an empty container gives nothing`() {
        val container = Container()
        container.fill(stick(wear = 0, count = 3))
        val _ = container.view().drain()

        assertTrue(container.view().drain().isEmpty())
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

    private val wearAndName = listOf(DataComponents.REPAIR_COST, DataComponents.CUSTOM_NAME)

    private fun nameOf(stack: ItemStack): String? = stack.get(DataComponents.CUSTOM_NAME)?.string

    @Test
    fun `the container mirrors the active variant`() {
        val container = Container(Rules(sort = mostWornFirst), wearAndName)
        container.fill(stick(wear = 5, name = "worn"), stick(wear = 1))

        assertEquals(5, container.stack.get(DataComponents.REPAIR_COST))
        assertEquals("worn", nameOf(container.stack))
    }

    @Test
    fun `the mirror follows the active variant when it changes`() {
        val container = Container(Rules(sort = mostWornFirst), wearAndName)
        container.fill(stick(wear = 1))
        assertEquals(1, container.stack.get(DataComponents.REPAIR_COST))

        container.fill(stick(wear = 5))
        assertEquals(5, container.stack.get(DataComponents.REPAIR_COST))

        val _ = container.view().split(1)
        assertEquals(1, container.stack.get(DataComponents.REPAIR_COST))
    }

    @Test
    fun `a component the active variant lacks is removed from the container`() {
        val container = Container(Rules(sort = mostWornFirst), wearAndName)
        container.fill(stick(wear = 5, name = "worn"), stick(wear = 1))

        val _ = container.view().split(1)

        assertNull(container.stack.get(DataComponents.CUSTOM_NAME))
    }

    @Test
    fun `draining clears the mirror`() {
        val container = Container(Rules(sort = mostWornFirst), wearAndName)
        container.fill(stick(wear = 5, name = "worn"))

        val _ = container.view().drain()

        assertNull(container.stack.get(DataComponents.CUSTOM_NAME))
        assertEquals(
            ItemStack(Items.BUNDLE).get(DataComponents.REPAIR_COST),
            container.stack.get(DataComponents.REPAIR_COST),
        )
    }

    @Test
    fun `unlisted components on the container are left alone`() {
        val container = Container(Rules(sort = mostWornFirst), listOf(DataComponents.REPAIR_COST))
        container.stack.set(DataComponents.CUSTOM_NAME, Component.literal("pouch"))

        container.fill(stick(wear = 5, name = "worn"))
        assertEquals("pouch", nameOf(container.stack))

        val _ = container.view().drain()
        assertEquals("pouch", nameOf(container.stack))
    }

    @Test
    fun `a default value from the base item is mirrored`() {
        val container = Container(mirrored = listOf(DataComponents.ITEM_NAME))
        container.fill(ItemStack(Items.STICK))

        assertEquals(
            ItemStack(Items.STICK).get(DataComponents.ITEM_NAME),
            container.stack.get(DataComponents.ITEM_NAME),
        )
    }

    private val stripName =
        Rules(enter = { DataComponentPatch.builder().remove(DataComponents.CUSTOM_NAME).build() })

    @Test
    fun `items entering go through onEnter`() {
        val container = Container(stripName)
        container.fill(stick(wear = 1, name = "lit"))

        assertNull(container.groups.single().get(DataComponents.CUSTOM_NAME))
    }

    @Test
    fun `leftovers that do not fit skip onEnter`() {
        val container = Container(stripName)
        container.fill(stick(wear = 1, count = 60))
        val source = stick(wear = 2, count = 10, name = "lit")

        assertEquals(4, container.view().insert(source))

        assertEquals(6, source.count)
        assertEquals("lit", nameOf(source))
        val entered = container.groups.single { wearOf(it) == 2 }
        assertNull(entered.get(DataComponents.CUSTOM_NAME))
    }

    @Test
    fun `items entering merge with matching groups after onEnter`() {
        val container = Container(stripName)
        container.fill(stick(wear = 1, count = 3), stick(wear = 1, count = 2, name = "lit"))

        assertEquals(1, container.groups.size)
        assertEquals(5, container.groups.single().count)
    }

    private fun named(name: String): DataComponentPatch = DataComponentPatch.builder()
        .set(DataComponents.CUSTOM_NAME, Component.literal(name))
        .build()

    @Test
    fun `patching the active stack changes exactly one item into its own group`() {
        val container = Container(Rules(sort = mostWornFirst))
        container.fill(stick(wear = 5, count = 3), stick(wear = 1, count = 4))

        assertTrue(container.view().patchActiveStack(named("lit")))

        val byName = container.groups.associate { (nameOf(it.create()) to wearOf(it)) to it.count }
        val expected = mapOf(("lit" to 5) to 1, (null to 5) to 2, (null to 1) to 4)
        assertEquals(expected, byName)
    }

    @Test
    fun `patching the active stack skips onEnter`() {
        val container = Container(stripName)
        container.fill(stick(wear = 1, count = 2))

        val _ = container.view().patchActiveStack(named("lit"))

        assertTrue(container.groups.any { nameOf(it.create()) == "lit" })
    }

    @Test
    fun `the mirror follows a patched item that becomes active`() {
        val namedFirst = Rules(sort = compareBy { it.get(DataComponents.CUSTOM_NAME) == null })
        val container = Container(namedFirst, listOf(DataComponents.CUSTOM_NAME))
        container.fill(stick(wear = 1, count = 3))

        val _ = container.view().patchActiveStack(named("lit"))

        assertEquals("lit", nameOf(container.stack))
    }

    @Test
    fun `patching an empty container does nothing`() {
        val container = Container()

        assertFalse(container.view().patchActiveStack(named("lit")))

        assertEquals(0, container.view().count)
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
