package net.piofox4.foxfurnace;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.ActionResult;
import net.piofox4.foxfurnace.block.ModBlocks;
import net.piofox4.foxfurnace.config.ModConfig;
import net.piofox4.foxfurnace.item.ModItemGroups;
import net.piofox4.foxfurnace.item.ModItems;
import net.piofox4.foxfurnace.util.ModLootTableModifiers;
import net.piofox4.foxfurnace.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoxFurnace implements ModInitializer {

	public static final String MOD_ID = "foxfurnace";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModItemGroups.registerItemGroups();
		ModLootTableModifiers.modifyLootTables();
		ModItems.registerModItems();
		ConfigHolder<ModConfig> configHolder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		configHolder.getConfig();
		Ref.getSettings();
		configHolder.registerSaveListener((holder, config) -> {
			Ref.getSettings();
			return ActionResult.SUCCESS;
		});
	}
}