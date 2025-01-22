package net.piofox4.foxfurnace.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.piofox4.foxfurnace.block.ModBlocks;
import net.piofox4.foxfurnace.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter recipeExporter) {

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.COPPER_FURNACE_BLOCK)
                .pattern("III")
                .pattern("BFB")
                .pattern("III")
                .input('I',Items.COPPER_INGOT)
                .input('B',Items.COPPER_BLOCK)
                .input('F',Items.FURNACE)
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .criterion(hasItem(Items.COPPER_BLOCK), conditionsFromItem(Items.COPPER_BLOCK))
                .criterion(hasItem(Items.FURNACE), conditionsFromItem(Items.FURNACE))
                .offerTo(recipeExporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.IRON_FURNACE_BLOCK)
                .pattern("III")
                .pattern("BFB")
                .pattern("III")
                .input('I',Items.IRON_INGOT)
                .input('B',Items.IRON_BLOCK)
                .input('F',ModBlocks.COPPER_FURNACE_BLOCK)
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .criterion(hasItem(Items.IRON_BLOCK), conditionsFromItem(Items.IRON_BLOCK))
                .criterion(hasItem(ModBlocks.COPPER_FURNACE_BLOCK), conditionsFromItem(ModBlocks.COPPER_FURNACE_BLOCK))
                .offerTo(recipeExporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.GOLD_FURNACE_BLOCK)
                .pattern("IBI")
                .pattern("BFB")
                .pattern("III")
                .input('I',Items.GOLD_INGOT)
                .input('B',Items.GOLD_BLOCK)
                .input('F',ModBlocks.IRON_FURNACE_BLOCK)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .criterion(hasItem(Items.GOLD_BLOCK), conditionsFromItem(Items.GOLD_BLOCK))
                .criterion(hasItem(ModBlocks.IRON_FURNACE_BLOCK), conditionsFromItem(ModBlocks.IRON_FURNACE_BLOCK))
                .offerTo(recipeExporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.EMERALD_FURNACE_BLOCK)
                .pattern("IBI")
                .pattern("BFB")
                .pattern("IBI")
                .input('I',Items.EMERALD)
                .input('B',Items.EMERALD_BLOCK)
                .input('F',ModBlocks.GOLD_FURNACE_BLOCK)
                .criterion(hasItem(Items.EMERALD), conditionsFromItem(Items.EMERALD))
                .criterion(hasItem(Items.EMERALD_BLOCK), conditionsFromItem(Items.EMERALD_BLOCK))
                .criterion(hasItem(ModBlocks.GOLD_FURNACE_BLOCK), conditionsFromItem(ModBlocks.GOLD_FURNACE_BLOCK))
                .offerTo(recipeExporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.DIAMOND_FURNACE_BLOCK)
                .pattern("IBI")
                .pattern("BFB")
                .pattern("IBI")
                .input('I',Items.DIAMOND)
                .input('B',Items.DIAMOND_BLOCK)
                .input('F',ModBlocks.EMERALD_FURNACE_BLOCK)
                .criterion(hasItem(Items.DIAMOND), conditionsFromItem(Items.DIAMOND))
                .criterion(hasItem(Items.DIAMOND_BLOCK), conditionsFromItem(Items.DIAMOND_BLOCK))
                .criterion(hasItem(ModBlocks.EMERALD_FURNACE_BLOCK), conditionsFromItem(ModBlocks.EMERALD_FURNACE_BLOCK))
                .offerTo(recipeExporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.NETHERITE_FURNACE_BLOCK)
                .pattern("IBI")
                .pattern("BFB")
                .pattern("IWI")
                .input('I',Items.NETHERITE_INGOT)
                .input('B',Items.NETHERITE_BLOCK)
                .input('F',ModBlocks.DIAMOND_FURNACE_BLOCK)
                .input('W', ModItems.WARDEN_HEART)
                .criterion(hasItem(Items.NETHERITE_INGOT), conditionsFromItem(Items.NETHERITE_INGOT))
                .criterion(hasItem(Items.NETHERITE_BLOCK), conditionsFromItem(Items.NETHERITE_BLOCK))
                .criterion(hasItem(ModBlocks.DIAMOND_FURNACE_BLOCK), conditionsFromItem(ModBlocks.DIAMOND_FURNACE_BLOCK))
                .criterion(hasItem(ModItems.WARDEN_HEART), conditionsFromItem(ModItems.WARDEN_HEART))
                .offerTo(recipeExporter);

    }

}
