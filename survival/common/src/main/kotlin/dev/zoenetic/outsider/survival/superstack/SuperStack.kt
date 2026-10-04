package dev.zoenetic.outsider.survival.superstack

import net.minecraft.core.component.DataComponentGetter
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.component.BundleContents

public class SuperStack internal constructor(
    private val stack: ItemStack,
    public val type: SuperStackType,
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

    internal fun refreshMirroredComponents() {
        val source = active ?: stack.item.components()
        stack.applyComponents(
            DataComponentPatch.builder().apply {
                type.mirroredComponents.forEach { copy(it, source) }
            }.build(),
        )
    }

    private fun set(contents: BundleContents) {
        stack.set(BUNDLE_CONTENTS, contents)
        refreshMirroredComponents()
    }

    private fun <T : Any> DataComponentPatch.Builder.copy(
        type: DataComponentType<T>,
        source: DataComponentGetter,
    ) {
        val value = source.get(type)
        if (value != null) set(type, value) else remove(type)
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

public fun ItemStack.isOrContains(item: Item): Boolean {
    if (`is`(item)) return true
    val superStack = asSuperStackOrNull() ?: return false
    return superStack.type.item == item && superStack.count > 0
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
