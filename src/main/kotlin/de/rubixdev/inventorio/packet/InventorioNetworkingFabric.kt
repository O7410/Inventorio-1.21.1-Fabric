package de.rubixdev.inventorio.packet

import de.rubixdev.inventorio.player.PlayerInventoryAddon.Companion.inventoryAddon
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

object InventorioNetworkingFabric : InventorioNetworking {
    init {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            PayloadTypeRegistry.playS2C().register(SelectUtilitySlotPacket.identifier, SelectUtilitySlotPacket.codec)
            ClientPlayNetworking.registerGlobalReceiver(SelectUtilitySlotPacket.identifier, SelectUtilitySlotPacket::consume)
            PayloadTypeRegistry.playS2C().register(GlobalSettingsS2CPacket.identifier, GlobalSettingsS2CPacket.codec)
            ClientPlayNetworking.registerGlobalReceiver(GlobalSettingsS2CPacket.identifier, GlobalSettingsS2CPacket::consume)
            PayloadTypeRegistry.playS2C().register(UpdateAddonStacksS2CPacket.identifier, UpdateAddonStacksS2CPacket.codec)
            ClientPlayNetworking.registerGlobalReceiver(UpdateAddonStacksS2CPacket.identifier, UpdateAddonStacksS2CPacket::consume)
        }

        PayloadTypeRegistry.playC2S().register(UseBoostRocketC2SPacket.identifier, UseBoostRocketC2SPacket.codec)
        ServerPlayNetworking.registerGlobalReceiver(UseBoostRocketC2SPacket.identifier, UseBoostRocketC2SPacket::consume)
        PayloadTypeRegistry.playC2S().register(SelectUtilitySlotPacket.identifier, SelectUtilitySlotPacket.codec)
        ServerPlayNetworking.registerGlobalReceiver(SelectUtilitySlotPacket.identifier, SelectUtilitySlotPacket::consume)
        PayloadTypeRegistry.playC2S().register(SwapItemsInHandsKeyC2SPacket.identifier, SwapItemsInHandsKeyC2SPacket.codec)
        ServerPlayNetworking.registerGlobalReceiver(SwapItemsInHandsKeyC2SPacket.identifier, SwapItemsInHandsKeyC2SPacket::consume)
        PayloadTypeRegistry.playC2S().register(SwappedHandsModeC2SPacket.identifier, SwappedHandsModeC2SPacket.codec)
        ServerPlayNetworking.registerGlobalReceiver(SwappedHandsModeC2SPacket.identifier, SwappedHandsModeC2SPacket::consume)
        PayloadTypeRegistry.playC2S().register(MoveItemToUtilityBeltC2SPacket.identifier, MoveItemToUtilityBeltC2SPacket.codec)
        ServerPlayNetworking.registerGlobalReceiver(MoveItemToUtilityBeltC2SPacket.identifier, MoveItemToUtilityBeltC2SPacket::consume)
        PayloadTypeRegistry.playC2S().register(OpenInventorioScreenC2SPacket.identifier, OpenInventorioScreenC2SPacket.codec)
        ServerPlayNetworking.registerGlobalReceiver(OpenInventorioScreenC2SPacket.identifier, OpenInventorioScreenC2SPacket::consume)
    }

    override fun s2cSelectUtilitySlot(player: ServerPlayerEntity) {
        val inventoryAddon = player.inventoryAddon ?: return
        ServerPlayNetworking.send(player, SelectUtilitySlotPacket(inventoryAddon.selectedUtility.toByte()))
    }

    override fun s2cGlobalSettings(player: ServerPlayerEntity) {
        ServerPlayNetworking.send(player, GlobalSettingsS2CPacket())
    }

    override fun s2cUpdateAddonStacks(player: ServerPlayerEntity, updatedStacks: Map<Int, ItemStack>) {
        ServerPlayNetworking.send(player, UpdateAddonStacksS2CPacket(updatedStacks))
    }

    @Environment(EnvType.CLIENT)
    override fun c2sSelectUtilitySlot(selectedUtility: Int) {
        ClientPlayNetworking.send(SelectUtilitySlotPacket(selectedUtility.toByte()))
    }

    @Environment(EnvType.CLIENT)
    override fun c2sUseBoostRocket() {
        ClientPlayNetworking.send(UseBoostRocketC2SPacket)
    }

    @Environment(EnvType.CLIENT)
    override fun c2sSetSwappedHandsMode(swappedHands: Boolean) {
        if (MinecraftClient.getInstance().networkHandler != null) {
            ClientPlayNetworking.send(SwappedHandsModeC2SPacket(swappedHands))
        }
    }

    @Environment(EnvType.CLIENT)
    override fun c2sMoveItemToUtilityBelt(sourceSlot: Int) {
        ClientPlayNetworking.send(MoveItemToUtilityBeltC2SPacket(sourceSlot.toByte()))
    }

    @Environment(EnvType.CLIENT)
    override fun c2sOpenInventorioScreen() {
        ClientPlayNetworking.send(OpenInventorioScreenC2SPacket)
    }

    override fun c2sSwapItemsInHands() {
        ClientPlayNetworking.send(SwapItemsInHandsKeyC2SPacket)
    }
}
