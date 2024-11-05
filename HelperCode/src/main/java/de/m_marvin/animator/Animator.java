package de.m_marvin.animator;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.m_marvin.univec.impl.Vec2f;
import de.m_marvin.univec.impl.Vec2i;

public class Animator {

	
	
	public Vec2i size;
	public Map<BufferedImage, Float> images = new HashMap<>();
	public Map<BufferedImage, Float> frameSteps = new HashMap<>();
	
	public Animator(Vec2i gridSize) {
		this.size = gridSize;
	}
	
	public void initRandom() {
//		for (int x = 0; x < size.x; x++) {
//			for (int y = 0; y < size.y; y++) {
//				this.grid[x][y] = this.rand.nextInt();
//			}
//		}
	}
	
	public void addLayer(BufferedImage image, float frameSteps, float startPos) {
		BufferedImage image2 = new BufferedImage(image.getWidth(), image.getHeight() * 2, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		image2.setRGB(0, 0, image.getWidth(), image.getHeight(), rgb, 0, image.getWidth());
		image2.setRGB(0, image.getHeight(), image.getWidth(), image.getHeight(), rgb, 0, image.getWidth());
		
		this.images.put(image2, startPos);
		this.frameSteps.put(image2, frameSteps);
	}
	
	public static int interpol(int a, int b, float f) {
		return Math.round(a + (b - a) * f);
	}

	public static int combine(int a, int b) {
		return Math.round((a + b) / 2F);
	}
	
	public void printImage(BufferedImage image, int ox, int oy) {
		
		int[] pixelData = new int[size.x * size.y];
		for (BufferedImage img : this.images.keySet()) {
			
			float position = this.images.get(img);
			int gridy = (int) Math.floor(position);
			float interpol = Math.clamp(position - gridy, 0F, 1F);
			
			int[] rgb = img.getRGB(0, gridy, size.x, (size.y + 1) % img.getHeight(), null, 0, size.x);
			
			for (int i = 0; i < pixelData.length; i++) {
				
				int color1 = rgb[i];
				int color2 = rgb[i + size.x];
				
				int color = 
						(interpol((color1 >> 24) & 0xFF, (color2 >> 24) & 0xFF, interpol) << 24) |
						(interpol((color1 >> 16) & 0xFF, (color2 >> 16) & 0xFF, interpol) << 16) |
						(interpol((color1 >> 8) & 0xFF, (color2 >> 8) & 0xFF, interpol) << 8) |
						(interpol((color1 >> 0) & 0xFF, (color2 >> 0) & 0xFF, interpol) << 0);

				pixelData[i] = //color;
						(combine((color >> 24) & 0xFF, (pixelData[i] >> 24) & 0xFF) << 24) |
						(combine((color >> 16) & 0xFF, (pixelData[i] >> 16) & 0xFF) << 16) |
						(combine((color >> 8) & 0xFF, (pixelData[i] >> 8) & 0xFF) << 8) |
						(combine((color >> 0) & 0xFF, (pixelData[i] >> 0) & 0xFF) << 0);
				
			}
			
		}
		
		image.setRGB(ox, oy, size.x, size.y, pixelData, 0, size.x);
		
	}

	public void stepFlow(Vec2f direction) {
		
		for (BufferedImage img : this.frameSteps.keySet()) {
			this.images.put(img, this.images.get(img) + this.frameSteps.get(img));
		}
		
//		for (int x = 0; x < size.x; x++) {
//			for (int y = 0; y < size.y; y++) {
//				
//				float factor = ((this.grid[x][y] >> 24) & 0xFF) / 255F;
//				Vec2f newPos = new Vec2f((float) x, (float) y).addI(direction.mul(factor));
//				grid2[Math.round(newPos.x) % this.size.x][Math.round(newPos.y) % this.size.y] = grid[x][y];
//				
//			}
//		}
//		
//		this.grid = grid2;
	}
	
	
	public BufferedImage printAnimation(int frames) {
		
		BufferedImage map = new BufferedImage(size.x, size.y * frames, BufferedImage.TYPE_INT_ARGB);
		
		for (int i = 0; i < frames; i++) {
			printImage(map, 0, size.y * i);
			stepFlow(new Vec2f(0, 1F));
		}
		
		return map;
		
	}
	
}
