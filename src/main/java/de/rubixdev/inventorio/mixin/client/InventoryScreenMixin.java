package de.rubixdev.inventorio.mixin.client;

import de.rubixdev.inventorio.client.ui.InventorioScreen;
import de.rubixdev.inventorio.mixin.client.accessor.HandledScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.rubixdev.inventorio.util.UIConstants.GUI_TOGGLE_BUTTON_OFFSET;

@Mixin(InventoryScreen.class)
@Environment(EnvType.CLIENT)
public class InventoryScreenMixin {

    @Unique private TexturedButtonWidget toggleButton;

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void inventorioAddToggle(CallbackInfo ci) {
        toggleButton = InventorioScreen.addToggleButton((InventoryScreen) (Object) this);
    }

    @Inject(method = "method_19891", at = @At("TAIL"))
    private void setToggleButtonPosition(ButtonWidget button, CallbackInfo ci) {
        toggleButton.setX(
            ((HandledScreenAccessor<?>) this).getX()
                + ((HandledScreenAccessor<?>) this).getBackgroundWidth()
                + GUI_TOGGLE_BUTTON_OFFSET.x
        );
        toggleButton.setY(((HandledScreenAccessor<?>) this).getY() + GUI_TOGGLE_BUTTON_OFFSET.y);
    }
}
