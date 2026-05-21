package com.matchinglootbeams;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Extracts a representative color from an item sprite by quantizing pixels into
 * a coarse RGB histogram and returning the centroid of the heaviest bucket.
 *
 * Pixels are weighted by saturation so neutral grays (UI shadows, outlines)
 * do not dominate visually-vivid items. Near-transparent and near-black /
 * near-white pixels are skipped.
 */
public final class DominantColor
{
	// 4 bits per channel → 16 levels per channel → 4096 buckets total.
	private static final int BITS_PER_CHANNEL = 4;
	private static final int LEVELS = 1 << BITS_PER_CHANNEL;
	private static final int SHIFT = 8 - BITS_PER_CHANNEL;
	private static final int BUCKETS = LEVELS * LEVELS * LEVELS;

	private static final int ALPHA_THRESHOLD = 128;
	private static final int MIN_LUMA = 24;
	private static final int MAX_LUMA = 240;

	private DominantColor()
	{
	}

	public static Color extract(BufferedImage image, Color fallback)
	{
		if (image == null || image.getWidth() == 0 || image.getHeight() == 0)
		{
			return fallback;
		}

		final int w = image.getWidth();
		final int h = image.getHeight();
		final int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);

		final double[] weight = new double[BUCKETS];
		final int[] count = new int[BUCKETS];
		final long[] rSum = new long[BUCKETS];
		final long[] gSum = new long[BUCKETS];
		final long[] bSum = new long[BUCKETS];

		for (int argb : pixels)
		{
			final int a = (argb >>> 24) & 0xFF;
			if (a < ALPHA_THRESHOLD)
			{
				continue;
			}

			final int r = (argb >>> 16) & 0xFF;
			final int g = (argb >>> 8) & 0xFF;
			final int b = argb & 0xFF;

			final int luma = (r * 299 + g * 587 + b * 114) / 1000;
			if (luma < MIN_LUMA || luma > MAX_LUMA)
			{
				continue;
			}

			final int max = Math.max(r, Math.max(g, b));
			final int min = Math.min(r, Math.min(g, b));
			final double saturation = max == 0 ? 0.0 : (max - min) / (double) max;
			final double pixelWeight = 0.05 + saturation * saturation;

			final int bucket = ((r >>> SHIFT) * LEVELS + (g >>> SHIFT)) * LEVELS + (b >>> SHIFT);
			weight[bucket] += pixelWeight;
			count[bucket]++;
			rSum[bucket] += r;
			gSum[bucket] += g;
			bSum[bucket] += b;
		}

		int bestBucket = -1;
		double bestWeight = 0.0;
		for (int i = 0; i < BUCKETS; i++)
		{
			if (weight[i] > bestWeight)
			{
				bestWeight = weight[i];
				bestBucket = i;
			}
		}

		if (bestBucket < 0)
		{
			return fallback;
		}

		final int n = count[bestBucket];
		final int r = (int) (rSum[bestBucket] / n);
		final int g = (int) (gSum[bestBucket] / n);
		final int b = (int) (bSum[bestBucket] / n);

		return boost(new Color(r, g, b));
	}

	private static Color boost(Color c)
	{
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		hsb[1] = Math.min(1.0f, hsb[1] * 1.15f);
		hsb[2] = Math.min(1.0f, Math.max(0.4f, hsb[2]));
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}
}
