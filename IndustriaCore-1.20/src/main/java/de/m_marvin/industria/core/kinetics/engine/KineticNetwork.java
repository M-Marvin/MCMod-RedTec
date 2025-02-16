package de.m_marvin.industria.core.kinetics.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.kinetics.engine.KineticHandlerCapabillity.Component;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.KineticReference;
import de.m_marvin.industria.core.util.types.PowerNetState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;

public class KineticNetwork {
	
	public static record TransmissionJoint(Component a, Component b, double ratio) {}
	
	protected final Supplier<Level> level;
	protected Map<KineticReference, Component> components = new HashMap<>();
	protected Map<Component, Double> component2ratioMap = new HashMap<>();
	protected double speed = 0;
	
	protected PowerNetState state = PowerNetState.ACTIVE;
	
	public KineticNetwork(Supplier<Level> level) {
		this.level = level;
	}
	
	public Level getLevel() {
		return level.get();
	}
	
	public CompoundTag saveNBT(KineticHandlerCapabillity handler) {
		CompoundTag tag = new CompoundTag();
		ListTag componentsTag = new ListTag();
		for (Component component : this.components.values()) {
			if (component == null) continue;
			try {
				CompoundTag compTag = new CompoundTag();
				component.serializeNbt(compTag);
				compTag.putDouble("Ratio", this.component2ratioMap.getOrDefault(component, 0.0));
				componentsTag.add(compTag);
			} catch (Exception e) {
				IndustriaCore.LOGGER.error("Failed to serialize kinetic component at " + component.reference() + "!");
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
			Component component = Component.deserializeNbt((CompoundTag) componentTag);
			this.components.put(component.reference(), component);
			this.component2ratioMap.put(component, ((CompoundTag) componentTag).getDouble("Ratio"));
		});
		this.state = PowerNetState.valueOf(tag.getString("State").toUpperCase());
	}
	
	public Collection<Component> getComponents() {
		return components.values();
	}
	
	public void addComponent(Component component) {
		this.components.put(component.reference(), component);
	}
	
	public boolean hasReference(KineticReference reference) {
		return components.containsKey(reference);
	}

	public boolean addTransmission(Component component1, Component component2, double ratio) {
		if (ratio == 0.0) return true;
		Double ratio1 = this.component2ratioMap.get(component1);
		if (ratio1 != null) {
			Double r = this.component2ratioMap.put(component2, ratio1 / ratio);
			if (r != null && Double.compare(r, ratio1 / ratio) != 0) return false;
		} else {
			Double ratio2 = this.component2ratioMap.get(component2);
			if (ratio2 == null) {
				ratio2 = 1.0;
				this.component2ratioMap.put(component2, ratio2);
			}
			this.component2ratioMap.put(component1, ratio2 * ratio);
		}
		return true;
	}
	
	public double getTransmission(Component component) {
		return this.component2ratioMap.getOrDefault(component, 0.0);
	}
	
	public void setNetworkSpeed(double speed) {
		this.speed = speed;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public void reset() {
		this.components.clear();
		this.component2ratioMap.clear();
		this.state = PowerNetState.INACTIVE;
	}
	
	public boolean isEmpty() {
		return components.isEmpty();
	}

	public void removeInvalidComponents() {
		List<Component> invalid = new ArrayList<>();
		for (Component component : this.components.values()) {
			if (component == null || component.instance(null) == null || !(component.instance(null).getBlock() instanceof IKineticBlock)) invalid.add(component);
		}
		invalid.forEach(c -> components.remove(c.reference()));
	}
	
	public void setLocked() {
		setState(PowerNetState.FAILED);
	}
	
	public void setState(PowerNetState state) {
		this.state = state;
	}
	
	public PowerNetState getState() {
		return state;
	}
	
	public boolean isLocked() {
		return this.state == PowerNetState.FAILED;
	}
	
	public boolean isOnline() {
		return this.state == PowerNetState.ACTIVE;
	}
	
}
