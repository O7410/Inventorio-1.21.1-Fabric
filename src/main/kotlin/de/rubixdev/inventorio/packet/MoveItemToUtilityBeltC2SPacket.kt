package de.rubixdev.inventorio.packet

import de.rubixdev.inventorio.player.InventorioScreenHandler.Companion.inventorioScreenHandler
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class MoveItemToUtilityBeltC2SPacket(private val sourceSlot: Byte) : CustomPayload {

    fun consume(context: ServerPlayNetworking.Context) {
        context.server().execute {
            val screenHandler = context.player().inventorioScreenHandler ?: return@execute
            screenHandler.tryTransferToUtilityBeltSlot(screenHandler.getSlot(sourceSlot.toInt()))
        }
    }

    override fun getId(): CustomPayload.Id<MoveItemToUtilityBeltC2SPacket> = identifier

    companion object {
        val codec: PacketCodec<RegistryByteBuf, MoveItemToUtilityBeltC2SPacket> = PacketCodec.tuple(PacketCodecs.BYTE, MoveItemToUtilityBeltC2SPacket::sourceSlot, ::MoveItemToUtilityBeltC2SPacket)
        val identifier = CustomPayload.Id<MoveItemToUtilityBeltC2SPacket>(Identifier.of("inventorio", "move_to_utility_c2s"))
    }
}
