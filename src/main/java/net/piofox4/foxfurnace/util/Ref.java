package net.piofox4.foxfurnace.util;

import me.shedaniel.autoconfig.AutoConfig;
import net.piofox4.foxfurnace.config.ModConfig;

public class Ref {

    public static ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

    public static int minusTotalCookTimeCopper;
    public static int minusTotalCookTimeIron;
    public static int minusTotalCookTimeGold;
    public static int minusTotalCookTimeEmerald;
    public static int minusTotalCookTimeDiamond;
    public static int minusTotalCookTimeNetherite;

    public static void getSettings(){
        minusTotalCookTimeCopper = config.minusTotalCookTimeCopper;
        minusTotalCookTimeIron = config.minusTotalCookTimeIron;
        minusTotalCookTimeGold = config.minusTotalCookTimeGold;
        minusTotalCookTimeEmerald = config.minusTotalCookTimeEmerald;
        minusTotalCookTimeDiamond = config.minusTotalCookTimeDiamond;
        minusTotalCookTimeNetherite = config.minusTotalCookTimeNetherite;
    }
}
