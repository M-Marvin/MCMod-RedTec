package de.m_marvin.industria.core.kinetics.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.Queue;
import java.util.Set;
import java.util.stream.DoubleStream;

import com.google.common.base.Objects;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.kinetics.engine.network.SSyncKineticComponentsPackage;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import de.m_marvin.industria.core.registries.Capabilities;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.industria.core.util.types.PowerNetState;
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
	private final HashSet<KineticNetwork> kineticNetworks = new HashSet<KineticNetwork>();
	private final HashMap<Component, KineticNetwork> component2kineticMap = new HashMap<Component, KineticNetwork>();
	
	public Level getLevel() {
		return level;
	}
	
	@Override
	public ListTag serializeNBT() {
		ListTag networksNbt = new ListTag();
		
		int componentCount = 0;
		for (KineticNetwork kineticNetwork : this.kineticNetworks) {
			kineticNetwork.removeInvalidComponents();
			if (kineticNetwork.isEmpty()) continue;
			networksNbt.add(kineticNetwork.saveNBT(this));
			componentCount += kineticNetwork.getComponents().size();
		}
		
		IndustriaCore.LOGGER.info("Saved " + networksNbt.size() + " kinetic networks");
		IndustriaCore.LOGGER.info("Saved " + componentCount + " kinetic components");
		return networksNbt;
	}
	
	@Override
	public void deserializeNBT(ListTag nbt) {
		this.pos2componentMap.clear();
		this.kineticNetworks.clear();
		this.component2kineticMap.clear();
		
		for (int i = 0; i < nbt.size(); i++) {
			CompoundTag kineticTag = nbt.getCompound(i);
			KineticNetwork kineticNetwork = new KineticNetwork(() -> this.level);
			kineticNetwork.loadNBT(this, kineticTag);
			if (!kineticNetwork.isEmpty()) {
				this.kineticNetworks.add(kineticNetwork);
				kineticNetwork.getComponents().forEach((component) -> {
					this.component2kineticMap.put(component, kineticNetwork);
					if (!this.pos2componentMap.containsValue(component)) {
						this.addToNetwork(component);
					}
				});
			}
		}
		
		IndustriaCore.LOGGER.info("Loaded " + this.kineticNetworks.size() + "/" + nbt.size() + " kinetic networks");
		IndustriaCore.LOGGER.info("Loaded " + this.pos2componentMap.size() + " kinetic components");
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
		} else {
			handler.removeComponent(event.getPos());
		}
	}
	
	@SubscribeEvent
	public static void onClientLoadsChunk(ChunkWatchEvent.Watch event) {
		Level level = event.getPlayer().level();
		KineticHandlerCapabillity kineticHandler = GameUtility.getLevelCapability(level, Capabilities.KINETIC_HANDLER_CAPABILITY);
		Set<Component> components = kineticHandler.findComponentsInChunk(event.getPos());
		
		if (!components.isEmpty()) {
			IndustriaCore.NETWORK.send(PacketDistributor.PLAYER.with(event::getPlayer), new SSyncKineticComponentsPackage(components, event.getChunk().getPos(), SyncRequestType.ADDED));
		}
	}
	
	@SubscribeEvent
	public static void onClientUnloadsChunk(ChunkWatchEvent.UnWatch event) {
		Level level = event.getPlayer().level();
		KineticHandlerCapabillity electricHandler = GameUtility.getLevelCapability(level, Capabilities.KINETIC_HANDLER_CAPABILITY);
		Set<Component> components = electricHandler.findComponentsInChunk(event.getPos());
		if (!components.isEmpty()) {
			IndustriaCore.NETWORK.send(PacketDistributor.PLAYER.with(event::getPlayer), new SSyncKineticComponentsPackage(components, event.getPos(), SyncRequestType.REMOVED));
		}
	}
	
	/* Kinetic handling */
	
	/**
	 * Represents a component in the kinetic networks
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
		
		public TransmissionNode[] getTransmissionNodes(Level level) {
			return this.type.getTransmissionNodes(level, pos, instance);
		}
		public int getSourceSpeed(Level level) {
			return this.type.getSourceSpeed(level, pos, instance);
		}
		public double getTorque(Level level) {
			return this.type.getTorque(level, pos, instance);
		}
		public void setRPM(Level level, int rpm) {
			this.type.setRPM(level, pos, instance, rpm);
		}
		public int getRPM(Level level) {
			return this.type.getRPM(level, pos, instance);
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
	 * Returns a set containing all components in the given chunk
	 */
	public Set<Component> findComponentsInChunk(ChunkPos chunkPos) {
		Set<Component> components = new HashSet<>();
		for (Entry<Object, Component> componentEntry : this.pos2componentMap.entrySet()) {
			if (new ChunkPos(componentEntry.getValue().pos()).equals(chunkPos)) components.add(componentEntry.getValue());
		}
		return components;
	}
	
	/**
	 * Updates the network which has a component at the given position.
	 * @return 
	 */
	public KineticNetwork updateNetwork(BlockPos position) {
		
		KineticNetwork network = makeNetwork(position);
		if (network == null) return null;

		System.out.println("Update at " + position);

		// Check for opposite rotations, if so, skip calculations
		if (network.isTripped()) {
			network.setNetworkSpeed(0.0);
			System.out.println("Locked!");
		} else {
			
			// Calculate source speeds
			double[] sources = network.getComponents().stream()
				.mapToDouble(c -> c.getSourceSpeed(this.level) * network.getTransmission(c))
				.distinct()
				.toArray();
			
			// Find fastest source-speed in network (in both directions)
			OptionalDouble maxSpeedH = DoubleStream.of(sources).filter(s -> s > 0).max();
			OptionalDouble maxSpeedL = DoubleStream.of(sources).filter(s -> s < 0).min();
			
			// Check if any source available
			if (maxSpeedH.isEmpty() && maxSpeedL.isEmpty()) {
				network.setNetworkSpeed(0.0);
				network.setState(PowerNetState.INACTIVE);
				System.out.println("No Sources!");
			} 

			// Check for reversed sources
			else if (maxSpeedH.isPresent() && maxSpeedL.isPresent()) {
				network.setNetworkSpeed(0.0);
				network.tripFuse();
				System.out.println("Reversed Sources!");
			} 
			
			else {
				
				double speed = maxSpeedL.orElseGet(() -> maxSpeedH.getAsDouble());
				
				// Calculate available torque
				double torque = network.components.stream()
					.filter(c -> c.getSourceSpeed(this.level) == 0)
					.mapToDouble(c -> c.getTorque(level) / network.getTransmission(c))
					.sum();
				
				// Calculate total load
				double load = network.components.stream()
						.filter(c -> c.getSourceSpeed(this.level) == 0)
						.mapToDouble(c -> c.getTorque(level) / network.getTransmission(c))
						.sum();
				
				// Check for overload
				if (load > torque) {
					network.setNetworkSpeed(0.0);
					network.tripFuse();
					System.out.println("Overload!");
				} 
				
				else {
					
					// Set network rotation speed
					network.setNetworkSpeed(speed);
					network.setState(PowerNetState.ACTIVE);
					System.out.println("Network Speed: " + network.getSpeed());
					System.out.println("Network Load: " + load + "/" + torque);
					
				}
				
			}
			
		}

		// TODO DEBUGGIN update component RPM
		for (Component c : network.getComponents()) {
			int cspeed = (int) Math.round(network.getSpeed() / network.getTransmission(c));
			c.setRPM(level, cspeed);
			GameUtility.triggerClientSync(level, c.pos());
		}
		
		return network;
		
	}

	/**
	 * Triggers the same update methods as when updating the network, without actually starting a new simulation.
	 */
	public void triggerUpdates(ElectricNetwork network) {
		// FIXME not sure if this is required
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
				if (!this.level.isClientSide) {
					ChunkPos chunkPos = new ChunkPos(component.pos());
					IndustriaCore.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunk(chunkPos.x, chunkPos.z)), new SSyncKineticComponentsPackage(component, chunkPos, SyncRequestType.REMOVED));
				}
				KineticNetwork network = this.component2kineticMap.remove(component);
				if (network != null) {
					Queue<Component> componentsToUpdate = new ArrayDeque<KineticHandlerCapabillity.Component>(network.getComponents());
					componentsToUpdate.forEach(this.component2kineticMap::remove);
					while (componentsToUpdate.size() > 0) {
						Component componentToUpdate = componentsToUpdate.poll();
						if (componentToUpdate == component) continue;
						KineticNetwork network2 = updateNetwork(componentToUpdate.pos());
						if (network2 != null)
							componentsToUpdate.removeAll(network2.getComponents());
					}
					this.kineticNetworks.remove(network);
				}
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
	}
	
	public KineticNetwork makeNetwork(BlockPos startPos) {
		
		KineticNetwork network = null;
		List<BlockPos> tnd = new ArrayList<>();
		Queue<BlockPos> neighbors = new ArrayDeque<>();
		neighbors.add(startPos);
		
		while (!neighbors.isEmpty()) {
			
			BlockPos pos1 = neighbors.poll();
			BlockState state1 = this.level.getBlockState(pos1);
			
			if (state1.getBlock() instanceof IKineticBlock kinetic1) {
				
				for (TransmissionNode node1 : kinetic1.getTransmissionNodes(level, pos1, state1)) {
					
					BlockPos tpos1 = node1.pos();
					
					tnd.add(tpos1);
					
					Component component1 = getComponentAt(pos1);
					if (component1 == null) {
						component1 = new Component(pos1, kinetic1, state1);
						addToNetwork(component1);
						
						ChunkPos chunkPos = new ChunkPos(component1.pos());
						IndustriaCore.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunk(chunkPos.x, chunkPos.z)), new SSyncKineticComponentsPackage(component1, chunkPos, SyncRequestType.ADDED));
					}
					
					if (network == null) {
						network = this.component2kineticMap.get(component1);
						if (network == null) {
							network = new KineticNetwork(() -> this.level);
							this.component2kineticMap.put(component1, network); 
						} else {
							network.reset();
						}
					} else {
						KineticNetwork previousNetwork = this.component2kineticMap.put(component1, network);
						if (previousNetwork != null && previousNetwork != network) {
							this.kineticNetworks.remove(previousNetwork);
						}
					}
					
					network.getComponents().add(component1);
					
					for (BlockPos tpos2 : node1.type().pos(node1)) {

						if (tnd.contains(tpos2)) continue;
						
						BlockState state2 = this.level.getBlockState(tpos2);
						
						if (state2.getBlock() instanceof IKineticBlock kinetic2) {
							
							for (TransmissionNode node2 : kinetic2.getTransmissionNodes(level, tpos2, state2)) {
								
								double transmission = node1.type().apply(node1, node2);
								if (transmission == 0.0) continue;

								BlockPos pos2 = node2.blockPos();

								Component component2 = getComponentAt(pos2);
								if (component2 == null) {
									component2 = new Component(pos2, kinetic2, state2);
									addToNetwork(component1);
								}
								
								if (!network.addTransmission(component1, component2, transmission))
									network.tripFuse();
								
								if (!neighbors.contains(pos2)) neighbors.add(pos2);
								break;
								
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		if (network != null)
			this.kineticNetworks.add(network);
		
		return network;
		
	}
	
}
