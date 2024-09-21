package de.rubixdev.inventorio.packet

import de.rubixdev.inventorio.player.PlayerInventoryAddon.Companion.inventoryAddon
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class SwappedHandsModeC2SPacket(private val swappedHands: Boolean) : CustomPayload {

    fun consume(context: ServerPlayNetworking.Context) {
        context.server().execute {
            context.player().inventoryAddon?.swappedHands = swappedHands
        }
    }

    override fun getId(): CustomPayload.Id<SwappedHandsModeC2SPacket> = identifier

    companion object {
        val codec: PacketCodec<RegistryByteBuf, SwappedHandsModeC2SPacket> = PacketCodec.tuple(PacketCodecs.BOOL, SwappedHandsModeC2SPacket::swappedHands, ::SwappedHandsModeC2SPacket)
        val identifier = CustomPayload.Id<SwappedHandsModeC2SPacket>(Identifier.of("inventorio", "swapped_hands"))
    }
}
