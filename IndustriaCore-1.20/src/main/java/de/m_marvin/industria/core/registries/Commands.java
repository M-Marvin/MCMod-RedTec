package de.m_marvin.industria.core.registries;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.conduits.engine.command.SetConduitCommand;
import de.m_marvin.industria.core.contraptions.engine.commands.ContraptionCommand;
import de.m_marvin.industria.core.contraptions.engine.commands.arguments.contraption.ContraptionSelectorOptions;
import de.m_marvin.industria.core.util.commands.DebugCommand;
import de.m_marvin.industria.core.util.commands.FixAttachmentsCommand;
import de.m_marvin.industria.core.util.commands.TemplateCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=IndustriaCore.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class Commands {
	
	static {
		ContraptionSelectorOptions.bootStrap();
	}
	
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		SetConduitCommand.register(event.getDispatcher());
		ContraptionCommand.register(event.getDispatcher());
		TemplateCommand.register(event.getDispatcher());
		FixAttachmentsCommand.register(event.getDispatcher());
		DebugCommand.register(event.getDispatcher());
	}
	
}
