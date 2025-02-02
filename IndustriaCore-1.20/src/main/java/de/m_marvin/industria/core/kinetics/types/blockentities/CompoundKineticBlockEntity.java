package de.m_marvin.industria.core.kinetics.types.blockentities;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.KineticReference;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import de.m_marvin.industria.core.registries.BlockEntityTypes;
import de.m_marvin.industria.core.util.types.virtualblock.VirtualBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CompoundKineticBlockEntity extends BlockEntity implements IKineticBlockEntity {

	protected static record KineticPart() {}
	
	protected Map<Integer, VirtualBlock<Block, BlockEntity>> parts = new HashMap<>();
	
	public CompoundKineticBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityTypes.COMPOUND_KINETIC.get(), pPos, pBlockState);
	}
	
	public TransmissionNode[] getTransmissionNodes() {
		return this.parts.entrySet().stream()
				.filter(e -> e.getValue().getBlock() instanceof IKineticBlock)
				.flatMap(e -> 
					Stream.of(((IKineticBlock) e.getValue().getBlock())
					.getTransmissionNodes(e.getValue().getLevel(), e.getValue().getPos(), e.getValue().getState()))
					.map(t -> t.withReference(KineticReference.subPart(worldPosition, e.getKey())))
				)
				.toArray(TransmissionNode[]::new);
	}
	
	public BlockState getPartState(int partId) {
		if (!this.parts.containsKey(partId)) return getBlockState();
		return this.parts.get(partId).getState();
	}
	
	public Map<Integer, VirtualBlock<Block, BlockEntity>> getParts() {
		return parts;
	}
	
	@Override
	public void setRPM(int partId, int rpm) {
		var virtualBlock = this.parts.get(partId);
		if (virtualBlock != null && virtualBlock.getBlock() instanceof IKineticBlock block) {
			block.setRPM(virtualBlock.getLevel(), virtualBlock.getPos(), 0, virtualBlock.getState(), rpm);
			this.setChanged();
		}
	}

	@Override
	public int getRPM(int partId) {
		var virtualBlock = this.parts.get(partId);
		if (virtualBlock != null && virtualBlock.getBlock() instanceof IKineticBlock block) {
			block.getRPM(virtualBlock.getLevel(), virtualBlock.getPos(), 0, virtualBlock.getState());
		}
		return 0;
	}
	
	public boolean addPart(BlockState state) {
		int id = 1;
		for (; this.parts.containsKey(id); id++)
			if (this.parts.get(id).getState().isAir()) break;
		if (!this.parts.containsKey(id)) {
			var virtualBlock = new VirtualBlock<Block, BlockEntity>(this::getBlockPos);
			if (level != null) virtualBlock.setLevel(level);
			this.parts.put(id, virtualBlock);
		}
		this.parts.get(id).setBlock(state);
		this.setChanged();
		return true;
	}
	
	@Override
	protected void saveAdditional(CompoundTag pTag) {
		CompoundTag parts = new CompoundTag();
		for (var part : this.parts.entrySet()) {
			if (part.getValue().getState().isAir()) continue;
			parts.put(Integer.toString(part.getKey()), part.getValue().serialize());
		}
		pTag.put("Parts", parts);
	}
	
	@Override
	public void setLevel(Level pLevel) {
		super.setLevel(pLevel);
		this.parts.values().forEach(v -> v.setLevel(pLevel));
	}
	
	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		CompoundTag parts = pTag.getCompound("Parts");
		this.parts.clear();
		for (var key : parts.getAllKeys()) {
			int id = Integer.parseInt(key);
			VirtualBlock<Block, BlockEntity> virtualBlock = 
					VirtualBlock.deserialize(this::getBlockPos, parts.getCompound(key));
			if (this.level != null) virtualBlock.setLevel(this.level);
			this.parts.put(id, virtualBlock);
		}
		this.setChanged();
	}
	
	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = new CompoundTag();
		saveAdditional(nbt);
		return nbt;
	}
	
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.load(tag);
	}
	
	@Override
	public CompoundPart[] getVisualParts() {
		return new CompoundPart[0];
	}
	
}
