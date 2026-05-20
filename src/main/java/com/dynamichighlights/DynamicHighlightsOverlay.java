package com.dynamichighlights;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.QuantityFormatter;

public class DynamicHighlightsOverlay extends Overlay
{
	private final Client client;
	private final DynamicHighlightsPlugin plugin;
	private final DynamicHighlightsConfig config;

	@Inject
	private DynamicHighlightsOverlay(Client client, DynamicHighlightsPlugin plugin, DynamicHighlightsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		Map<WorldPoint, Collection<TrackedItem>> grouped = plugin.getTrackedItems().asMap();
		if (grouped.isEmpty())
		{
			return null;
		}

		for (Map.Entry<WorldPoint, Collection<TrackedItem>> entry : grouped.entrySet())
		{
			Collection<TrackedItem> items = entry.getValue();
			if (items.isEmpty())
			{
				continue;
			}

			// Use the tile of the first tracked item to compute screen position.
			Tile tile = items.iterator().next().getTile();
			LocalPoint lp = tile.getLocalLocation();
			if (lp == null)
			{
				continue;
			}

			if (config.drawOutline())
			{
				Polygon poly = Perspective.getCanvasTilePoly(client, lp);
				if (poly != null)
				{
					// Outline color uses the topmost item's color for clarity.
					int topId = items.iterator().next().getItem().getId();
					Color outline = plugin.colorFor(topId);
					Stroke prev = g.getStroke();
					g.setStroke(new BasicStroke(1.5f));
					g.setColor(new Color(outline.getRed(), outline.getGreen(), outline.getBlue(), 180));
					g.drawPolygon(poly);
					g.setStroke(prev);
				}
			}

			if (!config.showName())
			{
				continue;
			}

			int yOffset = 0;
			for (TrackedItem t : items)
			{
				int id = t.getItem().getId();
				Color c = plugin.colorFor(id);

				String label = t.getName();
				if (config.showQuantity() && t.getItem().getQuantity() > 1)
				{
					label = label + " (" + QuantityFormatter.quantityToStackSize(t.getItem().getQuantity()) + ")";
				}

				Point textLoc = Perspective.getCanvasTextLocation(client, g, lp, label, 0);
				if (textLoc == null)
				{
					continue;
				}

				Point shifted = new Point(textLoc.x, textLoc.y - yOffset);
				OverlayUtil.renderTextLocation(g, shifted, label, c);
				yOffset += g.getFontMetrics().getHeight();
			}
		}

		return null;
	}
}
