package de.rubixdev.inventorio.enchantment

import de.rubixdev.inventorio.config.GlobalSettings
import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class DeepPocketsBookRecipe(category: CraftingRecipeCategory) : SpecialCraftingRecipe(category) {
    override fun matches(craftingRecipeInput: CraftingRecipeInput, world: World): Boolean {
        if (!GlobalSettings.deepPocketsBookCraft.boolValue) {
            return false
        }
        var shells = 0
        var books = 0

        for (i in 0 until craftingRecipeInput.size) {
            val itemStack = craftingRecipeInput.getStackInSlot(i)
            if (SHULKER_SHELL.test(itemStack)) {
                shells++
            }
            if (BOOKS.test(itemStack)) {
                books++
            }
        }
        return shells == 2 && books == 1
    }

    override fun craft(craftingRecipeInput: CraftingRecipeInput, lookup: RegistryWrapper.WrapperLookup): ItemStack {
        val enchantmentReference = lookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(InventorioEnchantments.DEEP_POCKETS)
        return EnchantedBookItem.forEnchantment(EnchantmentLevelEntry(enchantmentReference, 1))
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width >= 2 && height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return SERIALIZER
    }

    override fun isIgnoredInRecipeBook(): Boolean {
        return !GlobalSettings.deepPocketsBookCraft.boolValue
    }

    override fun getIngredients(): DefaultedList<Ingredient> {
        return DefaultedList.copyOf(SHULKER_SHELL, SHULKER_SHELL, BOOKS, SHULKER_SHELL)
    }

    override fun getResult(lookup: RegistryWrapper.WrapperLookup): ItemStack {
        if (!GlobalSettings.deepPocketsBookCraft.boolValue) {
            ItemStack.EMPTY
        }
        val enchantmentReference = lookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(InventorioEnchantments.DEEP_POCKETS)
        return EnchantedBookItem.forEnchantment(EnchantmentLevelEntry(enchantmentReference, 1))
    }

    companion object {
        private val SHULKER_SHELL = Ingredient.ofItems(Items.SHULKER_SHELL)
        private val BOOKS = Ingredient.ofItems(Items.BOOK, Items.WRITABLE_BOOK)
        val SERIALIZER: SpecialRecipeSerializer<DeepPocketsBookRecipe> = SpecialRecipeSerializer(::DeepPocketsBookRecipe)
    }
}
