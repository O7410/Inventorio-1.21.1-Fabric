package de.rubixdev.inventorio.packet

import com.mojang.datafixers.util.Pair
import de.rubixdev.inventorio.player.PlayerInventoryAddon
import de.rubixdev.inventorio.player.PlayerInventoryAddon.Companion.inventoryAddon
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.EquipmentSlot
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier

class SelectUtilitySlotPacket(private val utilitySlot: Byte) : CustomPayload {

    // Server's receiving consumer
    fun consume(context: ServerPlayNetworking.Context) {
        context.server().execute {
            context.player().inventoryAddon?.selectedUtility = utilitySlot.toInt()

            // Resending the current offhand item (aka a selected utility belt item) of this player to other players
            val broadcastPacket = EntityEquipmentUpdateS2CPacket(context.player().id, listOf(Pair(EquipmentSlot.OFFHAND, context.player().offHandStack)))
            (context.player().world as ServerWorld).chunkManager.sendToOtherNearbyPlayers(context.player(), broadcastPacket)
        }
    }

    // Client's receiving consumer
    fun consume(context: ClientPlayNetworking.Context) {
        context.client().execute {
            PlayerInventoryAddon.Client.local?.selectedUtility = utilitySlot.toInt()
        }
    }

    override fun getId(): CustomPayload.Id<SelectUtilitySlotPacket> = identifier

    companion object {
        val identifier = CustomPayload.Id<SelectUtilitySlotPacket>(Identifier.of("inventorio", "select_utility"))
        val codec: PacketCodec<RegistryByteBuf, SelectUtilitySlotPacket> = PacketCodec.tuple(PacketCodecs.BYTE, SelectUtilitySlotPacket::utilitySlot, ::SelectUtilitySlotPacket)
    }
}
