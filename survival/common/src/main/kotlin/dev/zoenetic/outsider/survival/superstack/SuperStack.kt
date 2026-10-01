package dev.zoenetic.outsider.survival.superstack

import net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.component.BundleContents

public class SuperStack internal constructor(
    private val stack: ItemStack,
    private val type: SuperStackType,
) {
    public val active: ItemStackTemplate?
        get() {
            val groups = contents.items()
            return activeIndex(groups)?.let { groups[it] }
        }

    private val contents: BundleContents
        get() = stack.getOrDefault(
            BUNDLE_CONTENTS,
            BundleContents.EMPTY
        )

    public val count: Int get() = contents.items().sumOf { it.count }

    public fun insert(itemsToAdd: ItemStack): Int {
        if (itemsToAdd.isEmpty || !type.isValid(itemsToAdd)) return 0
        val new = BundleContents.Mutable(contents)
        val inserts = new.tryInsert(itemsToAdd)
        if (inserts > 0) set(new.toImmutable())
        return inserts
    }

    private fun activeIndex(groups: List<ItemStackTemplate>): Int? {
        if (groups.isEmpty()) return null
        val rule = type.rule ?: return 0
        return groups.indices.minWith(compareBy(rule) { groups[it] })
    }

    private fun set(contents: BundleContents) {
        stack.set(BUNDLE_CONTENTS, contents)
    }

    public fun split(amount: Int): ItemStack {
        val groups = contents.items().toMutableList()
        val index = activeIndex(groups) ?: return ItemStack.EMPTY
        val active = groups[index]
        val amount = amount.coerceIn(1, active.count)
        val remainingCount = active.count - amount
        if (remainingCount <= 0) {
            groups.removeAt(index)
        } else {
            val remaining = active.withCount(remainingCount)
            groups[index] = remaining
        }
        set(BundleContents(groups))
        return active.withCount(amount).create()
    }
}

public fun ItemStack.asSuperStackOrNull(): SuperStack? {
    val type = (item as? SuperStackItem)?.type ?: return null
    return SuperStack(this, type)
}