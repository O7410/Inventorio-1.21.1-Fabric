package de.rubixdev.inventorio.player

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.DynamicRegistryManager

object PlayerAddonSerializer {
    fun serialize(
        inventoryAddon: PlayerInventoryAddon,
        inventorioTag: NbtCompound,
        registryManager: DynamicRegistryManager,
    ) {
        inventorioTag.putInt("SelectedUtilitySlot", inventoryAddon.selectedUtility)
        inventorioTag.put("DeepPockets", serializeSection(inventoryAddon.deepPockets, registryManager))
        inventorioTag.put("UtilityBelt", serializeSection(inventoryAddon.utilityBelt, registryManager))
        inventorioTag.put("ToolBelt", serializeSection(inventoryAddon.toolBelt, registryManager))
    }

    private fun serializeSection(section: List<ItemStack>, registryManager: DynamicRegistryManager): NbtList {
        val resultTag = NbtList()
        for ((slotIndex, itemStack) in section.withIndex()) {
            if (itemStack.isEmpty) {
                continue
            }
            val itemTag = NbtCompound()
            itemTag.putByte("Slot", slotIndex.toByte())
            resultTag.add(itemStack.encode(registryManager, itemTag))
        }
        return resultTag
    }

    fun deserialize(inventoryAddon: PlayerInventoryAddon, inventorioTag: NbtCompound, registryManager: DynamicRegistryManager) {
        inventoryAddon.selectedUtility = inventorioTag.getInt("SelectedUtilitySlot")

        deserializeSection(inventoryAddon, inventoryAddon.utilityBelt, inventorioTag.getList("UtilityBelt", 10), registryManager)
        deserializeSection(inventoryAddon, inventoryAddon.toolBelt, inventorioTag.getList("ToolBelt", 10), registryManager)
        deserializeSection(inventoryAddon, inventoryAddon.deepPockets, inventorioTag.getList("DeepPockets", 10), registryManager)
    }

    private fun deserializeSection(inventoryAddon: PlayerInventoryAddon, inventorySection: MutableList<ItemStack>, sectionTag: NbtList, registryManager: DynamicRegistryManager) {
        for (i in inventorySection.indices)
            inventorySection[i] = ItemStack.EMPTY

        for (itemTag in sectionTag) {
            val compoundTag = itemTag as NbtCompound
            val itemStack = ItemStack.fromNbt(registryManager, compoundTag).orElse(ItemStack.EMPTY)
            val slotIndex = compoundTag.getInt("Slot")
            if (slotIndex in inventorySection.indices) {
                inventorySection[slotIndex] = itemStack
            } else {
                inventoryAddon.player.dropItem(itemStack, false)
            }
        }
    }
}
