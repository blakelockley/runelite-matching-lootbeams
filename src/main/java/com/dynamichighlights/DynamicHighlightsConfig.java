package com.dynamichighlights;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(DynamicHighlightsConfig.GROUP)
public interface DynamicHighlightsConfig extends Config
{
	String GROUP = "dynamichighlights";

	@ConfigItem(
		keyName = "showName",
		name = "Show item name",
		description = "Draw the item's name above the tile.",
		position = 1
	)
	default boolean showName()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showQuantity",
		name = "Show quantity",
		description = "Append the stack quantity to the label.",
		position = 2
	)
	default boolean showQuantity()
	{
		return true;
	}

	@ConfigItem(
		keyName = "minQuantity",
		name = "Min stack value (gp)",
		description = "Hide items whose total GE value is below this. 0 shows everything.",
		position = 3
	)
	@Range(min = 0, max = 100_000_000)
	default int minQuantity()
	{
		return 0;
	}

	@Alpha
	@ConfigItem(
		keyName = "fallbackColor",
		name = "Fallback color",
		description = "Used when no dominant color can be extracted (e.g. monochrome sprites).",
		position = 4
	)
	default Color fallbackColor()
	{
		return new Color(0xFFFF00);
	}

	@ConfigItem(
		keyName = "drawOutline",
		name = "Outline tile",
		description = "Draw a thin colored outline around the item's tile.",
		position = 5
	)
	default boolean drawOutline()
	{
		return false;
	}
}
