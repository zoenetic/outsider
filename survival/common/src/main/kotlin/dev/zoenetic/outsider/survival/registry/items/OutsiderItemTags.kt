package dev.zoenetic.outsider.survival.registry.items

import dev.zoenetic.outsider.survival.Survival
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

public object OutsiderItemTags {
    public val TINDER_MATERIALS: TagKey<Item> = TagKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "tinder_materials"),
    )
}
