package de.rubixdev.inventorio.player

import com.google.common.collect.ImmutableList
import de.rubixdev.inventorio.api.InventorioAddonSection
import de.rubixdev.inventorio.api.InventorioTickHandler
import de.rubixdev.inventorio.api.ToolBeltSlotTemplate
import de.rubixdev.inventorio.client.ui.InventorioScreen
import de.rubixdev.inventorio.config.GlobalSettings
import de.rubixdev.inventorio.mixin.client.accessor.MinecraftClientAccessor
import de.rubixdev.inventorio.player.inventory.PlayerInventoryExtraStuff
import de.rubixdev.inventorio.util.PlayerDuck
import de.rubixdev.inventorio.util.ToolBeltMode
import de.rubixdev.inventorio.util.isNotEmpty
import de.rubixdev.inventorio.util.logger
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.NetworkSyncedItem
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.Util

/**
 * This class is responsible for the inventory addon itself,
 * while [InventorioScreen] is responsible for the visuals of the Player Screen UI
 * and [InventorioScreenHandler] is responsible for the slots and player interacting with the slots
*/
class PlayerInventoryAddon internal constructor(player: PlayerEntity) : PlayerInventoryExtraStuff(player) {
    init {
        bNoMoreToolBeltSlots = true
    }

    private var prevSelectedSlot = -1

    fun tick() {
        val ms = Util.getMeasuringTimeMs()
        if (player.handSwinging) {
            displayToolTimeStamp = ms + 1000
        }
        if (displayToolTimeStamp <= ms || (prevSelectedSlot != -1 && prevSelectedSlot != player.inventory.selectedSlot)) {
            displayTool = ItemStack.EMPTY
        }
        prevSelectedSlot = player.inventory.selectedSlot

        stacks.forEach { syncItems(player, it) }
        stacks.filter { it.isNotEmpty }.forEach {
            it.inventoryTick(player.world, player, -2, false)
        }
        for (tickHandler in tickHandlers.entries) {
            for ((index, item) in deepPockets.withIndex())
                tickMe(tickHandler, this, InventorioAddonSection.DEEP_POCKETS, item, index)
            for ((index, item) in toolBelt.withIndex())
                tickMe(tickHandler, this, InventorioAddonSection.TOOLBELT, item, index)
            for ((index, item) in utilityBelt.withIndex())
                tickMe(tickHandler, this, InventorioAddonSection.UTILITY_BELT, item, index)
        }
        if (!player.world.isClient) {
            sendUpdateS2C()
        }
    }

    private fun tickMe(
        tickHandler: Map.Entry<Identifier, InventorioTickHandler>,
        playerInventoryAddon: PlayerInventoryAddon,
        deepPockets: InventorioAddonSection,
        item: ItemStack,
        index: Int,
    ) {
        try {
            tickHandler.value.tick(playerInventoryAddon, deepPockets, item, index)
        } catch (e: Throwable) {
            logger.error("Inventory Tick Handler '${tickHandler.key}' has failed: ", e)
        }
    }

    private fun syncItems(player: PlayerEntity, stack: ItemStack) {
        if (stack.item.isNetworkSynced && player is ServerPlayerEntity) {
            (stack.item as? NetworkSyncedItem)?.createSyncPacket(stack, player.world, player)?.let { packet ->
                player.networkHandler.sendPacket(packet)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    object Client {
        val local get() = MinecraftClient.getInstance().player?.inventoryAddon
        @JvmField var selectedHotbarSection = -1
        @JvmField var triesToUseUtility = false
        @JvmField var isUsingUtility = false

        fun activateSelectedUtility() {
            triesToUseUtility = true
            isUsingUtility = true
            (MinecraftClient.getInstance() as MinecraftClientAccessor).invokeDoItemUse()
            triesToUseUtility = false
        }
    }

    companion object {
        @JvmStatic
        val PlayerEntity.inventoryAddon: PlayerInventoryAddon?
            get() = (this as PlayerDuck).inventorioAddon

        private val tickHandlers = mutableMapOf<Identifier, InventorioTickHandler>()
        internal val toolBeltTemplates = mutableListOf<ToolBeltSlotTemplate>()
        private var bNoMoreToolBeltSlots = false

        @JvmStatic
        fun registerTickHandler(customIdentifier: Identifier, tickHandler: InventorioTickHandler) {
            if (tickHandlers.containsKey(customIdentifier)) {
                throw IllegalStateException("The Identifier '$customIdentifier' has already been taken")
            }
            tickHandlers[customIdentifier] = tickHandler
        }

        @JvmStatic
        fun registerToolBeltTemplateIfNotExists(slotName: String, template: ToolBeltSlotTemplate): ToolBeltSlotTemplate? {
            if (GlobalSettings.toolBeltMode.value == ToolBeltMode.DISABLED) {
                return null
            }
            if (bNoMoreToolBeltSlots) {
                throw IllegalStateException("You can't add any more ToolBelt Slots after a player has already been spawned! Please move the creation of extra ToolBelt Slots earlier.")
            }
            val existing = getToolBeltTemplate(slotName)
            if (existing != null) {
                return existing
            }
            toolBeltTemplates.add(template)
            return template
        }

        @JvmStatic
        fun getToolBeltTemplate(slotName: String): ToolBeltSlotTemplate? {
            return toolBeltTemplates.firstOrNull { it.name == slotName }
        }

        @JvmStatic
        fun getToolBeltTemplates(): ImmutableList<ToolBeltSlotTemplate> {
            return ImmutableList.copyOf(toolBeltTemplates)
        }
    }
}
