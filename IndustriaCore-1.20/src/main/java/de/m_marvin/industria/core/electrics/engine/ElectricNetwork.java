package de.m_marvin.industria.core.electrics.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.collect.Maps;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.engine.ElectricHandlerCapability.Component;
import de.m_marvin.industria.core.electrics.types.IElectric.ICircuitPlot;
import de.m_marvin.industria.core.util.types.PowerNetState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;

public class ElectricNetwork {
		
	protected String title;
	protected final Supplier<Level> level;
	protected Set<Component<?, ?, ?>> components = ConcurrentHashMap.newKeySet();
	protected long templateCounter;
	protected StringBuilder circuitBuilder;
	protected String groundNode;
	protected String netList = "";
	protected Map<String, Double> nodeVoltages = Maps.newHashMap();
	protected PowerNetState state = PowerNetState.ACTIVE;
	protected double maxPower;
	protected double currentConsumtion;
	protected double currentProduction;

	public ElectricNetwork(Supplier<Level> level, String titleInfo) {
		this.level = level;
		this.title = titleInfo;
	}
	
	public Level getLevel() {
		return level.get();
	}
	
	public CompoundTag saveNBT(ElectricHandlerCapability handler) {
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
		tag.putString("State", this.state.name().toLowerCase());
		return tag;
	}
	
	public void loadNBT(ElectricHandlerCapability handler, CompoundTag tag) {
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
		this.state = PowerNetState.valueOf(tag.getString("State").toUpperCase());
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
			this.netList = filterSingularMatrixNodes(String.format("%s\n%s\n\n%s", title, circuitBuilder.toString(), groundResistor));
		} else {
			this.netList = "";
		}
	}

	private String filterSingularMatrixNodes(String netlist) {
		
		Optional<String> groundNode = netlist.lines().map(line -> {
			Matcher nodeMatcher = FILTER_GROUND_PATTERN.matcher(line);
			return nodeMatcher.find() ? nodeMatcher.group(1) : null;
		}).filter(s -> s != null).findAny();
		
		if (groundNode.isEmpty()) return null;
		
		List<List<String>> lineNodes = netlist.lines().map(line -> {
			Matcher nodeMatcher = FILTER_NODE_PATTERN.matcher(line);
			return nodeMatcher.results().map(MatchResult::group).toList();
		}).toList();
		
		List<String> connectedNodes = new ArrayList<>();
		findConnected(connectedNodes, groundNode.get(), lineNodes);

		StringBuilder filterList = new StringBuilder();
		List<String> lines = netlist.lines().toList();
		for (int i = 0; i < lineNodes.size(); i++) {
			boolean isSingular = lineNodes.get(i).size() > 0 && lineNodes.get(i).stream().filter(node -> connectedNodes.contains(node)).count() == 0;
			if (isSingular) continue;
			filterList.append(lines.get(i) + "\n");
		}
		
		return filterList.toString();
		
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
		setState(PowerNetState.FAILED);
	}
	
	public void setState(PowerNetState state) {
		this.state = state;
		recalculateLoads();
	}
	
	public PowerNetState getState() {
		return state;
	}
	
	public boolean isTripped() {
		return this.state == PowerNetState.FAILED;
	}
	
	public boolean isOnline() {
		return this.state == PowerNetState.ACTIVE;
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
	
	public synchronized Optional<Double> getFloatingNodeVoltage(NodePos node, int laneId, String lane) {
		String nodeName = getNodeKeyString(node, laneId, lane);
		if (!this.nodeVoltages.containsKey(nodeName)) return Optional.empty();
		return Optional.of(isOnline() ? this.nodeVoltages.get(nodeName) : 0.0);
	}

	public synchronized Optional<Double> getFloatingLocalNodeVoltage(BlockPos position, String lane, int group) {
		String nodeName = getLocalNodeKeyString(position, lane, group);
		if (!this.nodeVoltages.containsKey(nodeName)) return Optional.empty();
		return Optional.of(isOnline() ? this.nodeVoltages.get(nodeName) : 0.0);
	}

	private static final Pattern FILTER_NODE_PATTERN = Pattern.compile("(?:N[0-9_]{5,})|(?:node\\|[A-Za-z0-9_~]+\\|)|(?:intnode\\|[A-Za-z0-9_~]+\\|_[0-9])");
	private static final Pattern FILTER_GROUND_PATTERN = Pattern.compile("R0GND ([^ ]+) 0 1");
	
	public static String getLocalNodeKeyString(BlockPos position, String laneName, int group) {
		return ("IntNode|pos" + position.getX() + "_" + position.getY() + "_" + position.getZ() + "_lnm" + laneName + "|_" + group)
				.toLowerCase()
				.replace('-', '~'); // '-' would be interpreted as mathematical operator, '~' not
	}
	
	public static String getNodeKeyString(NodePos node, int laneId, String laneName) {
		return ("Node|pos" + node.getBlock().getX() + "_" + node.getBlock().getY() + "_" + node.getBlock().getZ() + "_id" + node.getNode() + "_lid" + laneId + "_lnm" + laneName + "|")
				.toLowerCase()
				.replace('-', '~'); // '-' would be interpreted as mathematical operator, '~' not
	}
	
}
