@file:JvmName("RandomStuff")

package de.rubixdev.inventorio.util

import de.rubixdev.inventorio.config.PlayerSettings
import de.rubixdev.inventorio.mixin.accessor.ScreenHandlerAccessor
import de.rubixdev.inventorio.player.PlayerInventoryAddon
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.item.TridentItem
import net.minecraft.registry.RegistryKeys
import net.minecraft.screen.ScreenHandler
import net.minecraft.world.WorldView
import org.apache.logging.log4j.LogManager

data class Point2I(@JvmField val x: Int, @JvmField val y: Int)
data class Point2F(@JvmField val x: Float, @JvmField val y: Float)
data class Rectangle(@JvmField val x: Int, @JvmField val y: Int, @JvmField val width: Int, @JvmField val height: Int)

enum class SegmentedHotbar {
    OFF,
    ONLY_VISUAL,
    ON,
    ONLY_FUNCTION,
}

enum class ScrollWheelUtilityBeltMode {
    OFF,
    REGULAR,
    REVERSE,
}

enum class ToolBeltMode {
    ENABLED,
    NO_VANILLA_SLOTS_ONLY,
    DISABLED,
}

interface PlayerDuck {
    @Suppress("INAPPLICABLE_JVM_NAME") // see https://stackoverflow.com/questions/47504279/java-interop-apply-jvmname-to-getters-of-properties-in-interface-or-abstract-c
    @get:JvmName("inventorio${'$'}getInventorioAddon")
    val inventorioAddon: PlayerInventoryAddon?
}

val logger = LogManager.getLogger("Inventorio")!!

val ItemStack.isNotEmpty
    get() = !this.isEmpty

fun canRMBItem(itemStack: ItemStack, world: WorldView): Boolean {
    val enchantmentRegistry = world.registryManager.get(RegistryKeys.ENCHANTMENT)
    val loyaltyEntry = enchantmentRegistry.entryOf(Enchantments.LOYALTY)
    val riptideEntry = enchantmentRegistry.entryOf(Enchantments.RIPTIDE)
    return PlayerSettings.canThrowUnloyalTrident.boolValue
        || itemStack.item !is TridentItem
        || EnchantmentHelper.getLevel(loyaltyEntry, itemStack) > 0
        || EnchantmentHelper.getLevel(riptideEntry, itemStack) > 0
}

fun ScreenHandler.insertItem(stack: ItemStack, indices: IntRange, fromLast: Boolean = false): Boolean =
    (this as ScreenHandlerAccessor).callInsertItem(stack, indices.first, indices.last + 1, fromLast)
