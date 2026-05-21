package com.matchinglootbeams;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(MatchingLootbeamsConfig.GROUP)
public interface MatchingLootbeamsConfig extends Config
{
	String GROUP = "matchinglootbeams";

	@ConfigItem(
		keyName = "showBeam",
		name = "Show beam",
		description = "Render a 3D loot beam at each tracked ground item, colored by its dominant sprite color. " +
			"Disable Ground Items' built-in beams in its config to avoid duplicates.",
		position = 1
	)
	default boolean showBeam()
	{
		return true;
	}

	@ConfigItem(
		keyName = "minValue",
		name = "Min value",
		description = "Skip items whose total value is below this. " +
			"Per-item value is max(shop value, GE price), multiplied by the stack quantity. " +
			"0 shows everything.",
		position = 2
	)
	@Range(min = 0, max = 2_147_483_647)
	default int minValue()
	{
		return 0;
	}

	@Alpha
	@ConfigItem(
		keyName = "fallbackColor",
		name = "Fallback color",
		description = "Used when no dominant color can be extracted (e.g. monochrome sprites or load failure).",
		position = 3
	)
	default Color fallbackColor()
	{
		return new Color(0xFFFFFF);
	}
}
