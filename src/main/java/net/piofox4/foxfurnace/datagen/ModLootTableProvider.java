package net.piofox4.foxfurnace.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;
import net.piofox4.foxfurnace.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {

    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.COPPER_FURNACE_BLOCK);
        addDrop(ModBlocks.IRON_FURNACE_BLOCK);
        addDrop(ModBlocks.GOLD_FURNACE_BLOCK);
        addDrop(ModBlocks.EMERALD_FURNACE_BLOCK);
        addDrop(ModBlocks.DIAMOND_FURNACE_BLOCK);
        addDrop(ModBlocks.NETHERITE_FURNACE_BLOCK);
    }

}
