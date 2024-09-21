package de.rubixdev.inventorio.packet

import de.rubixdev.inventorio.player.PlayerInventoryAddon.Companion.inventoryAddon
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

object SwapItemsInHandsKeyC2SPacket : CustomPayload {
    val codec: PacketCodec<RegistryByteBuf, SwapItemsInHandsKeyC2SPacket> = PacketCodec.of({ _, _ -> }, { SwapItemsInHandsKeyC2SPacket })
    val identifier = CustomPayload.Id<SwapItemsInHandsKeyC2SPacket>(Identifier.of("inventorio", "swap_items_in_hands"))

    fun consume(@Suppress("UNUSED_PARAMETER") packet: SwapItemsInHandsKeyC2SPacket, context: ServerPlayNetworking.Context) {
        context.server().execute {
            context.player().inventoryAddon?.swapItemsInHands()
        }
    }

    override fun getId(): CustomPayload.Id<SwapItemsInHandsKeyC2SPacket> = identifier
}
