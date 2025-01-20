package de.m_marvin.industria.core.kinetics.engine.network;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import de.m_marvin.industria.core.kinetics.engine.ClientKineticPackageHandler;
import de.m_marvin.industria.core.kinetics.engine.KineticHandlerCapabillity.Component;
import de.m_marvin.industria.core.util.types.SyncRequestType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent.Context;

/*
 * Tells the client that a networks needs to be updated with new node voltages
 */
public class SSyncKineticComponentsPackage {
	
	public final ChunkPos chunkPos;
	public final Set<Component> components;
	public final SyncRequestType request;
	
	public SSyncKineticComponentsPackage(Set<Component> components, ChunkPos targetChunk, SyncRequestType request) {
		this.chunkPos = targetChunk;
		this.components = components;
		this.request = request;
	}
	
	public SSyncKineticComponentsPackage(Component component, ChunkPos targetChunk, SyncRequestType request) {
		this.chunkPos = targetChunk;
		this.components = new HashSet<>();
		this.components.add(component);
		this.request = request;
	}

	public ChunkPos getChunkPos() {
		return chunkPos;
	}
	
	public Set<Component> getComponents() {
		return components;
	}
	
	public SyncRequestType getRequest() {
		return request;
	}
	
	public static void encode(SSyncKineticComponentsPackage msg, FriendlyByteBuf buff) {
		buff.writeInt(msg.components.size());
		for (Component component : msg.components) {
			CompoundTag componentTag = new CompoundTag();
			component.serializeNbt(componentTag);
			buff.writeNbt(componentTag);
		}
		buff.writeChunkPos(msg.chunkPos);
		buff.writeEnum(msg.request);
	}
	
	public static SSyncKineticComponentsPackage decode(FriendlyByteBuf buff) {
		int componentCount = buff.readInt();
		Set<Component> components = new HashSet<>();
		for (int i = 0; i < componentCount; i++) {
			CompoundTag componentTag = buff.readNbt();
			Component component = Component.deserializeNbt(componentTag);
			components.add(component);
		}
		ChunkPos chunkPos = buff.readChunkPos();
		SyncRequestType request = buff.readEnum(SyncRequestType.class);
		return new SSyncKineticComponentsPackage(components, chunkPos, request);
	}
	
	public static void handle(SSyncKineticComponentsPackage msg, Supplier<Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientKineticPackageHandler.handleSyncComponentsServer(msg, ctx.get());
		});
		ctx.get().setPacketHandled(true);
	}
	
}
