package de.m_marvin.industria.core.registries;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.electrics.types.blocks.VoltageSourceBlock;
import de.m_marvin.industria.core.electrics.types.blocks.WireHolderBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.GearBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.LargeGearBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.MotorBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.ShaftBlock;
import de.m_marvin.industria.core.magnetism.types.blocks.MagnetBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Blocks {

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IndustriaCore.MODID);
	public static void register() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	public static final RegistryObject<Block> ERROR_BLOCK =		BLOCKS.register("error_block", () -> new Block(Properties.of().noParticlesOnBreak().mapColor(MapColor.COLOR_RED).strength(0F, 3600000F).noLootTable().noOcclusion()));
	public static final RegistryObject<Block> VOLTAGE_SOURCE = 	BLOCKS.register("voltage_source", () -> new VoltageSourceBlock(Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
	public static final RegistryObject<Block> MAGNET = 			BLOCKS.register("magnet", () -> new MagnetBlock(Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
	public static final RegistryObject<Block> WIRE_HOLDER =		BLOCKS.register("wire_holder", () -> new WireHolderBlock(Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
	public static final RegistryObject<Block> GEAR =			BLOCKS.register("gear", () -> new GearBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
	public static final RegistryObject<Block> LARGE_GEAR =		BLOCKS.register("large_gear", () -> new LargeGearBlock(Properties.of().mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
	public static final RegistryObject<Block> SHAFT = 			BLOCKS.register("shaft", () -> new ShaftBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
	public static final RegistryObject<Block> MOTOR = 			BLOCKS.register("motor", () -> new MotorBlock(Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
	
}
