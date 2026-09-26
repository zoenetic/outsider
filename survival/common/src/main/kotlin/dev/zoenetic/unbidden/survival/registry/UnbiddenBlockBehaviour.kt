package dev.zoenetic.unbidden.survival.registry

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.PushReaction

public object UnbiddenBlockBehaviour {
    public object Properties {

        public fun campfire(): BlockBehaviour.Properties =
            BlockBehaviour.Properties
                .of()
                .strength(2F)
                .sound(SoundType.WOOD)
                .ignitedByLava()
                .noOcclusion()

        public fun deadCampfire(): BlockBehaviour.Properties =
            campfire().sound(SoundType.TUFF)

        public fun firewood(): BlockBehaviour.Properties = BlockBehaviour.Properties
            .of()
            .strength(2F)

        public fun torch(): BlockBehaviour.Properties = BlockBehaviour.Properties
            .of()
            .noCollision()
            .instabreak()
            .sound(SoundType.WOOD)
            .pushReaction(
                PushReaction.DESTROY
            )

        public fun wallTorch(): BlockBehaviour.Properties = torch()
            .overrideLootTable(UnbiddenBlocks.TORCH.lootTable)
            .overrideDescription(UnbiddenBlocks.TORCH.descriptionId)
    }
}
