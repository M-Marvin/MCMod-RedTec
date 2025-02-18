package de.m_marvin.industria.core.registries;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.conduits.engine.command.ConduitArgument;
import de.m_marvin.industria.core.contraptions.engine.commands.arguments.contraption.ContraptionArgument;
import de.m_marvin.industria.core.contraptions.engine.commands.arguments.vec3relative.Vec3RelativeArgument;
import de.m_marvin.industria.core.util.commands.MirroringArgument;
import de.m_marvin.industria.core.util.commands.RotationArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CommandArguments {

	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, IndustriaCore.MODID);
	public static void register() {
		COMMAND_ARGUMENT_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	public static final RegistryObject<SingletonArgumentInfo<Vec3RelativeArgument>> VEC3 = COMMAND_ARGUMENT_TYPES.register("vec3", () ->
		ArgumentTypeInfos.registerByClass(Vec3RelativeArgument.class, SingletonArgumentInfo.contextFree(Vec3RelativeArgument::vec3)));

	public static final RegistryObject<SingletonArgumentInfo<Vec3RelativeArgument>> VEC3_POSITION = COMMAND_ARGUMENT_TYPES.register("vec3_position", () ->
		ArgumentTypeInfos.registerByClass(Vec3RelativeArgument.class, SingletonArgumentInfo.contextFree(Vec3RelativeArgument::vec3position)));
	
	public static final RegistryObject<SingletonArgumentInfo<ContraptionArgument>> CONTRAPTION_ID_ARGUMENT_MULTIPLE = COMMAND_ARGUMENT_TYPES.register("contraption_multiple", () ->
		ArgumentTypeInfos.registerByClass(ContraptionArgument.class, SingletonArgumentInfo.contextFree(ContraptionArgument::contraptions)));
	
	public static final RegistryObject<SingletonArgumentInfo<ConduitArgument>> CONDUIT_ARGUMENT = COMMAND_ARGUMENT_TYPES.register("conduit", () ->
		ArgumentTypeInfos.registerByClass(ConduitArgument.class, SingletonArgumentInfo.contextFree(ConduitArgument::conduit)));
	
	public static final RegistryObject<SingletonArgumentInfo<RotationArgument>> BLOCK_ROTATION_ARGUMENT = COMMAND_ARGUMENT_TYPES.register("block_rotation", () ->
		ArgumentTypeInfos.registerByClass(RotationArgument.class, SingletonArgumentInfo.contextFree(RotationArgument::rotation)));

	public static final RegistryObject<SingletonArgumentInfo<MirroringArgument>> BLOCK_MIRROR_ARGUMENT = COMMAND_ARGUMENT_TYPES.register("block_mirror", () ->
		ArgumentTypeInfos.registerByClass(MirroringArgument.class, SingletonArgumentInfo.contextFree(MirroringArgument::mirroring)));
	
}
