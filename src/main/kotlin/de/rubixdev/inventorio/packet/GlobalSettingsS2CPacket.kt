package de.rubixdev.inventorio.packet

import com.google.gson.Gson
import com.google.gson.JsonObject
import de.rubixdev.inventorio.config.GlobalSettings
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class GlobalSettingsS2CPacket(private val jsonString: String) : CustomPayload {

    constructor() : this(GlobalSettings.asJson().toString())

    fun consume(context: ClientPlayNetworking.Context) {
        val settingsJson = Gson().fromJson(jsonString, JsonObject::class.java)
        context.client().execute {
            GlobalSettings.syncFromServer(settingsJson)
        }
    }

    override fun getId(): CustomPayload.Id<GlobalSettingsS2CPacket> = identifier

    companion object {
        val identifier: CustomPayload.Id<GlobalSettingsS2CPacket> = CustomPayload.Id(Identifier.of("inventorio", "global_settings"))
        val codec: PacketCodec<RegistryByteBuf, GlobalSettingsS2CPacket> = PacketCodec.tuple(PacketCodecs.STRING, GlobalSettingsS2CPacket::jsonString, ::GlobalSettingsS2CPacket)
    }
}
