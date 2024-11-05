package de.m_marvin.animator;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.m_marvin.commandlineparser.CommandLineParser;
import de.m_marvin.univec.impl.Vec2f;
import de.m_marvin.univec.impl.Vec2i;

public class AnimationUtility {
	
	public static void main(String[] args) {
		
		CommandLineParser parser = new CommandLineParser();
		parser.addOption("gen_random", "", "");
		
		parser.addOption("width", "16", "");
		parser.addOption("height", "16", "");
		parser.addOption("image_folder", "", "");
		parser.addOption("help", false, "Show help info");
		
		parser.parseInput(args);
		
		if (parser.getFlag("help")) {
			System.out.println(parser.printHelp());
			return;
		}
		
		if (!parser.getOption("gen_random").isEmpty()) {
			System.out.println("Run gen random list ..." + parser.getOption("gen_item_list"));
			genRandom(new File(parser.getOption("image_folder")), new Vec2i(Integer.parseInt(parser.getOption("width")), Integer.parseInt(parser.getOption("height"))));
		}
		
	}
	
	public static void genRandom(File imageFolder, Vec2i size) {
		
		Animator an = new Animator(size);
		
		try {

			an.addLayer(ImageIO.read(new File(imageFolder, "temp1.png")), 10, 0);

			an.addLayer(ImageIO.read(new File(imageFolder, "temp1.png")), 20, 0);
			
			ImageIO.write(an.printAnimation(10), "png", new File(imageFolder, "random.png"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
