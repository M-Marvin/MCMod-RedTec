package de.m_marvin.industria.core.electrics.engine.network;

import java.util.function.Supplier;

import de.m_marvin.industria.core.electrics.engine.ServerElectricPackageHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CPlayerSwitchNetworkPackage {
	
	BlockPos pos;
	boolean state;
	
	public CPlayerSwitchNetworkPackage(BlockPos component, boolean state) {
		this.pos = component;
		this.state = state;
	}
	
	public BlockPos getComponent() {
		return pos;
	}

	public boolean getState() {
		return state;
	}
	
	public static void encode(CPlayerSwitchNetworkPackage msg, FriendlyByteBuf buff) {
		buff.writeBlockPos(msg.pos);
		buff.writeBoolean(msg.state);
	}
	
	public static CPlayerSwitchNetworkPackage decode(FriendlyByteBuf buff) {
		BlockPos pos = buff.readBlockPos();
		boolean state = buff.readBoolean();
		return new CPlayerSwitchNetworkPackage(pos, state);
	}
	
	public static void handle(CPlayerSwitchNetworkPackage msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerElectricPackageHandler.handlePlayerSwitchNetwork(msg, ctx.get());
		});
		ctx.get().setPacketHandled(true);
	}
	
}
