package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.superstack.SuperStackHooks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {

    @ModifyVariable(method = "itemCount", at = @At("HEAD"), argsOnly = true, name = "countText")
    private @Nullable String outsider_survival$superStackCount(@Nullable String countText, Font font, ItemStack itemStack) {
        return SuperStackHooks.countText(itemStack, countText);
    }

}
