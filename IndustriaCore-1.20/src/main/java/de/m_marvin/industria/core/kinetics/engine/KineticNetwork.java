package de.m_marvin.industria.core.kinetics.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.kinetics.engine.KineticHandlerCapabillity.Component;
import de.m_marvin.industria.core.util.types.PowerNetState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;

public class KineticNetwork {
	
	public static record TransmissionJoint(Component a, Component b, double ratio) {}
	
	protected final Supplier<Level> level;
	protected Set<Component> components = ConcurrentHashMap.newKeySet();
	protected Set<TransmissionJoint> joints = ConcurrentHashMap.newKeySet();
	
	protected PowerNetState state = PowerNetState.ACTIVE;
	
//	protected double maxPower;
//	protected double currentConsumtion;
//	protected double currentProduction;

	public KineticNetwork(Supplier<Level> level) {
		this.level = level;
	}
	
	public Level getLevel() {
		return level.get();
	}
	
	public CompoundTag saveNBT(KineticHandlerCapabillity handler) {
		CompoundTag tag = new CompoundTag();
		
		ListTag componentsTag = new ListTag();
		for (Component component : this.components) {
			if (component == null) continue;
			try {
				CompoundTag compTag = new CompoundTag();
				component.serializeNbt(compTag);
				componentsTag.add(compTag);
			} catch (Exception e) {
				IndustriaCore.LOGGER.error("Failed to serialize kinetic component at " + component.pos() + "!");
				e.printStackTrace();
			}
		}
		tag.put("Components", componentsTag);
		tag.putString("State", this.state.name().toLowerCase());
		return tag;
	}
	
	public void loadNBT(KineticHandlerCapabillity handler, CompoundTag tag) {
		ListTag componentsTag = tag.getList("Components", ListTag.TAG_COMPOUND);
		componentsTag.stream().forEach((componentTag) -> {
			this.components.add(Component.deserializeNbt((CompoundTag) componentTag));
		});
		this.state = PowerNetState.valueOf(tag.getString("State").toUpperCase());
	}
	
	public Set<Component> getComponents() {
		return components;
	}

	public Set<TransmissionJoint> getJoints() {
		return joints;
	}
	
	public void reset() {
//		this.maxPower = 0;
//		this.currentConsumtion = 0;
//		this.circuitBuilder = new StringBuilder();
//		this.netList = "";
//		this.groundNode = null;
		this.components = ConcurrentHashMap.newKeySet();
	}
	
	public boolean isEmpty() {
		return components.isEmpty();
	}

	public void removeInvalidComponents() {
		List<Component> invalid = new ArrayList<>();
		for (Component component : this.components) {
			if (component == null || component.instance(null) == null) invalid.add(component);
		}
		invalid.forEach(c -> components.remove(c));
	}
	
//	public void tripFuse() {
//		setState(State.OVERLOADED);
//	}
	
//	public void setState(State state) {
//		this.state = state;
//		recalculateLoads();
//	}
	
//	public State getState() {
//		return state;
//	}
	
//	public boolean isTripped() {
//		return this.state == State.OVERLOADED;
//	}
//	
//	public boolean isOnline() {
//		return this.state == State.ACTIVE;
//	}
	
}
