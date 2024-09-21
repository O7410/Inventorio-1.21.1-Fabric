package de.rubixdev.inventorio.mixin.optional.enderchest;

import de.rubixdev.inventorio.util.EnderChestTester;
import de.rubixdev.inventorio.util.GeneralConstants;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.inventory.EnderChestInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Restriction(require = @Condition(type = Condition.Type.TESTER, tester = EnderChestTester.class))
@Mixin(EnderChestInventory.class)
public abstract class EnderChestInventoryMixin {

    /**
     * This inject enlarges the Ender Chest's capacity to 6 rows.
     */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 27))
    private static int inventorioResizeEnderChest(int constant) {
        return GeneralConstants.VANILLA_ROW_LENGTH * 6;
    }
}
