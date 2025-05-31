package net.piofox4.foxfurnace.util;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class FurnaceSpeedManager {
    private static Map<String, Integer> FURNACE_SPEED_MAP = null;

    private static void initializeFurnaceSpeedMap() {
        if (FURNACE_SPEED_MAP == null) {
            FURNACE_SPEED_MAP = new HashMap<>();
        }
        FURNACE_SPEED_MAP.put("copper", Ref.minusTotalCookTimeCopper);
        FURNACE_SPEED_MAP.put("iron", Ref.minusTotalCookTimeIron);
        FURNACE_SPEED_MAP.put("gold", Ref.minusTotalCookTimeGold);
        FURNACE_SPEED_MAP.put("emerald", Ref.minusTotalCookTimeEmerald);
        FURNACE_SPEED_MAP.put("diamond", Ref.minusTotalCookTimeDiamond);
        FURNACE_SPEED_MAP.put("netherite", Ref.minusTotalCookTimeNetherite);
    }

    public static void resetFurnaceSpeedMap() {
        FURNACE_SPEED_MAP = null;
    }
    public static void updateFurnaceSpeedMap() {
        resetFurnaceSpeedMap();
        initializeFurnaceSpeedMap();
    }

    public static void forceUpdate() {
        updateFurnaceSpeedMap();
    }

    public static int getFurnaceSpeedReduction(AbstractFurnaceBlockEntity blockEntity) {
        initializeFurnaceSpeedMap();

        if (blockEntity == null) return 0;

        Identifier blockId = Registries.BLOCK_ENTITY_TYPE.getId(blockEntity.getType());
        if (blockId == null) return 0;

        String blockIdString = blockId.toString().toLowerCase();

        for (Map.Entry<String, Integer> entry : FURNACE_SPEED_MAP.entrySet()) {
            if (blockIdString.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return 0;
    }

    public static int getAdjustedCookTime(int originalCookTime, AbstractFurnaceBlockEntity blockEntity) {
        int reduction = getFurnaceSpeedReduction(blockEntity);
        if (reduction > 0) {
            int newCookTime = originalCookTime - reduction;
            return Math.max(1, newCookTime);
        }
        return originalCookTime;
    }

    public static int getAdjustedFuelTime(int originalFuelTime, AbstractFurnaceBlockEntity blockEntity) {
        int reduction = getFurnaceSpeedReduction(blockEntity);

        if (reduction > 0) {
            int originalCookTime = 200;
            int reducedCookTime = originalCookTime - reduction;

            if (reducedCookTime > 0) {
                float reductionFactor = (float) reducedCookTime / originalCookTime;
                return Math.max(1, Math.round(originalFuelTime * reductionFactor));
            }
        }

        return originalFuelTime;
    }
}