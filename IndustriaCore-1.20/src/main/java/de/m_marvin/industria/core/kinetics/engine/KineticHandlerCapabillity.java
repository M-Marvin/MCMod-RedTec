package de.m_marvin.industria.core.kinetics.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.common.base.Objects;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.client.electrics.events.ElectricNetworkEvent;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.ElectricUtility;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.electrics.engine.ElectricHandlerCapability.Component;
import de.m_marvin.industria.core.electrics.engine.network.SSyncElectricComponentsPackage;
import de.m_marvin.industria.core.electrics.engine.network.SUpdateElectricNetworkPackage;
import de.m_marvin.industria.core.electrics.types.IElectric;
import de.m_marvin.industria.core.electrics.types.IElectric.ICircuitPlot;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricBlock;
import de.m_marvin.industria.core.kinetics.engine.network.SSyncKineticComponentsPackage;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import de.m_marvin.industria.core.registries.Capabilities;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.industria.core.util.NBTUtility;
import de.m_marvin.industria.core.util.types.SyncRequestType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=IndustriaCore.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class KineticHandlerCapabillity implements ICapabilitySerializable<ListTag> {
	
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
	private final HashMap<Object, Component> pos2componentMap = new HashMap<Object, Component>();
//	private final HashMap<NodePos, Set<Component<?, ?, ?>>> node2componentMap = new HashMap<NodePos, Set<Component<?, ?, ?>>>();
	private final HashSet<KineticNetwork> kineticNetworks = new HashSet<KineticNetwork>();
	private final HashMap<Component, KineticNetwork> component2kineticMap = new HashMap<Component, KineticNetwork>();
	
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
			if (handler.isInNetwork(event.getPos())) {
				Component component = handler.getComponentAt(event.getPos());
				if (component.instance(level).equals(event.getState())) return; // No real update, ignore
				handler.addToNetwork(component); // The component is already added to the network at this point, this call just ensures that the node maps are up to date
				component.setChanged();
				handler.updateNetwork(component.pos());
			} else {
				handler.addComponent(event.getPos(), kinetic, event.getState());
			}
			
//			handler.makeNetwork(event.getPos());
		} else {
			handler.removeComponent(event.getPos());
			
			
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
	
	/**
	 * Represents a component (can be a conduit or a block) in the electric networks
	 */
	public static class Component {
		protected BlockPos pos;
		protected boolean hasChanged;
		protected BlockState instance;
		protected IKineticBlock type;
		
		public Component(BlockPos pos, IKineticBlock type, BlockState instance) {
			this.type = type;
			this.instance = instance;
			this.pos = pos;
			this.hasChanged = true;
		}
		
		public void setChanged() {
			this.hasChanged = true;
		}
		
		public BlockPos pos() {
			return pos;
		}
		
		public IKineticBlock type() {
			return type;
		}
		
		public BlockState instance(Level level) {
			if ((this.hasChanged || instance.isAir()) && level != null) {
				this.instance = level.getBlockState(pos);
				if (!this.instance.isAir()) {
					this.hasChanged = false;
				}
			}
			return instance;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(this.type, this.pos);
		}
		
		@Override
		public String toString() {
			return "Component{pos=" + this.pos() + ",type=" + this.type.toString() + ",instance=" + (this.instance(null) == null ? "N/A" : this.instance(null).toString()) + "}#hash=" + this.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj instanceof Component other) {
				return this.type.equals(other.type) && this.pos.equals(other.pos);
			}
			return false;
		}
		
		public void serializeNbt(CompoundTag nbt) {
			nbt.put("Position", NbtUtils.writeBlockPos(pos));
			if (this.type instanceof Block typeBlock) nbt.putString("Type", ForgeRegistries.BLOCKS.getKey(typeBlock).toString());
			nbt.put("State", NbtUtils.writeBlockState(instance));
		}
		public static Component deserializeNbt(CompoundTag nbt) {
			ResourceLocation typeName = new ResourceLocation(nbt.getString("Type"));
			Block typeObject = ForgeRegistries.BLOCKS.getValue(typeName);
			if (typeObject instanceof IKineticBlock type) {
				BlockPos position = NbtUtils.readBlockPos(nbt.getCompound("Position"));
				@SuppressWarnings("deprecation")
				BlockState instance = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("State"));
				return new Component(position, type, instance);
			}
			return null;
		}
	}

	/**
	 * Returns the component with the given position
	 */
	public Component getComponentAt(BlockPos position) {
		return this.pos2componentMap.get(position);
	}
	
	/**
	 * Returns the network an component at the given position
	 */
	public KineticNetwork getNetworkAt(BlockPos position) {
		Component component = getComponentAt(position);
		if (component == null) return null;
		return this.component2kineticMap.get(component);
	}
	
	/**
	 * Updates the network which has a component at the given position.
	 */
	public <P> void updateNetwork(P position) {
		
//		if (this.level.isClientSide) return;
//		
//		Component<?, ?, ?> component = this.pos2componentMap.get(position);
//		
//		if (component != null) {
//			
//			ElectricNetwork circuit = this.component2circuitMap.get(component);
//			
//			if (circuit == null || !circuit.getComponents().contains(component)) circuit = new ElectricNetwork(() -> this.level, "ingame-level-circuit");
//			
//			circuit.reset();
//			buildCircuit(component, circuit);
//			if (circuit.isEmpty()) return;
//			
//			if (!this.circuitNetworks.contains(circuit)) this.circuitNetworks.add(circuit);
//			
//			final ElectricNetwork circuitFinalized = circuit;
//			circuit.getComponents().forEach((comp) -> {
//				ElectricNetwork previousNetwork = this.component2circuitMap.put(comp, circuitFinalized);
//				if (previousNetwork != null && previousNetwork != circuitFinalized) {
//					previousNetwork.getComponents().remove(comp);
//					if (previousNetwork.getComponents().isEmpty()) {
//						this.circuitNetworks.remove(previousNetwork);
//					}
//				}
//			});
//			
//			getSimulationProcessor().processNetwork(circuit).thenAccept(state -> {
//				if (!state) {
//					circuitFinalized.tripFuse();
//					MinecraftForge.EVENT_BUS.post(new ElectricNetworkEvent.FuseTripedEvent(this.level, circuitFinalized));
//				}
//				triggerUpdates(circuitFinalized);
//			});
//			
//		}
		
	}

	/**
	 * Triggers the same update methods as when updating the network, without actually starting a new simulation.
	 */
	public void triggerUpdates(ElectricNetwork network) {
//		network.getComponents().forEach(c -> c.onNetworkChange(network.getLevel()));
//		IndustriaCore.NETWORK.send(ElectricUtility.TRACKING_NETWORK.with(() -> network), new SUpdateNetworkPackage(network));
	}
	
	/**
	 * Removes a component from the network and updates it and its components
	 */
	public void removeComponent(BlockPos pos) {
		if (this.pos2componentMap.containsKey(pos)) {
			Component component = removeFromNetwork(pos);
			
			if (component != null) {
//				Set<Component> componentsToUpdate = new HashSet<Component>();
//				NodePos[] nodes = component.getNodes(level);
//				for (int i = 0; i < nodes.length; i++) {
//					Set<Component> components = this.node2componentMap.get(nodes[i]);
//					if (components != null && !components.isEmpty()) {
//						components.remove(component);
//						componentsToUpdate.add(components.stream().findAny().get());
//					}
//				}
//				if (componentsToUpdate.isEmpty()) {
//					ElectricNetwork emptyNetwork = this.component2circuitMap.remove(component);
//					if (emptyNetwork != null) {
//						this.circuitNetworks.remove(emptyNetwork);
//					}
//				}
//				if (!this.level.isClientSide) {
//					ChunkPos chunkPos = component.getAffectedChunk(level);
//					IndustriaCore.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunk(chunkPos.x, chunkPos.z)), new SSyncComponentsPackage(component, chunkPos, SyncRequestType.REMOVED));
//				}
//				for (Component comp : componentsToUpdate) {
//					updateNetwork(comp.pos());
//				}
//				component.onNetworkChange(level);
			}
		}
	}
	
	/**
	 * Adds a component to the network and updates it and its components
	 */
	public void addComponent(BlockPos pos, IKineticBlock type, BlockState instance) {
		
		Component component = this.pos2componentMap.get(pos);
		if (component != null) {
			if (!component.type.equals(type)) {
				removeFromNetwork(pos);
			}
		}
		Component component2 = new Component(pos, type, instance);
		addToNetwork(component2);
		if (!this.level.isClientSide) {
			ChunkPos chunkPos = new ChunkPos(component2.pos());
			IndustriaCore.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunk(chunkPos.x, chunkPos.z)), new SSyncKineticComponentsPackage(component2, chunkPos, SyncRequestType.ADDED));
		}

		updateNetwork(pos);

	}
	
	/**
	 * Removes a component from the network but does not cause any updates
	 */
	public Component removeFromNetwork(BlockPos pos) {
		Component component = this.pos2componentMap.remove(pos);
		if (component != null) {
//			List<NodePos> empty = new ArrayList<>();
//			for (Entry<NodePos, Set<Component>> entry : this.node2componentMap.entrySet()) {
//				entry.getValue().remove(component);
//				if (entry.getValue().isEmpty()) empty.add(entry.getKey());
//			}
//			empty.forEach(c -> this.component2kineticMap.remove(c));
		}
		return component;
	}
	
	/**
	 * Returns true if the component is already registered for an network
	 */
	public boolean isInNetwork(Component component) {
		if (component.instance(level) == null) return false;
		return this.component2kineticMap.containsKey(component) && this.pos2componentMap.containsKey(component.pos());
	}

	/**
	 * Returns true if the component at the given position is already registered for an network
	 */
	public boolean isInNetwork(Object pos) {
		if (!this.pos2componentMap.containsKey(pos)) return false;
		return this.component2kineticMap.containsKey(this.pos2componentMap.get(pos));
	}
	
	/**
	 * Returns the internal collection of all electric components
	 */
	public Collection<Component> getComponents() {
		return this.pos2componentMap.values();
	}
	
	/**
	 * Adds a component to the network but does not cause any updates
	 */
	public void addToNetwork(Component component) {
		if (component.instance(level) == null) return;
		this.pos2componentMap.put(component.pos, component);
//		for (NodePos node : component.type().getConnections(this.level, component.pos, component.instance(level))) {
//			Set<Component> componentSet = this.node2componentMap.getOrDefault(node, new HashSet<Component>>());
//			componentSet.add(component);
//			this.node2componentMap.put(node, componentSet);
//		}
	}
	
//	/**
//	 * Builds a ngspice netlist for the given network beginning from the given component
//	 */
//	private void buildCircuit(Component component, ElectricNetwork circuit) {
//		buildCircuit0(component, null, circuit);
//		circuit.complete(this.level.getGameTime());
//	}
//	private void buildCircuit0(Component<?, ?, ?> component, NodePos node, ElectricNetwork circuit) {
//		
//		if (circuit.getComponents().contains(component)) return;
//		
//		circuit.getComponents().add(component);
//		circuit.plotComponentDescriptor(component);
//		component.plotCircuit(level, circuit, template -> circuit.plotTemplate(component, template));
//		
//		for (NodePos node2 : component.getNodes(level)) {
//			if (node2.equals(node) || this.node2componentMap.get(node2) == null) continue; 
//			for (Component<?, ?, ?> component2 : this.node2componentMap.get(node2)) {
//				buildCircuit0(component2, node2, circuit);
//			}
//		}
//		
//	}
	
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
				
				for (TransmissionNode node1 : kinetic1.getTransmitionNodes(level, pos1, tstate1)) {
					
					BlockPos tpos1 = node1.pos();
					
					tnd.add(tpos1);
					
					System.out.println("Node at: " + tpos1 + " type " + node1.type());
					
					for (BlockPos tpos2 : node1.type().pos(node1)) {

						if (tnd.contains(tpos2)) continue;
						
						BlockState tstate2 = this.level.getBlockState(tpos2);
						
						if (tstate2.getBlock() instanceof IKineticBlock kinetic2) {
							
							for (TransmissionNode node2 : kinetic2.getTransmitionNodes(level, tpos2, tstate2)) {
								
								double transmission = node1.type().apply(node1, node2);
								if (transmission == 0.0) continue;

								BlockPos pos2 = node2.blockPos();
								
								
								
								System.out.println("Transmission to node at: " + tpos2 + " with ratio " + transmission);
								
								neighbors.add(pos2);
								break;
								
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
}
