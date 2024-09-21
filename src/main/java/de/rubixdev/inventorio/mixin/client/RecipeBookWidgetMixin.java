package de.rubixdev.inventorio.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeBookWidget.class)
public class RecipeBookWidgetMixin {
    @Shadow
    private int leftOffset;

    @ModifyReturnValue(method = "isWide", at = @At("RETURN"))
    private boolean correctIsWide(boolean original) {
        return original || leftOffset > 0;
    }

}
