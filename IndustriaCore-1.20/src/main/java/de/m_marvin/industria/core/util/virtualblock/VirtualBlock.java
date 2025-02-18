package de.m_marvin.industria.core.util.virtualblock;

import java.util.function.Supplier;

import com.google.common.base.Objects;

import de.m_marvin.industria.core.util.types.StateTransform;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VirtualBlock {
	
	protected final Supplier<BlockPos> pos;
	protected Level level;
	protected Block block;
	protected BlockState state;
	protected BlockEntity blockEntity;
	protected Runnable stateChangeEvent;
	
	public VirtualBlock(Supplier<BlockPos> pos) {
		this.pos = pos;
		this.block = Blocks.AIR;
		this.state = Blocks.AIR.defaultBlockState();
	}
	
	public void setStateChangeEvent(Runnable stateChangeEvent) {
		this.stateChangeEvent = stateChangeEvent;
	}
	
	public void setLevel(Level level) {
		if (level.isClientSide()) {
			this.level = ClientLevelRedirect.newRedirect(this, (ClientLevel) level);
		} else {
			this.level = ServerLevelRedirect.newRedirect(this, (ServerLevel) level);
		}
		if (this.blockEntity != null)
			this.blockEntity.setLevel(this.level);
	}
	
	public BlockPos getPos() {
		return pos.get();
	}
	
	public Level getLevel() {
		return this.level;
	}
	
	@Override
	public String toString() {
		return String.format("VirtualBlock[BlockState=%s]", state.toString());
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.block, this.state, this.blockEntity);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof VirtualBlock other) {
			return	this.block == other.block &&
					this.state.equals(other.state) &&
					Objects.equal(this.blockEntity, other.blockEntity);
		}
		return false;
	}
	
	public void setBlock(BlockState state) {
		try {
			this.block = state.getBlock();
			this.state = state;
			if (this.state.hasBlockEntity() && block instanceof EntityBlock entityBlock) {
				this.blockEntity = entityBlock.newBlockEntity(getPos(), state);
				this.blockEntity.setLevel(this.level);
			} else {
				this.blockEntity = null;
			}
			if (this.stateChangeEvent != null)
				this.stateChangeEvent.run();
		} catch (Throwable e) {
			throw new IllegalArgumentException("illegal candiate for virtual block: " + state.toString(), e);
		}
	}
	
	public void setBlockEntity(BlockEntity blockEntity) {
		this.blockEntity = blockEntity;
		if (this.blockEntity != null)
			this.blockEntity.setLevel(getLevel());
	}
	
	public Block getBlock() {
		return block;
	}
	
	public BlockState getState() {
		return state;
	}
	
	public BlockEntity getBlockEntity() {
		return blockEntity;
	}
	
	public CompoundTag serialize() {
		CompoundTag nbt = new CompoundTag();
		nbt.put("State", NbtUtils.writeBlockState(this.state));
		if (this.blockEntity != null) {
			nbt.put("BlockEntity", this.blockEntity.serializeNBT());
		}
		return nbt;
	}
	
	public static VirtualBlock deserialize(Supplier<BlockPos> pos, CompoundTag nbt) {
		@SuppressWarnings("deprecation")
		BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("State"));
		VirtualBlock virtualBlock = new VirtualBlock(pos);
		virtualBlock.setBlock(state);
		if (virtualBlock.blockEntity != null) {
			virtualBlock.blockEntity.deserializeNBT(nbt.getCompound("BlockEntity"));
		}
		return virtualBlock;
	}
	
	public void mirror(Mirror mirror) {
		BlockState oldState = getState();
		BlockState newState = oldState.mirror(mirror);
		if (!oldState.equals(newState)) {
			CompoundTag tag = null;
			if (oldState.hasBlockEntity() && this.blockEntity != null)
				tag = this.blockEntity.serializeNBT();
			setBlock(newState);
			if (newState.hasBlockEntity() && this.blockEntity != null && tag != null)
				this.blockEntity.deserializeNBT(tag);
		}
	}

	public void rotate(Rotation rotation) {
		BlockState oldState = getState();
		BlockState newState = oldState.rotate(getLevel(), getPos(), rotation);
		if (!oldState.equals(newState)) {
			CompoundTag tag = null;
			if (oldState.hasBlockEntity() && this.blockEntity != null)
				tag = this.blockEntity.serializeNBT();
			setBlock(newState);
			if (newState.hasBlockEntity() && this.blockEntity != null && tag != null)
				this.blockEntity.deserializeNBT(tag);
		}
	}
	
	public void transform(StateTransform transform) {
		if (transform.getRotation() != Rotation.NONE)
			rotate(transform.getRotation());
		if (transform.getMirror() != Mirror.NONE)
			mirror(transform.getMirror());
	}
	
}
