package me.lizardofoz.inventorio.util

import net.minecraft.item.ItemStack

const val DEEP_POCKETS_MAX_LEVEL = 3
const val VANILLA_ROW_LENGTH = 9
const val VANILLA_OFFHAND_SLOT_INDEX = 45

const val INVENTORY_SLOT_SIZE = 18

const val MAIN_INVENTORY_SIZE = VANILLA_ROW_LENGTH * 4
const val EXTENSION_SIZE = DEEP_POCKETS_MAX_LEVEL * VANILLA_ROW_LENGTH
const val ARMOR_SIZE = 4
const val TOOL_BELT_SIZE = 5
const val UTILITY_BELT_SIZE = 8


val CRAFTING_GRID_RANGE = 0 until 5
val ARMOR_RANGE = CRAFTING_GRID_RANGE.last + 1 until (CRAFTING_GRID_RANGE.last + 1) + ARMOR_SIZE
val MAIN_INVENTORY_RANGE = ARMOR_RANGE.last + 1 until (ARMOR_RANGE.last + 1) + MAIN_INVENTORY_SIZE
val DUD_OFFHAND_RANGE = MAIN_INVENTORY_RANGE.last + 1 .. MAIN_INVENTORY_RANGE.last + 1
val EXTENSION_RANGE = DUD_OFFHAND_RANGE.last + 1 until (DUD_OFFHAND_RANGE.last + 1) + EXTENSION_SIZE
val TOOL_BELT_RANGE = EXTENSION_RANGE.last + 1 until (EXTENSION_RANGE.last + 1) + TOOL_BELT_SIZE
val UTILITY_BELT_RANGE = TOOL_BELT_RANGE.last + 1 until (TOOL_BELT_RANGE.last + 1) + UTILITY_BELT_SIZE
val UTILITY_BELT_EXTENSION_RANGE = UTILITY_BELT_RANGE.first + 4 .. UTILITY_BELT_RANGE.last

val MAIN_INVENTORY_NO_HOTBAR_RANGE = MAIN_INVENTORY_RANGE.first .. MAIN_INVENTORY_RANGE.last - 9
val HOTBAR_INVENTORY_RANGE = MAIN_INVENTORY_NO_HOTBAR_RANGE.last + 1 .. MAIN_INVENTORY_RANGE.last

val HANDLER_TO_INVENTORY_OFFSET = 5

val ItemStack.isNotEmpty: Boolean
    get() = !this.isEmpty