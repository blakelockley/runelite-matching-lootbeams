package com.matchinglootbeams;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Matching Lootbeams",
	description = "Colors ground item loot beams by the item's dominant sprite color",
	tags = {"ground", "items", "color", "loot", "beam"}
)
public class MatchingLootbeamsPlugin extends Plugin
{
	private static final int MAX_LOAD_RETRIES = 30;

	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ItemManager itemManager;
	@Inject private MatchingLootbeamsConfig config;

	private final ConcurrentMap<Integer, Color> colorCache = new ConcurrentHashMap<>();
	private final ConcurrentMap<Integer, Integer> loadAttempts = new ConcurrentHashMap<>();

	// Beams are 3D scene objects — accessed only on the client thread.
	private final Map<TileItem, Lootbeam> beams = new HashMap<>();

	@Provides
	@SuppressWarnings("unused")
	MatchingLootbeamsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MatchingLootbeamsConfig.class);
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(this::destroyAllBeams);
		colorCache.clear();
		loadAttempts.clear();
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			destroyAllBeams();
		}
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onItemSpawned(ItemSpawned event)
	{
		TileItem item = event.getItem();
		Tile tile = event.getTile();
		int itemId = item.getId();

		if (config.minValue() > 0)
		{
			ItemComposition comp = itemManager.getItemComposition(itemId);
			int shopValue = comp.getPrice();
			int gePrice = itemManager.getItemPrice(itemId);
			int unitValue = Math.max(shopValue, gePrice);
			long totalValue = (long) unitValue * item.getQuantity();
			if (totalValue < config.minValue())
			{
				return;
			}
		}

		ensureColorLoaded(itemId, color -> spawnBeam(item, tile, color));
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onItemDespawned(ItemDespawned event)
	{
		destroyBeam(event.getItem());
	}

	private void spawnBeam(TileItem item, Tile tile, Color color)
	{
		// Runs on the client thread (invoked from ensureColorLoaded's invokeLater).
		if (!config.showBeam())
		{
			return;
		}
		if (beams.containsKey(item))
		{
			return;
		}

		WorldPoint wp = tile.getWorldLocation();
		Lootbeam beam = new Lootbeam(client, clientThread, wp, color, Lootbeam.Style.MODERN);
		beams.put(item, beam);
	}

	private void destroyBeam(TileItem item)
	{
		Lootbeam beam = beams.remove(item);
		if (beam != null)
		{
			clientThread.invoke(beam::remove);
		}
	}

	private void destroyAllBeams()
	{
		for (Lootbeam beam : beams.values())
		{
			beam.remove();
		}
		beams.clear();
	}

	private void ensureColorLoaded(int itemId, Consumer<Color> onReady)
	{
		Color cached = colorCache.get(itemId);
		if (cached != null)
		{
			clientThread.invoke(() -> onReady.accept(cached));
			return;
		}

		clientThread.invokeLater(() -> {
			Color have = colorCache.get(itemId);
			if (have != null)
			{
				onReady.accept(have);
				return true;
			}

			BufferedImage image = itemManager.getImage(itemId);
			Color extracted = image == null ? null : DominantColor.extract(image, null);
			if (extracted != null)
			{
				colorCache.put(itemId, extracted);
				loadAttempts.remove(itemId);
				onReady.accept(extracted);
				return true;
			}

			int attempts = loadAttempts.merge(itemId, 1, Integer::sum);
			if (attempts >= MAX_LOAD_RETRIES)
			{
				Color fb = config.fallbackColor();
				colorCache.put(itemId, fb);
				loadAttempts.remove(itemId);
				onReady.accept(fb);
				return true;
			}
			return false;
		});
	}
}
