package de.rubixdev.inventorio.player.inventory

import de.rubixdev.inventorio.config.GlobalSettings
import de.rubixdev.inventorio.enchantment.InventorioEnchantments
import de.rubixdev.inventorio.packet.InventorioNetworking
import de.rubixdev.inventorio.player.PlayerInventoryAddon
import de.rubixdev.inventorio.util.*
import kotlin.math.sign
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.collection.DefaultedList

abstract class PlayerInventoryExtension protected constructor(val player: PlayerEntity) :
    SimpleInventory(DEEP_POCKETS_MAX_SIZE + UTILITY_BELT_FULL_SIZE + PlayerInventoryAddon.toolBeltTemplates.size) {
    /** Warning! The length of [toolBelt], and thus [stacks], may differ across play sessions depending on the mods installed
     * We DO need this variable because on NeoForge the parent `stacks` is private. */
    @JvmField
    val stacks: DefaultedList<ItemStack> = this.heldStacks
    @JvmField val deepPockets = stacks.subList(INVENTORY_ADDON_DEEP_POCKETS_RANGE.first, INVENTORY_ADDON_DEEP_POCKETS_RANGE.last + 1)
    @JvmField val utilityBelt = stacks.subList(INVENTORY_ADDON_UTILITY_BELT_RANGE.first, INVENTORY_ADDON_UTILITY_BELT_RANGE.last + 1)

    /** Warning! The length of [toolBelt], and thus [stacks], may differ across play sessions depending on the mods installed */
    @JvmField val toolBelt = stacks.subList(INVENTORY_ADDON_TOOL_BELT_INDEX_OFFSET, INVENTORY_ADDON_TOOL_BELT_INDEX_OFFSET + PlayerInventoryAddon.toolBeltTemplates.size)

    private val lastTrackedStacksState = DefaultedList.ofSize(stacks.size, ItemStack.EMPTY)

    var selectedUtility = 0
        set(value) {
            field = value.coerceIn(0, 7)
        }

    // =========================
    // Boring Inventory Stuff
    // =========================
    protected fun sendUpdateS2C() {
        val stacksToUpdate = mutableMapOf<Int, ItemStack>()
        for ((index, stack) in stacks.withIndex()) {
            if (!ItemStack.areEqual(stack, lastTrackedStacksState[index])) {
                lastTrackedStacksState[index] = stack.copy()
                stacksToUpdate[index] = stack.copy()
            }
        }
        if (stacksToUpdate.isNotEmpty()) {
            InventorioNetworking.INSTANCE.s2cUpdateAddonStacks(player as ServerPlayerEntity, stacksToUpdate)
        }
    }

    fun updateState() {
        val stacksToUpdate = stacks.withIndex().associate { (index, stack) -> index to stack }
        InventorioNetworking.INSTANCE.s2cUpdateAddonStacks(player as ServerPlayerEntity, stacksToUpdate)
    }

    @Environment(EnvType.CLIENT)
    fun receiveStacksUpdateS2C(updatedStacks: Map<Int, ItemStack>) {
        for ((index, stack) in updatedStacks) {
            stacks[index] = stack
        }
    }

    fun cloneFrom(oldAddon: PlayerInventoryAddon) {
        for ((index, stack) in oldAddon.stacks.withIndex()) {
            this.setStack(index, stack)
        }
        this.selectedUtility = oldAddon.selectedUtility
    }

    fun dropAll() {
        for ((index, itemStack) in stacks.withIndex()) {
            val vanishingCurseEntry = player.world.registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(Enchantments.VANISHING_CURSE)
            if (EnchantmentHelper.getLevel(vanishingCurseEntry, itemStack) == 0) {
                player.dropItem(itemStack, true, false)
            }
            stacks[index] = ItemStack.EMPTY
        }
    }

    fun getTotalAmount(sampleStack: ItemStack): Int {
        var count = 0
        for (i in 0 until player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (areItemsSimilar(stack, sampleStack)) {
                count += stack.count
            }
        }
        return count + stacks.filter { areItemsSimilar(it, sampleStack) }.sumOf { it.count }
    }

    // =========================
    // Addon Special Stuff
    // =========================
    fun switchToNextUtility(direction: Int, skipEmptySlots: Boolean): Boolean {
        val slotIndex = findNextUtility(direction, skipEmptySlots).second
        if (slotIndex == -1) {
            return false
        }
        selectedUtility = slotIndex
        InventorioNetworking.INSTANCE.c2sSelectUtilitySlot(slotIndex)
        return true
    }

    /**
     * Returns (ItemStack.EMPTY; -1) if no next utility was found
     */
    fun findNextUtility(direction: Int, skipEmptySlots: Boolean): Pair<ItemStack, Int> {
        val range = if (direction.sign >= 0) {
            (selectedUtility + 1 until getAvailableUtilityBeltSize()) + (0 until selectedUtility)
        } else {
            (selectedUtility - 1 downTo 0) + (getAvailableUtilityBeltSize() - 1 downTo selectedUtility + 1)
        }

        for (i in range) {
            if (!skipEmptySlots || utilityBelt[i].isNotEmpty) {
                return Pair(utilityBelt[i], i)
            }
        }

        return Pair(ItemStack.EMPTY, -1)
    }

    fun switchToEmptyUtility(direction: Int): Boolean {
        val slotIndex = findEmptyUtility(direction)
        if (slotIndex == -1) {
            return false
        }
        selectedUtility = slotIndex
        InventorioNetworking.INSTANCE.c2sSelectUtilitySlot(slotIndex)
        return true
    }

    /**
     * Returns -1 if no empty utility slot was found
     */
    private fun findEmptyUtility(direction: Int): Int {
        val range = if (direction.sign >= 0) {
            (selectedUtility + 1 until getAvailableUtilityBeltSize()) + (0 until selectedUtility)
        } else {
            (selectedUtility - 1 downTo 0) + (getAvailableUtilityBeltSize() - 1 downTo selectedUtility + 1)
        }

        for (i in range)
            if (utilityBelt[i].isEmpty) {
                return i
            }

        return -1
    }

    /**
     * Note: this class returns the range within the SCREEN HANDLER, which is different from the range within the inventory
     */
    fun getAvailableUtilityBeltSize(): Int {
        return if (getDeepPocketsRowCount() > 0 || !GlobalSettings.utilityBeltShortDefaultSize.boolValue) {
            UTILITY_BELT_FULL_SIZE
        } else {
            UTILITY_BELT_SMALL_SIZE
        }
    }

    /**
     * Note: this class returns the range within the INVENTORY, which is different from the range within the Screen Handler
     */
    fun getAvailableDeepPocketsRange(): IntRange {
        return INVENTORY_ADDON_DEEP_POCKETS_RANGE.first expandBy getDeepPocketsRowCount() * VANILLA_ROW_LENGTH
    }

    /**
     * Note: this class returns the range within the INVENTORY, which is different from the range within the Screen Handler
     */
    fun getUnavailableDeepPocketsRange(): IntRange {
        return getAvailableDeepPocketsRange().last + 1..INVENTORY_ADDON_DEEP_POCKETS_RANGE.last
    }

    fun getDeepPocketsRowCount(): Int {
        val enchantmentReference = player.registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(InventorioEnchantments.DEEP_POCKETS)
        return EnchantmentHelper.getEquipmentLevel(enchantmentReference, player).coerceIn(0, 3)
    }

    protected fun areItemsSimilar(stack1: ItemStack, stack2: ItemStack): Boolean {
        return stack1.isNotEmpty && ItemStack.areItemsAndComponentsEqual(stack1, stack2)
    }

    fun contains(stack: ItemStack): Boolean = stacks.any { areItemsSimilar(it, stack) }

    fun contains(tag: TagKey<Item>): Boolean = stacks.any { it.isNotEmpty && it.isIn(tag) }

    fun findFittingToolBeltStack(sampleStack: ItemStack): ItemStack {
        val index = findFittingToolBeltIndex(sampleStack)
        return if (index == -1) ItemStack.EMPTY else toolBelt[index]
    }

    fun findFittingToolBeltIndex(sampleStack: ItemStack): Int {
        for ((index, template) in PlayerInventoryAddon.toolBeltTemplates.withIndex()) {
            if (template.test(sampleStack, this as PlayerInventoryAddon)) {
                return index
            }
        }
        return -1
    }
}
