package com.dynamichighlights;

import lombok.Value;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;

@Value
public class TrackedItem
{
	TileItem item;
	Tile tile;
	String name;
}
