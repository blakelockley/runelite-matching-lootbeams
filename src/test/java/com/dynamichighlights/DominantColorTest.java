package com.dynamichighlights;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DominantColorTest
{
	private static final Color FALLBACK = new Color(0xFFFF00);

	@Test
	public void returnsFallbackForFullyTransparentImage()
	{
		BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
		// default is fully transparent black
		assertEquals(FALLBACK, DominantColor.extract(img, FALLBACK));
	}

	@Test
	public void returnsFallbackForNullImage()
	{
		assertEquals(FALLBACK, DominantColor.extract(null, FALLBACK));
	}

	@Test
	public void picksDominantHueOverBackgroundClutter()
	{
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		// Fill 80% with vivid red, 20% with mid-gray (which should lose due to low saturation weight).
		for (int y = 0; y < 16; y++)
		{
			for (int x = 0; x < 16; x++)
			{
				int rgb;
				if (x < 13)
				{
					rgb = 0xFFCC2222; // vivid red
				}
				else
				{
					rgb = 0xFF888888; // gray
				}
				img.setRGB(x, y, rgb);
			}
		}

		Color result = DominantColor.extract(img, FALLBACK);
		assertTrue("expected red-dominant, got " + result, result.getRed() > result.getGreen() + 40);
		assertTrue("expected red-dominant, got " + result, result.getRed() > result.getBlue() + 40);
	}

	@Test
	public void ignoresTransparentAndNearBlackPixels()
	{
		BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
		// 4x8 transparent, 4x8 black outline-ish, then a single bright blue pixel.
		for (int y = 0; y < 8; y++)
		{
			for (int x = 0; x < 8; x++)
			{
				if (x < 4)
				{
					img.setRGB(x, y, 0x00000000);
				}
				else
				{
					img.setRGB(x, y, 0xFF050505);
				}
			}
		}
		// Single saturated blue pixel — should still win the histogram.
		img.setRGB(7, 7, 0xFF2244EE);

		Color result = DominantColor.extract(img, FALLBACK);
		assertTrue("expected blue-dominant, got " + result, result.getBlue() > result.getRed() + 40);
		assertTrue("expected blue-dominant, got " + result, result.getBlue() > result.getGreen() + 40);
	}
}
