package de.m_marvin.industria.core.kinetics.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.electrics.engine.ElectricHandlerCapability.Component;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import de.m_marvin.industria.core.registries.Capabilities;
import de.m_marvin.industria.core.util.GameUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
			
			handler.makeNetwork(event.getPos());
			
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
	
	// TODO DEBUGGING
	public void makeNetwork(BlockPos startPos) {
		
		System.out.println("TEST" + this.level.isClientSide);
		
		List<BlockPos> tnd = new ArrayList<>();
		
		Queue<BlockPos> neighbors = new ArrayDeque<>();
		neighbors.add(startPos);
		
		while (!neighbors.isEmpty()) {
			
			BlockPos pos1 = neighbors.poll();
			BlockState tstate1 = this.level.getBlockState(pos1);
			
			if (tstate1.getBlock() instanceof IKineticBlock kinetic1) {
				
				// TODO
				tnd.add(pos1);
				
				TransmissionNode[] nodes1 = kinetic1.getTransmitionNodes(level, pos1, tstate1);
				
				for (TransmissionNode node1 : nodes1) {
					
					BlockPos tpos1 = node1.pos();
					
					System.out.println("Node at: " + tpos1 + " type " + node1.type());
					
					for (BlockPos tpos2 : node1.type().pos(node1)) {
						
						BlockState tstate2 = this.level.getBlockState(tpos2);
						
						if (tstate2.getBlock() instanceof IKineticBlock kinetic2) {
							
							TransmissionNode[] nodes2 = kinetic2.getTransmitionNodes(level, tpos2, tstate2);
							
							for (TransmissionNode node2 : nodes2) {
								
								if (!node2.pos().equals(tpos2)) continue;
								
								double transmission = node1.type().apply(node1, node2);
								if (transmission == 0.0) continue;
								
								System.out.println("Transmission to node at: " + tpos2 + " type " + node2.type() + " with ratio " + transmission);
								
								neighbors.add(tpos2);
								break;
								
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
}
