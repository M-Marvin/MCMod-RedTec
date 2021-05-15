package de.industria.typeregistys;

import de.industria.Industria;
import de.industria.ModItems;
import de.industria.blocks.BlockJigsaw.JigsawType;
import de.industria.worldgen.JigsawFeatureConfig;
import de.industria.worldgen.StoneOreFeatureConfig;
import de.industria.worldgen.placements.HorizontalSpreadPlacementConfig;
import de.industria.worldgen.placements.SimpleOrePlacementConfig;
import de.industria.worldgen.placements.VerticalOffsetPlacementConfig;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.template.TagMatchRuleTest;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModConfiguredFeatures {
	
	// Ores
	public static final ConfiguredFeature<?, ?> COPPER_ORE = registerConfiguredFeature("copper_ore", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, 
							ModItems.copper_ore.getDefaultState(),
							14
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(11, 45, 14)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> NICKEL_ORE = registerConfiguredFeature("nickel_ore", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, 
							ModItems.nickel_ore.getDefaultState(),
							8
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(0, 64, 6)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> TIN_ORE = registerConfiguredFeature("tin_ore", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, 
							ModItems.tin_ore.getDefaultState(),
							12
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(0, 48, 6)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> TIN_ORE_EXTRA = registerConfiguredFeature("tin_ore_extra", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, 
							ModItems.tin_ore.getDefaultState(),
							12
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(0, 48, 6)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> SILVER_ORE = registerConfiguredFeature("silver_ore", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, 
							ModItems.silver_ore.getDefaultState(),
							6
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(16, 40, 1)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> SILVER_ORE_EXTRA = registerConfiguredFeature("silver_ore_extra", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, 
							ModItems.silver_ore.getDefaultState(),
							3
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(16, 40, 1)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> PALLADIUM_ORE = registerConfiguredFeature("palladium_ore", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, 
							ModItems.palladium_ore.getDefaultState(),
							2
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(0, 16, 1)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> PALLADIUM_ORE_EXTRA = registerConfiguredFeature("palladium_ore_extra", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, 
							ModItems.palladium_ore.getDefaultState(),
							2
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(0, 16, 1)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> SULFUR_ORE = registerConfiguredFeature("sulfur_ore", 
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_NETHER, 
							ModItems.sulfur_ore.getDefaultState(),
							10
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(0, 64, 15)
					)
			)
	);
	
	public static final ConfiguredFeature<?, ?> BAUXIT_STONE_ORE = registerConfiguredFeature("bauxit_stone_ore",
			ModFeature.STONE_ORE.withConfiguration(
					new StoneOreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD,
							ModItems.bauxit.getDefaultState(),
							ModItems.bauxit_ore.getDefaultState(),
							42
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(20, 180, 5)
					)
			)
	);
	public static final ConfiguredFeature<?, ?> WOLFRAM_STONE_ORE = registerConfiguredFeature("wolfram_stone_ore",
			ModFeature.STONE_ORE.withConfiguration(
					new StoneOreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD,
							ModItems.wolframit.getDefaultState(),
							ModItems.wolframit_ore.getDefaultState(),
							42
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(0, 32, 1)
					)
			).withPlacement(
					Placement.CHANCE.configure(
							new ChanceConfig(5)
					)
			)
	);
	
	public static final ConfiguredFeature<?, ?> OIL_DEPOT = registerConfiguredFeature("oil_depot",
			Feature.ORE.withConfiguration(
					new OreFeatureConfig(
							OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD,
							ModFluids.RAW_OIL.getDefaultState().getBlockState(),
							128
					)
			).withPlacement(
					ModPlacement.SIMPLE_ORE.configure(
							new SimpleOrePlacementConfig(0, 30, 6)
					)
			).withPlacement(
					Placement.CHANCE.configure(
							new ChanceConfig(180)
					)
			)
	);
	
	// Trees
	public static final ConfiguredFeature<?, ?> RUBBER_TREE = registerConfiguredFeature("rubber_tree",
			ModFeature.JIGSAW_FEATURE.withConfiguration(new JigsawFeatureConfig(
					new TagMatchRuleTest(ModTags.DIRT), Direction.NORTH, JigsawType.VERTICAL_UP, 
					new ResourceLocation(Industria.MODID, "nature/rubber_tree"), new ResourceLocation(Industria.MODID, "tree_log"),
					Blocks.DIRT.getDefaultState(), false, 1, 1)
			).withPlacement(
					ModPlacement.VERTICAL_OFFSET.configure(
							new VerticalOffsetPlacementConfig(-1)
					)
			).withPlacement(
					ModPlacement.HORIZONTAL_SPREAD.configure(
							new HorizontalSpreadPlacementConfig(2)
					)
			).withPlacement(
					Placement.HEIGHTMAP_WORLD_SURFACE.configure(new NoPlacementConfig())
			).withPlacement(
					Placement.CHANCE.configure(new ChanceConfig(10))
			)
	);
	
	public static ConfiguredFeature<?, ?> registerConfiguredFeature(String key, ConfiguredFeature<?, ?> configuredFeature) {
		return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(Industria.MODID, key), configuredFeature);
	}
	
}
