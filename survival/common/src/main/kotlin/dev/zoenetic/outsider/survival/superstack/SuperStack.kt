package dev.zoenetic.outsider.survival.superstack

import net.minecraft.core.component.DataComponentPatch
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
            BundleContents.EMPTY,
        )

    public val count: Int get() = contents.items().sumOf { it.count }

    public fun insert(itemsToAdd: ItemStack): Int = insertWithPatch(itemsToAdd, type.rules::onEnter)

    private fun reinsert(itemsToAdd: ItemStack): Int =
        insertWithPatch(itemsToAdd) { DataComponentPatch.EMPTY }

    private fun insertWithPatch(
        itemsToAdd: ItemStack,
        patchFunction: (ItemStackTemplate) -> DataComponentPatch,
    ): Int {
        if (itemsToAdd.isEmpty || !type.isValid(itemsToAdd)) return 0
        val template = ItemStackTemplate.fromNonEmptyStack(itemsToAdd)
        val newContents = BundleContents.Mutable(contents)
        val entering = template.create()
        entering.applyComponents(patchFunction(template))
        val inserts = newContents.tryInsert(entering)
        if (inserts > 0) {
            itemsToAdd.shrink(inserts)
            set(newContents.toImmutable())
        }
        return inserts
    }

    private fun activeIndex(groups: List<ItemStackTemplate>): Int? {
        val sort = type.rules.sort
        return when {
            groups.isEmpty() -> null
            sort == null -> 0
            else -> groups.indices.minWith(compareBy(sort) { groups[it] })
        }
    }

    public fun patchActiveStack(patch: DataComponentPatch): Boolean {
        val original = contents
        val active = split(1)
        if (active.isEmpty) return false
        active.applyComponents(patch)
        val inserts = reinsert(active)
        if (inserts == 0) {
            // uh oh, put it back
            set(original)
            return false
        }
        return true
    }

    private fun set(contents: BundleContents) {
        stack.set(BUNDLE_CONTENTS, contents)
        updateMirroredComponents(contents)
    }

    private fun updateMirroredComponents(contents: BundleContents) {
        val index = activeIndex(contents.items())
        val source = if (index != null) contents.items()[index] else stack.item.components()
        type.mirroredComponents.forEach { componentType ->
            stack.copyFrom(componentType, source)
        }
    }

    private fun List<ItemStackTemplate>.reducedAt(
        index: Int,
        amount: Int,
    ): List<ItemStackTemplate> = mapIndexedNotNull { i, group ->
        when {
            i != index -> group
            group.count > amount -> group.withCount(group.count - amount)
            else -> null
        }
    }

    public fun split(amount: Int): ItemStack {
        val groups = contents.items()
        val index = activeIndex(groups) ?: return ItemStack.EMPTY
        val active = groups[index]
        if (amount < 1 || amount > active.count) return ItemStack.EMPTY
        set(BundleContents(groups.reducedAt(index, amount)))
        return active.withCount(amount).create()
    }

    public fun drain(): List<ItemStack> {
        val stacks = contents.items().map { item ->
            item.create()
        }
        set(BundleContents.EMPTY)
        return stacks
    }
}

public fun ItemStack.asSuperStackOrNull(): SuperStack? {
    val type = (item as? SuperStackItem)?.type ?: return null
    return SuperStack(this, type)
}

public fun ItemStack.moveIntoSuperStack(): ItemStack? {
    if (isEmpty) return null
    val superStackItem = SuperStacks.byBaseItem[item] ?: return null
    val container = ItemStack(superStackItem, 1)
    val superStack = checkNotNull(container.asSuperStackOrNull()) {
        "Registered SuperStackItem did not produce a SuperStack view"
    }
    return container.takeIf { superStack.insert(this) > 0 }
}
