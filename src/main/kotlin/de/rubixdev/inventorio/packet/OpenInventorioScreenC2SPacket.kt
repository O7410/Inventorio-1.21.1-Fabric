package de.rubixdev.inventorio.packet

import de.rubixdev.inventorio.player.InventorioScreenHandler
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@Suppress("UNUSED_PARAMETER")
object OpenInventorioScreenC2SPacket : CustomPayload {
    val codec: PacketCodec<RegistryByteBuf, OpenInventorioScreenC2SPacket> = PacketCodec.of({ _, _ -> }, { OpenInventorioScreenC2SPacket })
    val identifier = CustomPayload.Id<OpenInventorioScreenC2SPacket>(Identifier.of("inventorio", "open_screen"))

    fun consume(packet: OpenInventorioScreenC2SPacket, context: ServerPlayNetworking.Context) {
        context.server().execute {
            InventorioScreenHandler.open(context.player())
        }
    }

    override fun getId(): CustomPayload.Id<OpenInventorioScreenC2SPacket> = identifier
}
