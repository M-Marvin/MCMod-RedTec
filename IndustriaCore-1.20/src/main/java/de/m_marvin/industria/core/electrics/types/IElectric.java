package de.m_marvin.industria.core.electrics.types;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.conduits.types.conduits.Conduit;
import de.m_marvin.industria.core.electrics.engine.ElectricHandlerCapability;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.registries.Capabilities;
import de.m_marvin.industria.core.registries.Conduits;
import de.m_marvin.industria.core.util.GameUtility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public interface IElectric<I, P, T> {
	
	public static interface ICircuitPlot {
		public void prepare(long templateId);
		public String plot();
		public String getAnyNode();
	}
	
	public default void updateNetwork(Level level, P position) {
		ElectricHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.ELECTRIC_HANDLER_CAPABILITY);
		handler.updateNetwork(position);
	}
	
	public void plotCircuit(Level level, I instance, P position, ElectricNetwork circuit, Consumer<ICircuitPlot> plotter);
	public default void serializeNBT(I instance, P position, CompoundTag nbt) {
		serializeNBTInstance(instance, nbt);
		serializeNBTPosition(position, nbt);
	}
	public void serializeNBTInstance(I instance, CompoundTag nbt);
	public void serializeNBTPosition(P position, CompoundTag nbt);
	public I deserializeNBTInstance(CompoundTag nbt);
	public P deserializeNBTPosition(CompoundTag nbt);
	
	public double getMaxPowerGeneration(Level level, P pos, I instance);
	public double getCurrentPower(Level level, P pos, I instance);
	
	public NodePos[] getConnections(Level level, P pos, I instance);
	public String[] getWireLanes(Level level, P pos, I instance, NodePos node);
	public void setWireLanes(Level level, P pos, I instance, NodePos node, String[] laneLabels);
	public boolean isWire();
	public ChunkPos getAffectedChunk(Level level, P pos);
	public Optional<I> getInstance(Level level, P pos);
	public boolean isInstanceValid(Level level, I instance);
	
	/**
	 * WARNING: Is called on an another thread, do not interact with the world to much, limit access to the level to triggering updates.
	 */
	public default void onNetworkNotify(Level level, I instance, P position) {}
	
	public static enum Type {
		
		BLOCK(() -> ForgeRegistries.BLOCKS),CONDUIT(Conduits.CONDUITS_REGISTRY::get);
		
		private Supplier<IForgeRegistry<?>> registry;
		
		private Type(Supplier<IForgeRegistry<?>> registry) {
			this.registry = registry;
		}
		
		@SuppressWarnings("unchecked")
		public IForgeRegistry<Object> getRegistry() {
			return (IForgeRegistry<Object>) this.registry.get();
		}
		
		public static Type getType(Object type) {
			if (type instanceof Block) return BLOCK;
			if (type instanceof Conduit) return CONDUIT;
			return BLOCK;
		}
		
	}
	
}
