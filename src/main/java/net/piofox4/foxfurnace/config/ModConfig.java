package net.piofox4.foxfurnace.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui;

@Config(name = "foxfurnace")
public class ModConfig implements ConfigData {


    @ConfigEntry.BoundedDiscrete(min = 0, max = 199)
    @Gui.Tooltip(count = 4)
    public int minusTotalCookTimeCopper = 20;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 199)
    @Gui.Tooltip(count = 4)
    public int minusTotalCookTimeIron = 40;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 199)
    @Gui.Tooltip(count = 4)
    public int minusTotalCookTimeGold = 80;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 199)
    @Gui.Tooltip(count = 4)
    public int minusTotalCookTimeEmerald = 100;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 199)
    @Gui.Tooltip(count = 4)
    public int minusTotalCookTimeDiamond = 130;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 199)
    @Gui.Tooltip(count = 4)
    public int minusTotalCookTimeNetherite = 160;

}
