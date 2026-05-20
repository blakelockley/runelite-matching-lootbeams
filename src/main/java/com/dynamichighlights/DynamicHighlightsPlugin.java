package com.dynamichighlights;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Dynamic Highlights",
	description = "Highlights ground items using each item's dominant sprite color",
	tags = {"ground", "items", "color", "loot"}
)
public class DynamicHighlightsPlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ItemManager itemManager;
	@Inject private OverlayManager overlayManager;
	@Inject private DynamicHighlightsConfig config;
	@Inject private DynamicHighlightsOverlay overlay;

	@Getter
	private final Multimap<WorldPoint, TrackedItem> trackedItems = HashMultimap.create();

	private final ConcurrentMap<Integer, Color> colorCache = new ConcurrentHashMap<>();

	@Provides
	DynamicHighlightsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DynamicHighlightsConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		trackedItems.clear();
		colorCache.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			trackedItems.clear();
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		TileItem item = event.getItem();
		Tile tile = event.getTile();
		WorldPoint wp = tile.getWorldLocation();

		ItemComposition comp = itemManager.getItemComposition(item.getId());
		int unitPrice = comp.getPrice();
		int totalValue = (int) Math.min(Integer.MAX_VALUE, (long) unitPrice * item.getQuantity());
		if (config.minQuantity() > 0 && totalValue < config.minQuantity())
		{
			return;
		}

		trackedItems.put(wp, new TrackedItem(item, tile, comp.getName()));
		ensureColorLoaded(item.getId());
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event)
	{
		TileItem item = event.getItem();
		WorldPoint wp = event.getTile().getWorldLocation();
		trackedItems.get(wp).removeIf(t -> t.getItem() == item);
	}

	@Subscribe
	public void onItemQuantityChanged(ItemQuantityChanged event)
	{
		// Quantity changes don't require a re-color; the cache is keyed by item ID.
	}

	private void ensureColorLoaded(int itemId)
	{
		if (colorCache.containsKey(itemId))
		{
			return;
		}

		AsyncBufferedImage image = itemManager.getImage(itemId);
		image.onLoaded(() -> {
			Color c = DominantColor.extract(image, config.fallbackColor());
			colorCache.put(itemId, c);
		});
	}

	Color colorFor(int itemId)
	{
		return colorCache.getOrDefault(itemId, config.fallbackColor());
	}
}
