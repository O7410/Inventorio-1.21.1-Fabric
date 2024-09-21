package de.rubixdev.inventorio.enchantment

import de.rubixdev.inventorio.util.DEEP_POCKETS_MAX_LEVEL
import net.minecraft.component.type.AttributeModifierSlot
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier

object InventorioEnchantments {

    val DEEP_POCKETS: RegistryKey<Enchantment> = of("deep_pockets")

    fun bootstrap(registry: Registerable<Enchantment>) {
        val itemRegistryEntryLookup = registry.getRegistryLookup(RegistryKeys.ITEM)
        register(
            registry,
            DEEP_POCKETS,
            Enchantment.builder(
                Enchantment.definition(
                    itemRegistryEntryLookup.getOrThrow(ItemTags.LEG_ARMOR_ENCHANTABLE),
                    5,
                    DEEP_POCKETS_MAX_LEVEL,
                    Enchantment.leveledCost(5, 8),
                    Enchantment.leveledCost(61, 10), // original functionality, maybe a bug
//                    Enchantment.leveledCost(55, 8), // modified functionality inferred by the original
                    2,
                    AttributeModifierSlot.LEGS,
                ),
            ),
        )
    }

    private fun register(
        registry: Registerable<Enchantment>,
        key: RegistryKey<Enchantment>,
        builder: Enchantment.Builder,
    ) {
        registry.register(key, builder.build(key.value))
    }

    private fun of(id: String): RegistryKey<Enchantment> {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("inventorio", id))
    }
}
