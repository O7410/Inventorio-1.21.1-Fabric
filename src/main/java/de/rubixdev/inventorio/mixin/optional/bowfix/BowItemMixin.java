package de.rubixdev.inventorio.mixin.optional.bowfix;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.rubixdev.inventorio.util.BowTester;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(require = @Condition(type = Condition.Type.TESTER, tester = BowTester.class))
@Mixin(BowItem.class)
public class BowItemMixin {
    /**
     * This fixes a bug (yes, it's a bug! check out
     * {@link BowItem#onStoppedUsing} method!) that an Infinity Bow requires an
     * arrow to shoot.
     */
    @ModifyExpressionValue(
        method = "use",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isInCreativeMode()Z")
    )
    private boolean inventorioFixInfinityBow(
        boolean original,
        World world,
        PlayerEntity user,
        Hand hand,
        @Local ItemStack bow
    ) {
        RegistryEntry.Reference<Enchantment> enchantmentReference =
            world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(Enchantments.INFINITY);
        return original || EnchantmentHelper.getLevel(enchantmentReference, bow) > 0;
    }
}
