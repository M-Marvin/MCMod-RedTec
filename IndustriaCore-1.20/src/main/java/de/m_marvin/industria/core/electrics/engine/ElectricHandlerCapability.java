package de.m_marvin.industria.core.electrics.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.base.Objects;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.Config;
import de.m_marvin.industria.core.client.electrics.events.ElectricNetworkEvent;
import de.m_marvin.industria.core.conduits.events.ConduitEvent;
import de.m_marvin.industria.core.conduits.events.ConduitEvent.ConduitBreakEvent;
import de.m_marvin.industria.core.conduits.events.ConduitEvent.ConduitPlaceEvent;
import de.m_marvin.industria.core.conduits.types.ConduitPos;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.ElectricUtility;
import de.m_marvin.industria.core.electrics.engine.network.SSyncElectricComponentsPackage;
import de.m_marvin.industria.core.electrics.engine.network.SUpdateElectricNetworkPackage;
import de.m_marvin.industria.core.electrics.types.IElectric;
import de.m_marvin.industria.core.electrics.types.IElectric.ICircuitPlot;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricBlock;
import de.m_marvin.industria.core.electrics.types.conduits.IElectricConduit;
import de.m_marvin.industria.core.registries.Capabilities;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.industria.core.util.types.PowerNetState;
import de.m_marvin.industria.core.util.types.SyncRequestType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid=IndustriaCore.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ElectricHandlerCapability implements ICapabilitySerializable<ListTag> {
	
	public static final String CIRCUIT_FILE_NAME = "circuit_";
	public static final String CIRCUIT_FILE_EXTENSION = ".net";
	public static final String DATALIST_FILE_EXTENSION = ".dat";
	
	/* Capability handling */
	
	private LazyOptional<ElectricHandlerCapability> holder = LazyOptional.of(() -> this);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == Capabilities.ELECTRIC_HANDLER_CAPABILITY) {
			return holder.cast();
		}
		return LazyOptional.empty();
	}
	
	private static SimulationProcessor simulationProcessor;
	
	private final Level level;
	private final HashMap<Object, Component<?, ?, ?>> pos2componentMap = new HashMap<Object, Component<?, ?, ?>>();
	private final HashMap<NodePos, Set<Component<?, ?, ?>>> node2componentMap = new HashMap<NodePos, Set<Component<?, ?, ?>>>();
	private final HashSet<ElectricNetwork> circuitNetworks = new HashSet<ElectricNetwork>();
	private final HashMap<Component<?, ?, ?>, ElectricNetwork> component2circuitMap = new HashMap<Component<?, ?, ?>, ElectricNetwork>();
	private int circuitFileCounter;
	
	public Level getLevel() {
		return level;
	}
	
	@Override
	public ListTag serializeNBT() {
		this.circuitFileCounter = 0;
		
		ListTag networksNbt = new ListTag();
		int componentCount = 0;
		for (ElectricNetwork circuitNetwork : this.circuitNetworks) {
			circuitNetwork.removeInvalidComponents();
			if (circuitNetwork.isEmpty()) continue;
			networksNbt.add(circuitNetwork.saveNBT(this));
			componentCount += circuitNetwork.getComponents().size();
		}
		cleanupUnusedCircuitFiles();
		
		IndustriaCore.LOGGER.info("Saved " + networksNbt.size() + " electric networks");
		IndustriaCore.LOGGER.info("Saved " + componentCount + " electric components");
		return networksNbt;
	}
	
	@Override
	public void deserializeNBT(ListTag nbt) {
		this.pos2componentMap.clear();
		this.node2componentMap.clear();
		this.circuitNetworks.clear();
		this.component2circuitMap.clear();
		
		for (int i = 0; i < nbt.size(); i++) {
			CompoundTag circuitTag = nbt.getCompound(i);
			ElectricNetwork circuitNetwork = new ElectricNetwork(() -> this.level, "ingame-level-circuit");
			circuitNetwork.loadNBT(this, circuitTag);
			if (!circuitNetwork.isEmpty()) {
				this.circuitNetworks.add(circuitNetwork);
				circuitNetwork.getComponents().forEach((component) -> {
					this.component2circuitMap.put(component, circuitNetwork);
					if (!this.pos2componentMap.containsValue(component)) {
						this.addToNetwork(component);
					}
				});
			}
		}
		
		IndustriaCore.LOGGER.info("Loaded " + this.circuitNetworks.size() + "/" + nbt.size() + " electric networks");
		IndustriaCore.LOGGER.info("Loaded " + this.pos2componentMap.size() + " electric components");
	}
	
	public String saveCircuit(String netList, String dataList) {
		if (this.level instanceof ServerLevel serverLevel) {
			String circuit = CIRCUIT_FILE_NAME + circuitFileCounter++;
			File fileNetList = new File(serverLevel.getDataStorage().dataFolder, circuit + CIRCUIT_FILE_EXTENSION);
			File fileDatList = new File(serverLevel.getDataStorage().dataFolder, circuit + DATALIST_FILE_EXTENSION);
			
			try {
				OutputStream outputStream = new FileOutputStream(fileNetList);
				outputStream.write(netList.getBytes());
				outputStream.close();
			} catch (IOException e) {
				IndustriaCore.LOGGER.error("Could not save circuit net '" + circuit + "' to file!");
				e.printStackTrace();
			}
			
			try {
				OutputStream outputStream = new FileOutputStream(fileDatList);
				outputStream.write(dataList.getBytes());
				outputStream.close();
			} catch (IOException e) {
				IndustriaCore.LOGGER.error("Could not save data list '" + circuit + "' to file!");
				e.printStackTrace();
			}
			return circuit;
		}
		return null;
	}
	
	public String[] loadCircuit(String circuit) {
		if (this.level instanceof ServerLevel serverLevel) {
			File fileNetList = new File(serverLevel.getDataStorage().dataFolder, circuit + CIRCUIT_FILE_EXTENSION);
			File fileDatList = new File(serverLevel.getDataStorage().dataFolder, circuit + DATALIST_FILE_EXTENSION);
			
			String netList = "";
			String dataList = "";
			try {
				InputStream inputStream = new FileInputStream(fileNetList);
				netList = new String(inputStream.readAllBytes());
				inputStream.close();
			} catch (IOException e) {
				IndustriaCore.LOGGER.error("Could not load circuit net '" + circuit + "' from file!");
				e.printStackTrace();
			}
			try {
				InputStream inputStream = new FileInputStream(fileDatList);
				dataList = new String(inputStream.readAllBytes());
				inputStream.close();
			} catch (IOException e) {
				IndustriaCore.LOGGER.error("Could not load data list '" + circuit + "' from file!");
				e.printStackTrace();
			}
			return new String[] {netList, dataList};
		}
		return null;
	}

	public void cleanupUnusedCircuitFiles() {
		File dataFolder = ((ServerLevel) this.level).getDataStorage().dataFolder;
		File circuitFile;
		int counter = this.circuitFileCounter;
		while ((circuitFile = new File(dataFolder, CIRCUIT_FILE_NAME + counter++ + CIRCUIT_FILE_EXTENSION)).isFile()) {
			try {
				Files.delete(circuitFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		counter = this.circuitFileCounter;
		while ((circuitFile = new File(dataFolder, CIRCUIT_FILE_NAME + counter++ + DATALIST_FILE_EXTENSION)).isFile()) {
			try {
				Files.delete(circuitFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ElectricHandlerCapability(Level level) {
		this.level = level;
	}
	
	/* Event handling */
	
	@SubscribeEvent
	public static void onBlockStateChange(BlockEvent.NeighborNotifyEvent event) {
		Level level = (Level) event.getLevel();
		ElectricHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.ELECTRIC_HANDLER_CAPABILITY);
		
		if (event.getState().getBlock() instanceof IElectricBlock electric && electric.getConnectorMasterPos(level, event.getPos(), event.getState()).equals(event.getPos())) {
			if (handler.isInNetwork(event.getPos())) {
				Component<Object, BlockPos, Object> component = handler.getComponentAt(event.getPos());
				if (component.instance(level).equals(event.getState())) return; // No real update, ignore
				handler.addToNetwork(component); // The component is already added to the network at this point, this call just ensures that the node maps are up to date
				component.setChanged();
				handler.updateNetwork(component.pos());
			} else {
				handler.addComponent(event.getPos(), electric, event.getState());
			}
		} else {
			handler.removeComponent(event.getPos());
		}
	}
	
	@SubscribeEvent
	public static void onConduitStateChange(ConduitEvent event) {
		Level level = (Level) event.getLevel();
		ElectricHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.ELECTRIC_HANDLER_CAPABILITY);
		if (event.getConduitState().getConduit() instanceof IElectricConduit) {
			if (event instanceof ConduitPlaceEvent) {
				if (handler.isInNetwork(event.getPosition())) {
					Component<Object, ConduitPos, Object> component = handler.getComponentAt(event.getPosition());
					handler.addToNetwork(component); // The component is already added to the network at this point, this call just ensures that the node maps are up to date
					component.setChanged();
					handler.updateNetwork(component.pos());
				} else {
					IElectricConduit conduit = (IElectricConduit) event.getConduitState().getConduit();
					handler.addComponent(event.getPosition(), conduit, event.getConduitState());
				}
			} else if (event instanceof ConduitBreakEvent) {
				handler.removeComponent(event.getPosition());
			}
		}
	}
	
	@SubscribeEvent
	public static void onClientLoadsChunk(ChunkWatchEvent.Watch event) {
		Level level = event.getPlayer().level();
		ElectricHandlerCapability electricHandler = GameUtility.getLevelCapability(level, Capabilities.ELECTRIC_HANDLER_CAPABILITY);
		Set<Component<?, ?, ?>> componentsInChunk = electricHandler.findComponentsInChunk(event.getPos());
		Set<Component<?, ?, ?>> components = electricHandler.findComponentsConnectedWith(componentsInChunk.toArray(i -> new Component[i]));
		
		if (!components.isEmpty()) {
			List<ElectricNetwork> networks = components.stream().map(electricHandler.component2circuitMap::get).distinct().toList();
			IndustriaCore.NETWORK.send(PacketDistributor.PLAYER.with(event::getPlayer), new SSyncElectricComponentsPackage(components, event.getChunk().getPos(), SyncRequestType.ADDED));
			for (ElectricNetwork network : networks) {
				IndustriaCore.NETWORK.send(ElectricUtility.TRACKING_NETWORK.with(() -> network), new SUpdateElectricNetworkPackage(network));
			}
		}
	}
	
	@SubscribeEvent
	public static void onClientUnloadsChunk(ChunkWatchEvent.UnWatch event) {
		Level level = event.getPlayer().level();
		ElectricHandlerCapability electricHandler = GameUtility.getLevelCapability(level, Capabilities.ELECTRIC_HANDLER_CAPABILITY);
		Set<Component<?, ?, ?>> components = electricHandler.findComponentsInChunk(event.getPos());
		if (!components.isEmpty()) {
			IndustriaCore.NETWORK.send(PacketDistributor.PLAYER.with(event::getPlayer), new SSyncElectricComponentsPackage(components, event.getPos(), SyncRequestType.REMOVED));
		}
	}
	
	/* SPICE worker thread */

	public static SimulationProcessor getSimulationProcessor() {
		if (!hasProcessor()) startupProcessor();
		return simulationProcessor;
	}
	
	public static boolean hasProcessor() {
		return simulationProcessor != null && simulationProcessor.isRunning();
	}
	
	public static void startupProcessor() {
		if (hasProcessor()) {
			IndustriaCore.LOGGER.log(org.apache.logging.log4j.Level.WARN, "Electric network processor already running, this is not right!");
		}
		if (simulationProcessor == null) simulationProcessor = new SimulationProcessor(Config.ELECTIRC_SIMULATION_THREADS.get());
		simulationProcessor.start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownProcessor()));
	}
	
	public static void shutdownProcessor() {
		simulationProcessor.shutdown();
	}
	
	/* ElectricNetwork handling */
	
	/**
	 * Represents a component (can be a conduit or a block) in the electric networks
	 */
	public static class Component<I, P, T> {
		protected P pos;
		protected boolean hasChanged;
		protected I instance;
		protected IElectric<I, P, T> type;
		
		public Component(P pos, IElectric<I, P, T> type, I instance) {
			this.type = type;
			this.instance = instance;
			this.pos = pos;
			this.hasChanged = true;
		}
		
		public void setChanged() {
			this.hasChanged = true;
		}
		
		public P pos() {
			return pos;
		}
		
		public IElectric<I, P, T> type() {
			return type;
		}
		
		public I instance(Level level) {
			if ((this.hasChanged || !this.type.isInstanceValid(level, instance)) && level != null) {
				Optional<I> instanceLoaded = this.type.getInstance(level, this.pos);
				if (instanceLoaded.isPresent()) {
					this.instance = instanceLoaded.get();
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
			IElectric.Type componentType = IElectric.Type.getType(this.type);
			this.type.serializeNBTPosition(pos, nbt);
			nbt.putString("Type", componentType.getRegistry().getKey(this.type).toString());
			nbt.putString("ComponentType", componentType.name().toLowerCase());
			this.type.serializeNBTInstance(instance, nbt);
		}
		public static <I, P, T> Component<I, P, T> deserializeNbt(CompoundTag nbt) {
			IElectric.Type componentType = IElectric.Type.valueOf(nbt.getString("ComponentType").toUpperCase());
			ResourceLocation typeName = new ResourceLocation(nbt.getString("Type"));
			Object typeObject = componentType.getRegistry().getValue(typeName);
			if (typeObject instanceof IElectric) {
				@SuppressWarnings("unchecked")
				IElectric<I, P, T> type = (IElectric<I, P, T>) typeObject;
				P position = type.deserializeNBTPosition(nbt);
				I instance = type.deserializeNBTInstance(nbt);
				return new Component<I, P, T>(position, type, instance);
			}
			return null;
		}
		public void plotCircuit(Level level, ElectricNetwork circuit, Consumer<ICircuitPlot> plotter) {
			type.plotCircuit(level, instance(level), pos, circuit, plotter);
		}
		public NodePos[] getNodes(Level level) {
			return type.getConnections(level, pos, instance(level));
		}
		public void onNetworkChange(Level level) {
			type.onNetworkNotify(level, instance(level), pos);
		}
		public String[] getWireLanes(Level level, NodePos node) {
			return type.getWireLanes(level, pos, instance(level), node);
		}
		public void setWireLanes(Level level, NodePos node, String[] laneLabels) {
			String[] oldLanes = type.getWireLanes(level, pos, instance(level), node);
			type.setWireLanes(level, pos, instance(level), node, laneLabels);
			for (int i = 0; i < oldLanes.length && i < laneLabels.length; i++) {
				if (!oldLanes[i].equals(laneLabels[i])) {
					ElectricUtility.updateNetwork(level, pos);
					return;
				}
			}
		}
		public boolean isWire() {
			return type.isWire();
		}
		public ChunkPos getAffectedChunk(Level level) {
			return type.getAffectedChunk(level, pos);
		}
		public double getMaxPowerGeneration(Level level) {
			return type.getMaxPowerGeneration(level, pos, this.instance(level));
		}
		public double getCurrentPower(Level level) {
			return type.getCurrentPower(level, pos, this.instance(level));
		}
	}
	
	/**
	 * Returns the circuit which contains this component
	 */
	public ElectricNetwork getCircuitWithComponent(Component<?, ?, ?> component) {
		return this.component2circuitMap.get(component);
	}
	
	/**
	 * Returns the component with the given position
	 */
	@SuppressWarnings("unchecked")
	public <I, P, T> Component<I, P, T> getComponentAt(P position) {
		return (Component<I, P, T>) this.pos2componentMap.get(position);
	}
	
	/**
	 * Returns the network an component at the given position
	 */
	public <P> ElectricNetwork getNetworkAt(P position) {
		Component<?, P, ?> component = getComponentAt(position);
		if (component == null) return null;
		return this.component2circuitMap.get(component);
	}
	
	/**
	 * Returns a set containing all components attached to the given node
	 */
	public Set<Component<?, ?, ?>> findComponentsOnNode(NodePos node) {
		return this.node2componentMap.getOrDefault(node, new HashSet<>());
	}
	
	/**
	 * Returns a set containing all components in the given chunk
	 */
	public Set<Component<?, ?, ?>> findComponentsInChunk(ChunkPos chunkPos) {
		Set<Component<?, ?, ?>> components = new HashSet<>();
		for (Entry<Object, Component<?, ?, ?>> componentEntry : this.pos2componentMap.entrySet()) {
			if (componentEntry.getValue().getAffectedChunk(level).equals(chunkPos)) components.add(componentEntry.getValue());
		}
		return components;
	}
	
	/**
	 * Returns a set containing all components connected with the given components
	 */
	public Set<Component<?, ?, ?>> findComponentsConnectedWith(Component<?, ?, ?>... components) {
		Set<ElectricNetwork> networks = new HashSet<>();
		Set<Component<?, ?, ?>> componentsConnected = new HashSet<>();
		for (Component<?, ?, ?> component : components) {
			ElectricNetwork circuit = this.component2circuitMap.get(component);
			if (!networks.contains(circuit)) {
				networks.add(circuit);
				componentsConnected.addAll(circuit.getComponents());
			}
		}
		return componentsConnected;
	}
	
	/**
	 * Returns the floating voltage currently available on the given node.
	 * NOTE: Floating means that the voltage is referenced to "global ground", meaning a second voltage is required to calculate the actual difference (the voltage) between the two nodes.
	 */
	public Optional<Double> getFloatingNodeVoltage(NodePos node, int laneId, String lane) {
		Set<Component<?, ?, ?>> components = this.node2componentMap.get(node);
		if (components == null) return Optional.empty();
		Component<?, ?, ?> component = components.stream().findAny().orElseGet(() -> this.pos2componentMap.get(node.getBlock()));
		if (component != null) {
			ElectricNetwork network = this.component2circuitMap.get(component);
			if (network != null) {
				return network.getFloatingNodeVoltage(node, laneId, lane);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns the floating voltage currently available on the given node.
	 * NOTE: Floating means that the voltage is referenced to "global ground", meaning a second voltage is required to calculate the actual difference (the voltage) between the two nodes.
	 */
	public Optional<Double> getFloatingLocalNodeVoltage(BlockPos position, String lane, int group) {
		Component<?, ?, ?> component = this.pos2componentMap.get(position);
		if (component != null) {
			ElectricNetwork network = this.component2circuitMap.get(component);
			if (network != null) {
				return network.getFloatingLocalNodeVoltage(position, lane, group);
			}
		}
		return Optional.empty();
	}

	/**
	 * Injects node voltages without recalculating the network.
	 * Used to update the node voltages on the client side, were no simulation occurs.
	 */
	public void injectNodeVoltages(Set<Component<?, ?, ?>> components, String dataList) {
		ElectricNetwork network = new ElectricNetwork(this::getLevel, "ingame-dummy-level-circuit");
		network.getComponents().addAll(components);
		network.parseDataList(dataList);
		for (Component<?, ?, ?> component : components) {
			ElectricNetwork emptyNetwork = this.component2circuitMap.remove(component);
			if (emptyNetwork != null) {
				this.circuitNetworks.remove(emptyNetwork);
				emptyNetwork.getNodeVoltages().clear();
				emptyNetwork.getComponents().clear();
			}
			this.component2circuitMap.put(component, network);
		}
		this.circuitNetworks.add(network);
	}
	
	/**
	 * Changes the state of the network at the given position.
	 * If the state has actually changed, necessary events are triggered.
	 */
	public <P> void updateNetworkState(P position, PowerNetState state) {
		ElectricNetwork network = getNetworkAt(position);
		MinecraftForge.EVENT_BUS.post(new ElectricNetworkEvent.StateChangeEvent(this.level, network, state));
		network.setState(state);
		if (state == PowerNetState.FAILED) {
			MinecraftForge.EVENT_BUS.post(new ElectricNetworkEvent.FuseTripedEvent(this.level, network));
		}
		triggerUpdates(network);
	}
	
	/**
	 * Updates the network which has a component at the given position.
	 */
	public <P> void updateNetwork(P position) {
		
		if (this.level.isClientSide) return;
		
		Component<?, ?, ?> component = this.pos2componentMap.get(position);
		
		if (component != null) {
			
			ElectricNetwork circuit = this.component2circuitMap.get(component);
			
			if (circuit == null || !circuit.getComponents().contains(component)) circuit = new ElectricNetwork(() -> this.level, "ingame-level-circuit");
			
			circuit.reset();
			buildCircuit(component, circuit);
			if (circuit.isEmpty()) return;
			
			if (!this.circuitNetworks.contains(circuit)) this.circuitNetworks.add(circuit);
			
			final ElectricNetwork circuitFinalized = circuit;
			circuit.getComponents().forEach((comp) -> {
				ElectricNetwork previousNetwork = this.component2circuitMap.put(comp, circuitFinalized);
				if (previousNetwork != null && previousNetwork != circuitFinalized) {
					previousNetwork.getComponents().remove(comp);
					if (previousNetwork.getComponents().isEmpty()) {
						this.circuitNetworks.remove(previousNetwork);
					}
				}
			});
			
			getSimulationProcessor().processNetwork(circuit).thenAccept(state -> {
				if (!state) {
					circuitFinalized.tripFuse();
					MinecraftForge.EVENT_BUS.post(new ElectricNetworkEvent.FuseTripedEvent(this.level, circuitFinalized));
				}
				triggerUpdates(circuitFinalized);
			});
			
		}
		
	}

	/**
	 * Triggers the same update methods as when updating the network, without actually starting a new simulation.
	 */
	public void triggerUpdates(ElectricNetwork network) {
		network.getComponents().forEach(c -> c.onNetworkChange(network.getLevel()));
		IndustriaCore.NETWORK.send(ElectricUtility.TRACKING_NETWORK.with(() -> network), new SUpdateElectricNetworkPackage(network));
	}
	
	/**
	 * Removes a component from the network and updates it and its components
	 */
	public <I, P, T> void removeComponent(P pos) {
		if (this.pos2componentMap.containsKey(pos)) {
			Component<?, ?, ?> component = removeFromNetwork(pos);
			
			if (component != null) {
				Set<Component<?, ?, ?>> componentsToUpdate = new HashSet<Component<?, ?, ?>>();
				NodePos[] nodes = component.getNodes(level);
				for (int i = 0; i < nodes.length; i++) {
					Set<Component<?, ?, ?>> components = this.node2componentMap.get(nodes[i]);
					if (components != null && !components.isEmpty()) {
						components.remove(component);
						componentsToUpdate.add(components.stream().findAny().get());
					}
				}
				if (componentsToUpdate.isEmpty()) {
					ElectricNetwork emptyNetwork = this.component2circuitMap.remove(component);
					if (emptyNetwork != null) {
						this.circuitNetworks.remove(emptyNetwork);
					}
				}
				if (!this.level.isClientSide) {
					ChunkPos chunkPos = component.getAffectedChunk(level);
					IndustriaCore.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunk(chunkPos.x, chunkPos.z)), new SSyncElectricComponentsPackage(component, chunkPos, SyncRequestType.REMOVED));
				}
				for (Component<?, ?, ?> comp : componentsToUpdate) {
					updateNetwork(comp.pos());
				}
				component.onNetworkChange(level);
			}
		}
	}
	
	/**
	 * Adds a component to the network and updates it and its components
	 */
	public <I, P, T> void addComponent(P pos, IElectric<I, P, T> type, I instance) {
		
		Component<?, ?, ?> component = this.pos2componentMap.get(pos);
		if (component != null) {
			if (!component.type.equals(type)) {
				removeFromNetwork(pos);
			}
		}
		Component<I, P, T> component2 = new Component<I, P, T>(pos, type, instance);
		addToNetwork(component2);
		if (!this.level.isClientSide) {
			ChunkPos chunkPos = component2.getAffectedChunk(level);
			IndustriaCore.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunk(chunkPos.x, chunkPos.z)), new SSyncElectricComponentsPackage(component2, chunkPos, SyncRequestType.ADDED));
		}

		updateNetwork(pos);

	}
	
	/**
	 * Returns true if the component is already registered for an network
	 */
	public boolean isInNetwork(Component<?, ?, ?> component) {
		if (component.instance(level) == null) return false;
		return this.component2circuitMap.containsKey(component) && this.pos2componentMap.containsKey(component.pos());
	}

	/**
	 * Returns true if the component at the given position is already registered for an network
	 */
	public boolean isInNetwork(Object pos) {
		if (!this.pos2componentMap.containsKey(pos)) return false;
		return this.component2circuitMap.containsKey(this.pos2componentMap.get(pos));
	}
	
	/**
	 * Returns the internal collection of all electric components
	 */
	public Collection<Component<?, ?, ?>> getComponents() {
		return this.pos2componentMap.values();
	}
	
	/**
	 * Adds a component to the network but does not cause any updates
	 */
	public <I, P, T> void addToNetwork(Component<I, P, T> component) {
		if (component.instance(level) == null) return;
		this.pos2componentMap.put(component.pos, component);
		for (NodePos node : component.type().getConnections(this.level, component.pos, component.instance(level))) {
			Set<Component<?, ?, ?>> componentSet = this.node2componentMap.getOrDefault(node, new HashSet<Component<?, ?, ?>>());
			componentSet.add(component);
			this.node2componentMap.put(node, componentSet);
		}
	}
	
	/**
	 * Removes a component from the network but does not cause any updates
	 */
	public <I, P, T> Component<I, P, T> removeFromNetwork(P pos) {
		@SuppressWarnings("unchecked")
		Component<I, P, T> component = (Component<I, P, T>) this.pos2componentMap.remove(pos);
		if (component != null) {
			List<NodePos> empty = new ArrayList<>();
			for (Entry<NodePos, Set<Component<?, ?, ?>>> entry : this.node2componentMap.entrySet()) {
				entry.getValue().remove(component);
				if (entry.getValue().isEmpty()) empty.add(entry.getKey());
			}
			empty.forEach(c -> this.node2componentMap.remove(c));
		}
		return component;
	}
	
	/**
	 * Builds a ngspice netlist for the given network beginning from the given component
	 */
	private void buildCircuit(Component<?, ?, ?> component, ElectricNetwork circuit) {
		buildCircuit0(component, null, circuit);
		circuit.complete(this.level.getGameTime());
	}
	private void buildCircuit0(Component<?, ?, ?> component, NodePos node, ElectricNetwork circuit) {
		
		if (circuit.getComponents().contains(component)) return;
		
		circuit.getComponents().add(component);
		circuit.plotComponentDescriptor(component);
		component.plotCircuit(level, circuit, template -> circuit.plotTemplate(component, template));
		
		for (NodePos node2 : component.getNodes(level)) {
			if (node2.equals(node) || this.node2componentMap.get(node2) == null) continue; 
			for (Component<?, ?, ?> component2 : this.node2componentMap.get(node2)) {
				buildCircuit0(component2, node2, circuit);
			}
		}
		
	}
	
}
