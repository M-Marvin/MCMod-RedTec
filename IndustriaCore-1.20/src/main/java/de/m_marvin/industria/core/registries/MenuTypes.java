package de.m_marvin.industria.core.registries;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.electrics.types.blockentities.JunctionBoxBlockEntity;
import de.m_marvin.industria.core.electrics.types.containers.JunctionBoxContainer;
import de.m_marvin.industria.core.electrics.types.containers.VoltageSourceContainer;
import de.m_marvin.industria.core.kinetics.types.containers.MotorContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuTypes {

	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, IndustriaCore.MODID);
	public static void register() {
		MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	public static final RegistryObject<MenuType<JunctionBoxContainer<JunctionBoxBlockEntity>>> JUNCTION_BOX = 	MENU_TYPES.register("junction_box", () -> IForgeMenuType.create(JunctionBoxContainer::new));
	public static final RegistryObject<MenuType<VoltageSourceContainer>> VOLTAGE_SOURCE = 						MENU_TYPES.register("voltage_source", () -> IForgeMenuType.create(VoltageSourceContainer::new));
	public static final RegistryObject<MenuType<MotorContainer>> MOTOR = 										MENU_TYPES.register("motor", () -> IForgeMenuType.create(MotorContainer::new));
	
}
