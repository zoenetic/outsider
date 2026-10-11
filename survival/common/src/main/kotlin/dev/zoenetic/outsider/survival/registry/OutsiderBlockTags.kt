package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.Survival
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

public object OutsiderBlockTags {
    public val LIGHTABLE_WITH_DRILL: TagKey<Block> = TagKey.create(
        Registries.BLOCK,
        Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "lightable_with_drill"),
    )
}
