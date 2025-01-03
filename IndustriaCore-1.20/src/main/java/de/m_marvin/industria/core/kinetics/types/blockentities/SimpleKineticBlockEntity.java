package de.m_marvin.industria.core.kinetics.types.blockentities;

import de.m_marvin.industria.core.registries.BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleKineticBlockEntity extends BlockEntity implements IKineticBlockEntity {

	protected int rpm;
	
	public SimpleKineticBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityTypes.SIMPLE_KINETIC.get(), pPos, pBlockState);
	}

	@Override
	public void setRPM(int rpm) {
		this.rpm = rpm;
		this.setChanged();
	}

	@Override
	public int getRPM() {
		return this.rpm;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("RPM", this.rpm);
		return tag;
	}
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.load(tag);
	}
	
	@Override
	public void load(CompoundTag pTag) {
		this.rpm = pTag.getInt("RPM");
	}
	
	@Override
	protected void saveAdditional(CompoundTag pTag) {
		pTag.putInt("RPM", this.rpm);
	}
	
}
