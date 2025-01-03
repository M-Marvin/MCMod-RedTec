package de.m_marvin.industria.core.kinetics.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.Maps;

import de.m_marvin.industria.core.electrics.engine.ElectricHandlerCapability;
import de.m_marvin.industria.core.electrics.engine.ElectricHandlerCapability.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class KineticNetwork {
	
	public static enum State {
		ACTIVE,INACTIVE,OVERLOADED;
	}
	
	protected final Supplier<Level> level;
	protected Set<Component<?, ?, ?>> components = ConcurrentHashMap.newKeySet();
	protected long templateCounter;
	protected StringBuilder circuitBuilder;
	protected String groundNode;
	protected String netList = "";
	protected Map<String, Double> nodeVoltages = Maps.newHashMap();
	protected State state = State.ACTIVE;
	
	protected double maxPower;
	protected double currentConsumtion;
	protected double currentProduction;

	public KineticNetwork(Supplier<Level> level) {
		this.level = level;
	}
	
	public Level getLevel() {
		return level.get();
	}
	
	public CompoundTag saveNBT(ElectricHandlerCapability handler) {
		CompoundTag tag = new CompoundTag();
		
//		ListTag componentsTag = new ListTag();
//		for (Component<?, ?, ?> component : this.components) {
//			if (component == null) continue;
//			try {
//				CompoundTag compTag = new CompoundTag();
//				component.serializeNbt(compTag);
//				componentsTag.add(compTag);
//			} catch (Exception e) {
//				IndustriaCore.LOGGER.error("Failed to serialize electric component at " + component.pos() + "!");
//				e.printStackTrace();
//			}
//		}
//		tag.put("Components", componentsTag);
//		if (!this.isEmpty() && !this.isPlotEmpty() && this.components.size() > 1) {
//			String circuitName = handler.saveCircuit(this.netList, printDataList());
//			tag.putString("Circuit", circuitName);
//		}
//		tag.putString("State", this.state.name().toLowerCase());
		return tag;
	}
	
	public void loadNBT(ElectricHandlerCapability handler, CompoundTag tag) {
//		ListTag componentsTag = tag.getList("Components", ListTag.TAG_COMPOUND);
//		componentsTag.stream().forEach((componentTag) -> {
//			this.components.add(Component.deserializeNbt((CompoundTag) componentTag));
//		});
//		if (tag.contains("Circuit")) {
//			String circuitName = tag.getString("Circuit");
//			String[] lists = handler.loadCircuit(circuitName);
//			this.netList = lists[0];
//			this.parseDataList(lists[1]);
//		}
//		this.state = State.valueOf(tag.getString("State").toUpperCase());
	}
	
	public Set<Component<?, ?, ?>> getComponents() {
		return components;
	}

	public void reset() {
		this.maxPower = 0;
		this.currentConsumtion = 0;
		this.circuitBuilder = new StringBuilder();
		this.netList = "";
		this.groundNode = null;
		this.components = ConcurrentHashMap.newKeySet();
	}
	
	private void findConnected(List<String> connectedList, String current, List<List<String>> nodeGroups) {
		Set<String> foundNodes = new HashSet<>();
		nodeGroups.stream().filter(group -> group.contains(current)).forEach(group -> group.stream().filter(node -> !connectedList.contains(node)).forEach(foundNodes::add));
		if (!foundNodes.isEmpty()) {
			connectedList.addAll(foundNodes);
			for (String node : foundNodes) {
				findConnected(connectedList, node, nodeGroups);
			}
		}
	}
	
	public boolean isPlotEmpty() {
		return this.netList.isEmpty();
	}
	
	public boolean isEmpty() {
		return components.isEmpty();
	}

	public void removeInvalidComponents() {
		List<Component<?, ?, ?>> invalid = new ArrayList<>();
		for (Component<?, ?, ?> component : this.components) {
			if (component == null || component.instance(null) == null) invalid.add(component);
		}
		invalid.forEach(c -> components.remove(c));
	}
	
	@Override
	public String toString() {
		return isPlotEmpty() ? "EMPTY" : (this.netList == null ? this.circuitBuilder.toString() : netList);
	}
	
	public synchronized Map<String, Double> getNodeVoltages() {
		return nodeVoltages;
	}
	
	public synchronized boolean parseDataList(String dataList) {
		this.nodeVoltages.clear();
		Stream.of(dataList.split("\n"))
			.map(s -> s.split("\t"))
			.filter(s -> s.length == 2)
			.forEach(s -> this.nodeVoltages.put(s[0], Double.valueOf(s[1].split(" V")[0])));

		recalculateLoads();
		return this.nodeVoltages.size() > 0;
	}
	
	public synchronized void recalculateLoads() {
		this.maxPower = 0;
		this.currentConsumtion = 0;
		this.currentProduction = 0;
		for (Component<?, ?, ?> c : this.components) {
			this.maxPower += c.getMaxPowerGeneration(getLevel());
			double p = c.getCurrentPower(getLevel());
			if (p > 0) {
				this.currentProduction += p;
			} else {
				this.currentConsumtion += -p;
			}
		}
	}
	
	public double getMaxPower() {
		return maxPower;
	}
	
	public double getCurrentConsumtion() {
		return currentConsumtion;
	}
	
	public double getCurrentProduction() {
		return currentProduction;
	}
	
	public void tripFuse() {
		setState(State.OVERLOADED);
	}
	
	public void setState(State state) {
		this.state = state;
		recalculateLoads();
	}
	
	public State getState() {
		return state;
	}
	
	public boolean isTripped() {
		return this.state == State.OVERLOADED;
	}
	
	public boolean isOnline() {
		return this.state == State.ACTIVE;
	}
	
}
