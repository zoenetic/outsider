package dev.zoenetic.outsider.survival.registry.blocks

import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.PushReaction

public object OutsiderBlockBehaviour {
    public object Properties {

        public fun campfire(): BlockBehaviour.Properties = BlockBehaviour.Properties
            .of()
            .strength(2F)
            .sound(SoundType.WOOD)
            .ignitedByLava()
            .noOcclusion()

        public fun deadCampfire(): BlockBehaviour.Properties = campfire().sound(SoundType.TUFF)

        public fun firewood(): BlockBehaviour.Properties = BlockBehaviour.Properties
            .of()
            .strength(2F)

        public fun looseStone(): BlockBehaviour.Properties = BlockBehaviour.Properties
            .of()
            .noCollision()
            .instabreak()
            .sound(SoundType.STONE)
            .pushReaction(PushReaction.DESTROY)

        public fun torch(): BlockBehaviour.Properties = BlockBehaviour.Properties
            .of()
            .noCollision()
            .instabreak()
            .sound(SoundType.WOOD)
            .pushReaction(
                PushReaction.DESTROY,
            )

        public fun wallTorch(): BlockBehaviour.Properties = torch()
            .overrideDescription(OutsiderBlocks.TORCH.descriptionId)

        public fun deadTorch(): BlockBehaviour.Properties = torch()

        public fun deadWallTorch(): BlockBehaviour.Properties = torch()
            .overrideDescription(OutsiderBlocks.DEAD_TORCH.descriptionId)
            .overrideLootTable(OutsiderBlocks.DEAD_TORCH.lootTable)
    }
}
