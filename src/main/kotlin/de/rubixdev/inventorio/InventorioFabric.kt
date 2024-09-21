package de.rubixdev.inventorio

import de.rubixdev.inventorio.api.InventorioAPI
import de.rubixdev.inventorio.client.control.InventorioControls
import de.rubixdev.inventorio.client.control.InventorioKeyHandler
import de.rubixdev.inventorio.config.PlayerSettings
import de.rubixdev.inventorio.enchantment.DeepPocketsBookRecipe
import de.rubixdev.inventorio.integration.ClumpsIntegration
import de.rubixdev.inventorio.integration.InventorioModIntegration
import de.rubixdev.inventorio.integration.ModIntegration
import de.rubixdev.inventorio.packet.InventorioNetworking
import de.rubixdev.inventorio.packet.InventorioNetworkingFabric
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

open class InventorioFabric : ModInitializer {
    private val fabricModIntegrations = listOf<ModIntegration>(ClumpsIntegration)

    override fun onInitialize() {
        ScreenTypeProvider.INSTANCE = ScreenTypeProviderFabric
        InventorioNetworking.INSTANCE = InventorioNetworkingFabric
//        Registry.register(Registries.ENCHANTMENT, Identifier.of("inventorio", "deep_pockets"), DeepPocketsEnchantment)
        Registry.register(
            Registries.RECIPE_SERIALIZER,
            Identifier.of("inventorio", "deep_pockets_book"),
            //#if MC >= 12002
            DeepPocketsBookRecipe.SERIALIZER,
            //#else
            //$$ SpecialRecipeSerializer { ident, category -> DeepPocketsBookRecipe(ident, category) },
            //#endif
        )

        initToolBelt()

        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { InventorioKeyHandler.tick() })
            InventorioControls.keys.forEach { KeyBindingHelper.registerKeyBinding(it) }
            PlayerSettings.load(FabricLoader.getInstance().configDir.resolve("inventorio.json").toFile())
            ScreenTypeProviderFabric.registerScreen()
        }

        InventorioModIntegration.applyModIntegrations(fabricModIntegrations)
    }

    private fun initToolBelt() {
        // What this actually does is loads the [InventorioAPI] which creates the ToolBelt
        // The reason why we do it this way is that we can't guarantee that other mods
        // won't call [InventorioAPI] BEFORE [InventorioFabric#onInitialize] has been invoked
        InventorioAPI.getToolBeltSlotTemplate(InventorioAPI.SLOT_PICKAXE)
            ?.addAllowingTag(Identifier.of("fabric", "pickaxes"))
            ?.addAllowingTag(Identifier.of("fabric", "hammers"))

        InventorioAPI.getToolBeltSlotTemplate(InventorioAPI.SLOT_SWORD)
            ?.addAllowingTag(Identifier.of("fabric", "swords"))
            ?.addAllowingTag(Identifier.of("fabric", "tridents"))
            ?.addAllowingTag(Identifier.of("fabric", "battleaxes"))

        InventorioAPI.getToolBeltSlotTemplate(InventorioAPI.SLOT_AXE)
            ?.addAllowingTag(Identifier.of("fabric", "axes"))
            ?.addAllowingTag(Identifier.of("fabric", "battleaxes"))

        InventorioAPI.getToolBeltSlotTemplate(InventorioAPI.SLOT_SHOVEL)
            ?.addAllowingTag(Identifier.of("fabric", "shovels"))
            ?.addAllowingTag(Identifier.of("fabric", "mattocks"))

        InventorioAPI.getToolBeltSlotTemplate(InventorioAPI.SLOT_HOE)
            ?.addAllowingTag(Identifier.of("fabric", "hoes"))
            ?.addAllowingTag(Identifier.of("fabric", "shears"))
    }
}
