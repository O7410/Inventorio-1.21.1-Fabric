package de.rubixdev.inventorio.slot

import com.mojang.datafixers.util.Pair
import de.rubixdev.inventorio.player.InventorioScreenHandler
import net.minecraft.component.EnchantmentEffectComponentTypes
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier

class ArmorSlot(
    private val screenHandler: InventorioScreenHandler,
    inventory: Inventory,
    private val entity: LivingEntity,
    private val equipmentSlot: EquipmentSlot,
    index: Int,
    x: Int,
    y: Int,
) : Slot(inventory, index, x, y) {

    override fun setStack(stack: ItemStack, previousStack: ItemStack) {
        this.entity.onEquipStack(this.equipmentSlot, previousStack, stack)
        super.setStack(stack, previousStack)
        this.screenHandler.updateDeepPocketsCapacity()
    }

    override fun getMaxItemCount() = 1

    override fun canInsert(stack: ItemStack): Boolean {
        return this.equipmentSlot == this.entity.getPreferredEquipmentSlot(stack)
    }

    override fun canTakeItems(playerEntity: PlayerEntity): Boolean {
        val itemStack = this.stack
        return if (!itemStack.isEmpty
            && !playerEntity.isCreative
            && EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)
        ) {
            false
        } else {
            super.canTakeItems(playerEntity)
        }
    }

    override fun getBackgroundSprite(): Pair<Identifier, Identifier> =
        Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.entitySlotId])

    companion object {
        private val EMPTY_ARMOR_SLOT_TEXTURES = arrayOf(
            PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE,
        )
    }
}
