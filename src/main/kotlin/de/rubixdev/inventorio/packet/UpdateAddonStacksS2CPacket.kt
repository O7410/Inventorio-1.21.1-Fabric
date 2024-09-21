package de.rubixdev.inventorio.packet

import de.rubixdev.inventorio.player.PlayerInventoryAddon
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class UpdateAddonStacksS2CPacket(private val updatedStacks: Map<Int, ItemStack>) : CustomPayload {

    fun consume(context: ClientPlayNetworking.Context) {
        context.client().execute {
            PlayerInventoryAddon.Client.local?.receiveStacksUpdateS2C(updatedStacks)
        }
    }

    fun write(buf: RegistryByteBuf) {
        buf.writeInt(updatedStacks.size)
        for ((index, stack) in updatedStacks) {
            buf.writeInt(index)
            ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, stack)
        }
    }

    override fun getId(): CustomPayload.Id<UpdateAddonStacksS2CPacket> = identifier

    companion object {
        val identifier = CustomPayload.Id<UpdateAddonStacksS2CPacket>(Identifier.of("inventorio", "update_addon_stacks"))
        val codec: PacketCodec<RegistryByteBuf, UpdateAddonStacksS2CPacket> = PacketCodec.of(UpdateAddonStacksS2CPacket::write, UpdateAddonStacksS2CPacket::read)

        fun read(buf: RegistryByteBuf): UpdateAddonStacksS2CPacket {
            val size = buf.readInt()
            val updatedStacks = mutableMapOf<Int, ItemStack>()
            for (i in 0 until size)
                updatedStacks[buf.readInt()] = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf)
            return UpdateAddonStacksS2CPacket(updatedStacks)
        }
    }
}
