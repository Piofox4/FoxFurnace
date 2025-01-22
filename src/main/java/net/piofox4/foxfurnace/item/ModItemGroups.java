package net.piofox4.foxfurnace.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.piofox4.foxfurnace.FoxFurnace;
import net.piofox4.foxfurnace.block.ModBlocks;

public class ModItemGroups {

    public static final ItemGroup FURNACE_BLOCK_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(FoxFurnace.MOD_ID,"furnace_blocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.NETHERITE_FURNACE_BLOCK))
                    .displayName(Text.translatable("itemgroup.foxfurnace.furnace_blocks"))
                    .entries((displayContext, entries) -> {

                        entries.add(ModBlocks.COPPER_FURNACE_BLOCK);
                        entries.add(ModBlocks.IRON_FURNACE_BLOCK);
                        entries.add(ModBlocks.GOLD_FURNACE_BLOCK);
                        entries.add(ModBlocks.EMERALD_FURNACE_BLOCK);
                        entries.add(ModBlocks.DIAMOND_FURNACE_BLOCK);
                        entries.add(ModBlocks.NETHERITE_FURNACE_BLOCK);
                    }).build());

    public static void registerItemGroups() {
        FoxFurnace.LOGGER.info("Registering Item Groups for " + FoxFurnace.MOD_ID);
    }
}
