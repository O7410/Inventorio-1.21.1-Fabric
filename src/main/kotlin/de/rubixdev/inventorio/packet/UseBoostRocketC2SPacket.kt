package de.rubixdev.inventorio.packet

import de.rubixdev.inventorio.player.PlayerInventoryAddon.Companion.inventoryAddon
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

object UseBoostRocketC2SPacket : CustomPayload {
    val codec: PacketCodec<RegistryByteBuf, UseBoostRocketC2SPacket> = PacketCodec.of({ _, _ -> }, { UseBoostRocketC2SPacket })
    val identifier = CustomPayload.Id<UseBoostRocketC2SPacket>(Identifier.of("inventorio", "fire_boost_rocket_c2s"))

    fun consume(packet: UseBoostRocketC2SPacket, context: ServerPlayNetworking.Context) {
        context.server().execute {
            context.player().inventoryAddon?.fireRocketFromInventory()
        }
    }

    override fun getId(): CustomPayload.Id<UseBoostRocketC2SPacket> = identifier
}
