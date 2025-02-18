package de.m_marvin.industria.core.registries;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.conduits.engine.ConduitHandlerCapability;
import de.m_marvin.industria.core.contraptions.engine.ContraptionHandlerCapability;
import de.m_marvin.industria.core.electrics.engine.ElectricHandlerCapability;
import de.m_marvin.industria.core.kinetics.engine.KineticHandlerCapabillity;
import de.m_marvin.industria.core.magnetism.engine.MagnetismHandlerCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD,modid=IndustriaCore.MODID)
public class Capabilities {
	
	public static final Capability<ConduitHandlerCapability> CONDUIT_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<ConduitHandlerCapability>() {});
	public static final Capability<ElectricHandlerCapability> ELECTRIC_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<ElectricHandlerCapability>() {});
	public static final Capability<ContraptionHandlerCapability> CONTRAPTION_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<ContraptionHandlerCapability>() {});
	public static final Capability<MagnetismHandlerCapability> MAGNETISM_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<MagnetismHandlerCapability>() {});
	public static final Capability<KineticHandlerCapabillity> KINETIC_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<KineticHandlerCapabillity>() {});
	
	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(ConduitHandlerCapability.class);
	}

	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE,modid=IndustriaCore.MODID)
	public class Attachment {
		
		@SubscribeEvent
		public static void attachCapabilities(AttachCapabilitiesEvent<Level> event) {
			event.addCapability(new ResourceLocation(IndustriaCore.MODID, "conduits"), new ConduitHandlerCapability(event.getObject()));
			event.addCapability(new ResourceLocation(IndustriaCore.MODID, "electrics"), new ElectricHandlerCapability(event.getObject()));
			event.addCapability(new ResourceLocation(IndustriaCore.MODID, "contraption"), new ContraptionHandlerCapability(event.getObject()));
			event.addCapability(new ResourceLocation(IndustriaCore.MODID, "magnetism"), new MagnetismHandlerCapability(event.getObject()));
			event.addCapability(new ResourceLocation(IndustriaCore.MODID, "kinetics"), new KineticHandlerCapabillity(event.getObject()));
		}
		
	}
	
}
