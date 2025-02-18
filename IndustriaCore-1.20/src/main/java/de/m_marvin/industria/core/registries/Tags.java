package de.m_marvin.industria.core.registries;

import de.m_marvin.industria.IndustriaCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Tags {
	
	public static class Blocks {
		
		public static final TagKey<Block> MACHINERY = tag("machinery");
		public static final TagKey<Block> ELECTRICS = tag("electrics");
		public static final TagKey<Block> MAGNETIC = tag("magnetic");
		public static final TagKey<Block> KINETICS = tag("kinetics");
		public static final TagKey<Block> COMPOUNDABLE = tag("compoundable");
		public static final TagKey<Block> BELT_SHAFTS = tag("belt_shafts");
		
		private static TagKey<Block> tag(String name) {
			return BlockTags.create(new ResourceLocation(IndustriaCore.MODID, name));
		}
		
	}
	
	public static class Items {

		public static final TagKey<Item> CONDUITS = tag("conduits");
		public static final TagKey<Item> SCREW_DRIVERS = tag("screw_drivers");
		public static final TagKey<Item> CUTTERS = tag("cutters");
		
		private static TagKey<Item> tag(String name) {
			return ItemTags.create(new ResourceLocation(IndustriaCore.MODID, name));
		}
		
	}
	
}
