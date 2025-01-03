package de.m_marvin.industria.core.registries;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.electrics.types.blockentities.JunctionBoxBlockEntity;
import de.m_marvin.industria.core.electrics.types.blockentities.VoltageSourceBlockEntity;
import de.m_marvin.industria.core.kinetics.types.blockentities.SimpleKineticBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypes {

	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IndustriaCore.MODID);
	public static void register() {
		BLOCK_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	public static final RegistryObject<BlockEntityType<JunctionBoxBlockEntity>> JUNCTION_BOX = BLOCK_ENTITY_TYPES.register("junction_box", () -> BlockEntityType.Builder.of(JunctionBoxBlockEntity::new).build(null));
	public static final RegistryObject<BlockEntityType<VoltageSourceBlockEntity>> VOLTAGE_SOURCE = BLOCK_ENTITY_TYPES.register("voltage_source", () -> BlockEntityType.Builder.of(VoltageSourceBlockEntity::new, Blocks.VOLTAGE_SOURCE.get()).build(null));
	public static final RegistryObject<BlockEntityType<SimpleKineticBlockEntity>> SIMPLE_KINETIC = BLOCK_ENTITY_TYPES.register("simple_kinetic", () -> {
		return BlockEntityType.Builder.of(SimpleKineticBlockEntity::new, Blocks.GEAR.get()).build(null);
	});
	
}
