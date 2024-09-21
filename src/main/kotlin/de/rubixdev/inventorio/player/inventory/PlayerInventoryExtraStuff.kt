package de.rubixdev.inventorio.player.inventory



//#if MC >= 12002
import com.google.common.collect.MultimapBuilder
import de.rubixdev.inventorio.mixin.client.accessor.MinecraftClientAccessor
import de.rubixdev.inventorio.packet.InventorioNetworking
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.StainedGlassBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FireworkRocketEntity
import net.minecraft.item.FireworkRocketItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.ToolItem
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import kotlin.math.max
//#else
//$$ import net.minecraft.block.AbstractGlassBlock
//#endif

abstract class PlayerInventoryExtraStuff protected constructor(player: PlayerEntity) : PlayerInventoryHandFeatures(player) {
    /**
     * Returns the block breaking speed based on the return of [getMostPreferredTool]
     */
    fun getMiningSpeedMultiplier(block: BlockState): Float {
        val tool = getMostPreferredTool(block)
        if (tool != getActualMainHandItem()
            // TODO: find a better way around this that's not exclusive to Jade
            && !Thread.currentThread().stackTrace.any { it.className.startsWith("snownee.jade.addon") }
        ) {
            displayTool = tool
        }
        return max(1f, tool.getMiningSpeedMultiplier(block))
    }

    private fun getMostPreferredTool(block: BlockState): ItemStack {
        // TODO: some mods will disagree with justifying the most preferred tool by just speed
        // If a player tries to dig with a selected tool, respect that tool and don't change anything
        if (getActualMainHandItem().item is ToolItem || findFittingToolBeltIndex(getActualMainHandItem()) != -1) {
            return getActualMainHandItem()
        }
        // Try to find the fastest tool on the tool belt to mine this block
        val result = toolBelt.maxByOrNull { it.getMiningSpeedMultiplier(block) } ?: ItemStack.EMPTY
        if (result.getMiningSpeedMultiplier(block) > 1.0f) {
            return result
        }
        //#if MC >= 12002
        val isGlass = block.block is StainedGlassBlock || block.isOf(Blocks.GLASS)
        //#else
        //$$ val isGlass = block.block is AbstractGlassBlock
        //#endif
        if (isGlass) {
            val silkTouchEntry = player.world.registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(Enchantments.SILK_TOUCH)
            return toolBelt.firstOrNull { EnchantmentHelper.getLevel(silkTouchEntry, it) > 0 } ?: ItemStack.EMPTY
        }
        return ItemStack.EMPTY
    }

    private fun playerAttackConditions(): Boolean {
        // if a player tries to attack with a tool, respect that tool and don't change anything
        if (getActualMainHandItem().item is ToolItem || findFittingToolBeltIndex(getActualMainHandItem()) != -1) {
            return false
        }
        // if the attack is done through a Pickarang (from Quark), do nothing
        if (Thread.currentThread().stackTrace.any { it.className.startsWith("org.violetmoon.quark.content.tools.entity.rang") }) {
            return false
        }
        return true
    }

    fun prePlayerAttack() {
        if (!playerAttackConditions()) return
        // Or else set a sword/trident as a weapon of choice, or an axe if a sword slot is empty
        player.handSwinging = true
        val swordStack = findFittingToolBeltStack(ItemStack(Items.DIAMOND_SWORD))
        displayTool = if (!swordStack.isEmpty) {
            swordStack
        } else {
            findFittingToolBeltStack(ItemStack(Items.DIAMOND_AXE))
        }
        // For some reason we need to manually add weapon's attack modifiers - the game doesn't do that for us
        val attributeModifiers = MultimapBuilder.hashKeys().hashSetValues().build<RegistryEntry<EntityAttribute>, EntityAttributeModifier>()
        displayTool.applyAttributeModifiers(EquipmentSlot.MAINHAND) { entry, modifier -> attributeModifiers.put(entry, modifier) }
        player.attributes.addTemporaryModifiers(attributeModifiers)
    }

    fun postPlayerAttack() {
        if (!playerAttackConditions()) return
        val attributeModifiers = MultimapBuilder.hashKeys().hashSetValues().build<RegistryEntry<EntityAttribute>, EntityAttributeModifier>()
        displayTool.applyAttributeModifiers(EquipmentSlot.MAINHAND) { entry, modifier -> attributeModifiers.put(entry, modifier) }
        player.attributes.removeModifiers(attributeModifiers)
    }

    fun fireRocketFromInventory() {
        if (!player.isFallFlying) {
            return
        }
        for (itemStack in player.inventory.main)
            if (tryFireRocket(itemStack)) {
                return
            }
        for (itemStack in stacks)
            if (tryFireRocket(itemStack)) {
                return
            }
    }

    private fun tryFireRocket(itemStack: ItemStack): Boolean {
        if (itemStack.item is FireworkRocketItem && itemStack.get(DataComponentTypes.FIREWORKS)?.explosions?.isEmpty() != false) {
            val copyStack = itemStack.copy()
            if (!player.abilities.creativeMode) {
                itemStack.decrement(1)
            }
            // If this is the client side, request a server to actually fire a rocket.
            if (player.world.isClient) {
                displayTool = itemStack
                InventorioNetworking.INSTANCE.c2sUseBoostRocket()
                (MinecraftClient.getInstance() as MinecraftClientAccessor).itemUseCooldown = 4
            } else {
                // If this is a server, spawn a firework entity
                player.world.spawnEntity(FireworkRocketEntity(player.world, copyStack, player))
            }
            return true
        }
        return false
    }
}
