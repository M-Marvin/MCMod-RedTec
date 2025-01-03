package de.m_marvin.industria.core.kinetics.engine;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.electrics.engine.ElectricHandlerCapability.Component;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.registries.Capabilities;
import de.m_marvin.industria.core.util.GameUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=IndustriaCore.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class KineticHandlerCapabillity implements ICapabilitySerializable<ListTag> {
	
	public static final String CIRCUIT_FILE_NAME = "circuit_";
	public static final String CIRCUIT_FILE_EXTENSION = ".net";
	public static final String DATALIST_FILE_EXTENSION = ".dat";
	
	/* Capability handling */
	
	private LazyOptional<KineticHandlerCapabillity> holder = LazyOptional.of(() -> this);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == Capabilities.KINETIC_HANDLER_CAPABILITY) {
			return holder.cast();
		}
		return LazyOptional.empty();
	}
	
	private final Level level;
	
	public Level getLevel() {
		return level;
	}
	
	@Override
	public ListTag serializeNBT() {
		ListTag networksNbt = new ListTag();

		return networksNbt;
	}
	
	@Override
	public void deserializeNBT(ListTag nbt) {
		
	}
	
	public KineticHandlerCapabillity(Level level) {
		this.level = level;
	}
	
	/* Event handling */
	
	@SubscribeEvent
	public static void onBlockStateChange(BlockEvent.NeighborNotifyEvent event) {
		Level level = (Level) event.getLevel();
		KineticHandlerCapabillity handler = GameUtility.getLevelCapability(level, Capabilities.KINETIC_HANDLER_CAPABILITY);
		
		if (event.getState().getBlock() instanceof IKineticBlock kinetic) {
//			if (handler.isInNetwork(event.getPos())) {
//				Component<Object, BlockPos, Object> component = handler.getComponentAt(event.getPos());
//				if (component.instance(level).equals(event.getState())) return; // No real update, ignore
//				handler.addToNetwork(component); // The component is already added to the network at this point, this call just ensures that the node maps are up to date
//				component.setChanged();
//				handler.updateNetwork(component.pos());
//			} else {
//				IElectricBlock block = (IElectricBlock) event.getState().getBlock();
//				handler.addComponent(event.getPos(), block, event.getState());
//			}
		} else {
//			handler.removeComponent(event.getPos());
		}
		
	}
	
	@SubscribeEvent
	public static void onClientLoadsChunk(ChunkWatchEvent.Watch event) {
		Level level = event.getPlayer().level();
		KineticHandlerCapabillity handler = GameUtility.getLevelCapability(level, Capabilities.KINETIC_HANDLER_CAPABILITY);
		
	}
	
	@SubscribeEvent
	public static void onClientUnloadsChunk(ChunkWatchEvent.UnWatch event) {
		Level level = event.getPlayer().level();
		KineticHandlerCapabillity handler = GameUtility.getLevelCapability(level, Capabilities.KINETIC_HANDLER_CAPABILITY);
		
	}

	/* Kinetic handling */
	
}
