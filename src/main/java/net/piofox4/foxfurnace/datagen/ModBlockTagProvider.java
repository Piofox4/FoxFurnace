package net.piofox4.foxfurnace.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.piofox4.foxfurnace.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {

    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.COPPER_FURNACE_BLOCK)
                .add(ModBlocks.IRON_FURNACE_BLOCK)
                .add(ModBlocks.GOLD_FURNACE_BLOCK)
                .add(ModBlocks.EMERALD_FURNACE_BLOCK)
                .add(ModBlocks.DIAMOND_FURNACE_BLOCK)
                .add(ModBlocks.NETHERITE_FURNACE_BLOCK);

        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.COPPER_FURNACE_BLOCK)
                .add(ModBlocks.IRON_FURNACE_BLOCK);

        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.GOLD_FURNACE_BLOCK)
                .add(ModBlocks.EMERALD_FURNACE_BLOCK)
                .add(ModBlocks.DIAMOND_FURNACE_BLOCK);

        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.NETHERITE_FURNACE_BLOCK);
    }
}
