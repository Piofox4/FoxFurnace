package net.piofox4.foxfurnace.util;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.util.Identifier;
import net.piofox4.foxfurnace.item.ModItems;

public class ModLootTableModifiers {

    private static final Identifier WARDEN_ID = Identifier.ofVanilla("entities/warden");

    public static void modifyLootTables() {

        LootTableEvents.MODIFY.register((registryKey, builder, lootTableSource, wrapperLookup) ->{

            if(WARDEN_ID.equals(registryKey.getValue())){
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(KilledByPlayerLootCondition.builder())
                        .with(ItemEntry.builder(ModItems.WARDEN_HEART))
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f,1.0f)));

                builder.pool(poolBuilder.build());
            }
        });

    }

}