package de.m_marvin.industria.core.electrics.engine.network;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import de.m_marvin.industria.core.electrics.engine.ClientElectricPackageHandler;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork.State;
import de.m_marvin.industria.core.electrics.engine.ElectricNetworkHandlerCapability.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/*
 * Tells the client that a networks needs to be updated with new node voltages
 */
public class SUpdateNetworkPackage {
	
	public final String dataList;
	public final Set<Component<?, ?, ?>> components;
	public final State state;
	
	public SUpdateNetworkPackage(ElectricNetwork network) {
		this.dataList = network.printDataList();
		this.components = network.getComponents();
		this.state = network.getState();;
	}
	
	public SUpdateNetworkPackage(Set<Component<?, ?, ?>> components, String dataList, State state) {
		this.dataList = dataList;
		this.components = components;
		this.state = state;
	}
	
	public Set<Component<?, ?, ?>> getComponents() {
		return components;
	}
	
	public String getDataList() {
		return dataList;
	}
	
	public State getState() {
		return state;
	}
	
	public static void encode(SUpdateNetworkPackage msg, FriendlyByteBuf buff) {
		buff.writeInt(msg.components.size());
		for (Component<?, ?, ?> component : msg.components) {
			CompoundTag componentTag = new CompoundTag();
			component.serializeNbt(componentTag);
			buff.writeNbt(componentTag);
		}
		buff.writeUtf(msg.dataList);
		buff.writeEnum(msg.state);
	}
	
	public static SUpdateNetworkPackage decode(FriendlyByteBuf buff) {
		int componentCount = buff.readInt();
		Set<Component<?, ?, ?>> components = new HashSet<>();
		for (int i = 0; i < componentCount; i++) {
			CompoundTag componentTag = buff.readNbt();
			Component<?, ?, ?> component = Component.deserializeNbt(componentTag);
			components.add(component);
		}
		String dataList = buff.readUtf();
		State state = buff.readEnum(State.class);
		return new SUpdateNetworkPackage(components, dataList, state);
	}
	
	public static void handle(SUpdateNetworkPackage msg, Supplier<NetworkEvent.Context> ctx) {
		
		ctx.get().enqueueWork(() -> {
			ClientElectricPackageHandler.handleUpdateNetwork(msg, ctx.get());
		});
		ctx.get().setPacketHandled(true);
		
	}
	
}
