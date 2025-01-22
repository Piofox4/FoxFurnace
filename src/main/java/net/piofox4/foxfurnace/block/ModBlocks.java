package net.piofox4.foxfurnace.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.piofox4.foxfurnace.FoxFurnace;
import net.piofox4.foxfurnace.block.entity.*;

public class ModBlocks {

    public static final Block COPPER_FURNACE_BLOCK = registerBlock("copper_furnace",
            new CopperFurnaceBlock(AbstractBlock.Settings.copy(Blocks.FURNACE).mapColor(MapColor.ORANGE)
                    .strength(3.0F, 6.0F).sounds(BlockSoundGroup.COPPER)));

    public static final BlockEntityType<CopperFurnaceBlockEntity> COPPER_FURNACE_ENTITY_TYPE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,Identifier.of(FoxFurnace.MOD_ID,"copper_furnace"),
                    BlockEntityType.Builder.create(CopperFurnaceBlockEntity::new,COPPER_FURNACE_BLOCK).build());

    public static final Block IRON_FURNACE_BLOCK = registerBlock("iron_furnace",
            new IronFurnaceBlock(AbstractBlock.Settings.copy(Blocks.FURNACE).mapColor(MapColor.IRON_GRAY)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE).strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)));

    public static final BlockEntityType<IronFurnaceBlockEntity> IRON_FURNACE_ENTITY_TYPE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,Identifier.of(FoxFurnace.MOD_ID,"iron_furnace"),
                    BlockEntityType.Builder.create(IronFurnaceBlockEntity::new,IRON_FURNACE_BLOCK).build());

    public static final Block GOLD_FURNACE_BLOCK = registerBlock("gold_furnace",
            new GoldFurnaceBlock(AbstractBlock.Settings.copy(Blocks.FURNACE).mapColor(MapColor.GOLD).instrument(NoteBlockInstrument.BELL)
                    .strength(3.0F, 6.0F).sounds(BlockSoundGroup.METAL)));

    public static final BlockEntityType<GoldFurnaceBlockEntity> GOLD_FURNACE_ENTITY_TYPE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,Identifier.of(FoxFurnace.MOD_ID,"gold_furnace"),
                    BlockEntityType.Builder.create(GoldFurnaceBlockEntity::new,GOLD_FURNACE_BLOCK).build());

    public static final Block EMERALD_FURNACE_BLOCK = registerBlock("emerald_furnace",
            new EmeraldFurnaceBlock(AbstractBlock.Settings.copy(Blocks.FURNACE).mapColor(MapColor.EMERALD_GREEN)
                    .instrument(NoteBlockInstrument.BIT).strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)));

    public static final BlockEntityType<EmeraldFurnaceBlockEntity> EMERALD_FURNACE_ENTITY_TYPE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,Identifier.of(FoxFurnace.MOD_ID,"emerald_furnace"),
                    BlockEntityType.Builder.create(EmeraldFurnaceBlockEntity::new,EMERALD_FURNACE_BLOCK).build());

    public static final Block DIAMOND_FURNACE_BLOCK = registerBlock("diamond_furnace",
            new DiamondFurnaceBlock(AbstractBlock.Settings.copy(Blocks.FURNACE).mapColor(MapColor.DIAMOND_BLUE)
                    .strength(5.0F, 6.0F).sounds(BlockSoundGroup.METAL)));

    public static final BlockEntityType<DiamondFurnaceBlockEntity> DIAMOND_FURNACE_ENTITY_TYPE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,Identifier.of(FoxFurnace.MOD_ID,"diamond_furnace"),
                    BlockEntityType.Builder.create(DiamondFurnaceBlockEntity::new,DIAMOND_FURNACE_BLOCK).build());

    public static final Block NETHERITE_FURNACE_BLOCK = registerBlock("netherite_furnace",
            new NetheriteFurnaceBlock(AbstractBlock.Settings.copy(Blocks.FURNACE).strength(50.0F,1200.0F)
                    .mapColor(MapColor.BLACK).sounds(BlockSoundGroup.NETHERITE)));

    public static final BlockEntityType<NetheriteFurnaceBlockEntity> NETHERITE_FURNACE_ENTITY_TYPE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,Identifier.of(FoxFurnace.MOD_ID,"netherite_furnace"),
                    BlockEntityType.Builder.create(NetheriteFurnaceBlockEntity::new,NETHERITE_FURNACE_BLOCK).build());


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(FoxFurnace.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        if(name.contains("netherite"))
            Registry.register(Registries.ITEM, Identifier.of(FoxFurnace.MOD_ID, name),
                    new BlockItem(block, new Item.Settings().maxCount(64).fireproof().rarity(getRarity(name))));
        else
            Registry.register(Registries.ITEM, Identifier.of(FoxFurnace.MOD_ID, name),
                new BlockItem(block, new Item.Settings().maxCount(64).rarity(getRarity(name))));
    }

    private static Rarity getRarity(String name) {
        if(name.contains("copper") || name.contains("iron"))
            return Rarity.COMMON;
        else if (name.contains("gold"))
            return Rarity.UNCOMMON;
        else if (name.contains("diamond") || name.contains("emerald"))
            return Rarity.RARE;
        else if (name.contains("netherite"))
            return Rarity.EPIC;

        return Rarity.COMMON;
    }

    public static void registerModBlocks() {
        FoxFurnace.LOGGER.info("Registering Mod Blocks for " + FoxFurnace.MOD_ID);
    }
}
