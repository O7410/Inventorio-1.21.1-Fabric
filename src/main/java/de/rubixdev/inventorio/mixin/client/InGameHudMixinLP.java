package de.rubixdev.inventorio.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.rubixdev.inventorio.client.ui.HotbarHUDRenderer;
import de.rubixdev.inventorio.player.PlayerInventoryAddon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InGameHud.class, priority = 99)
@Environment(EnvType.CLIENT)
public class InGameHudMixinLP {
    /**
     * This mixin redirects rendering the hotbar itself in case Segmented Hotbar
     * is selected.
     */
    @Inject(method = "renderHotbar", at = @At(value = "HEAD"), cancellable = true, require = 0)
    private void inventorioRenderSegmentedHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HotbarHUDRenderer.INSTANCE.renderSegmentedHotbar(context)) ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At(value = "RETURN"), require = 0)
    private void inventorioRenderFunctionOnlySelector(
        DrawContext context,
        RenderTickCounter tickCounter,
        CallbackInfo ci
    ) {
        HotbarHUDRenderer.INSTANCE.renderFunctionOnlySelector(context);
    }

    /**
     * In vanilla, when you look at an entity with a sword, it shows an attack
     * indicator. This mixin restores this feature if you have a sword in your
     * tool belt.
     */
    @ModifyExpressionValue(
        method = "renderCrosshair",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgressPerTick()F"
        ),
        require = 0
    )
    private float inventorioShowAttackIndicator(float original) {
        PlayerInventoryAddon addon = PlayerInventoryAddon.Client.INSTANCE.getLocal();
        if (addon != null && !addon.findFittingToolBeltStack(new ItemStack(Items.DIAMOND_SWORD)).isEmpty())
            return 20.0f;
        return original;
    }
}
