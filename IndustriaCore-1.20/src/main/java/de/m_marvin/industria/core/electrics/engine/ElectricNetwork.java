package de.m_marvin.industria.core.electrics.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.Maps;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.engine.ElectricNetworkHandlerCapability.Component;
import de.m_marvin.industria.core.electrics.types.IElectric.ICircuitPlot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;

public class ElectricNetwork {
	
	protected String title;
	protected final Supplier<Level> level;
	protected Set<Component<?, ?, ?>> components = ConcurrentHashMap.newKeySet();
	protected Set<Component<?, ?, ?>> componentsLast = new HashSet<>();
	protected long templateCounter;
	protected StringBuilder circuitBuilder;
	protected String groundNode;
	protected String netList = "";
	protected Map<String, Double> nodeVoltages = Maps.newHashMap();

	protected static Object ngLinkLock = new Object();
	
	public ElectricNetwork(Supplier<Level> level, String titleInfo) {
		this.level = level;
		this.title = titleInfo;
	}
	
	public Level getLevel() {
		return level.get();
	}
	
	public CompoundTag saveNBT(ElectricNetworkHandlerCapability handler) {
		CompoundTag tag = new CompoundTag();
		ListTag componentsTag = new ListTag();
		for (Component<?, ?, ?> component : this.components) {
			if (component == null) continue;
			try {
				CompoundTag compTag = new CompoundTag();
				component.serializeNbt(compTag);
				componentsTag.add(compTag);
			} catch (Exception e) {
				IndustriaCore.LOGGER.error("Failed to serialize electric component at " + component.pos() + "!");
				e.printStackTrace();
			}
		}
		tag.put("Components", componentsTag);
		if (!this.isEmpty() && !this.isPlotEmpty() && this.components.size() > 1) {
			String circuitName = handler.saveCircuit(this.netList, printDataList());
			tag.putString("Circuit", circuitName);
		}
		return tag;
	}
	
	public void loadNBT(ElectricNetworkHandlerCapability handler, CompoundTag tag) {
		ListTag componentsTag = tag.getList("Components", ListTag.TAG_COMPOUND);
		componentsTag.stream().forEach((componentTag) -> {
			this.components.add(Component.deserializeNbt((CompoundTag) componentTag));
		});
		if (tag.contains("Circuit")) {
			String circuitName = tag.getString("Circuit");
			String[] lists = handler.loadCircuit(circuitName);
			this.netList = lists[0];
			this.parseDataList(lists[1]);
		}
	}
	
	public Set<Component<?, ?, ?>> getComponents() {
		return components;
	}

	public void reset() {
		this.circuitBuilder = new StringBuilder();
		this.netList = "";
		this.groundNode = null;
		this.componentsLast = components;
		this.components = ConcurrentHashMap.newKeySet();
	}
	
	public void plotComponentDescriptor(Component<?, ?, ?> component) {
		this.circuitBuilder.append("\n* Component " + component.type().toString() + " " + component.pos().toString() + "\n");
	}
	
	public void plotTemplate(Component<?, ?, ?> component, ICircuitPlot template) {
		template.prepare(templateCounter++);
		this.circuitBuilder.append(template.plot());
		if (this.groundNode == null) this.groundNode = template.getAnyNode();
	}

	public void complete(long frame) {
		if (!this.circuitBuilder.isEmpty()) {
			String groundResistor = "R0GND " + groundNode + " 0 1";
			this.netList = String.format("%s\n%s\n\n%s", title, circuitBuilder.toString(), groundResistor);
		} else {
			this.netList = "";
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
	
	public synchronized void parseDataList(String dataList) {
		Stream.of(dataList.split("\n"))
			.map(s -> s.split("\t"))
			.filter(s -> s.length == 2)
			.forEach(s -> this.nodeVoltages.put(s[0], Double.valueOf(s[1].split(" V")[0])));
	}
	
	public String getNetList() {
		return netList == null ? "" : this.netList;
	}
	
	public String printDataList() {
		StringBuffer sb = new StringBuffer();
		for (Entry<String, Double> e : this.nodeVoltages.entrySet()) {
			sb.append(e.getKey()).append("\t").append(e.getValue()).append("\n");
		}
		return sb.toString();
	}
	
	public synchronized double getFloatingNodeVoltage(NodePos node, int laneId, String lane) {
		return this.nodeVoltages.getOrDefault(getNodeKeyString(node, laneId, lane), 0.0);
	}
	
	public static String getNodeKeyString(NodePos node, int laneId, String laneName) {
		return ("Node|pos" + node.getBlock().getX() + "_" + node.getBlock().getY() + "_" + node.getBlock().getZ() + "_id" + node.getNode() + "_lid" + laneId + "_lnm" + laneName + "|")
				.toLowerCase()
				.replace('-', '~'); // '-' would be interpreted as mathematical operator, '~' not
	}
	
	public static String getPositionKeyString(BlockPos position) {
		return ("pos" + position.getX() + "_" + position.getY() + "_" + position.getZ())
				.toLowerCase()
				.replace('-', '~'); // '-' would be interpreted as mathematical operator, '~' not
	}
	
}
