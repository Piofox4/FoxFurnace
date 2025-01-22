package net.piofox4.foxfurnace.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.piofox4.foxfurnace.FoxFurnace;

public class ModItems {


    public static final Item WARDEN_HEART = registerItem("warden_heart", new Item(new Item.Settings()
            .rarity(Rarity.EPIC).maxCount(64)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(FoxFurnace.MOD_ID, name), item);
    }

    public static void registerModItems() {
        FoxFurnace.LOGGER.info("Registering Mod Items for " + FoxFurnace.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(WARDEN_HEART);
        });
    }

}
