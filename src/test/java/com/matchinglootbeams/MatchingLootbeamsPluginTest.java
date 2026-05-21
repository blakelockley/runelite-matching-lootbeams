package com.matchinglootbeams;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Boots the full RuneLite client with this plugin side-loaded.
 * Run this main() from IntelliJ to develop and debug the plugin.
 */
public class MatchingLootbeamsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MatchingLootbeamsPlugin.class);
		RuneLite.main(args);
	}
}
