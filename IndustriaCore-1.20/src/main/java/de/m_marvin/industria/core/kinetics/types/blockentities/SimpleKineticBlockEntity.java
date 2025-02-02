package de.m_marvin.industria.core.kinetics.types.blockentities;

import de.m_marvin.industria.core.registries.BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SimpleKineticBlockEntity extends BlockEntity implements IKineticBlockEntity {

	protected int rpm;
	
	protected final double rotationalOffset;

	public SimpleKineticBlockEntity(BlockPos pPos, BlockState pBlockState) {
		this(pPos, pBlockState, IKineticBlockEntity.DEFAULT_ROTATIONAL_OFFSET);
	}

	public SimpleKineticBlockEntity(BlockPos pPos, BlockState pBlockState, double rotationalOffset) {
		super(BlockEntityTypes.SIMPLE_KINETIC.get(), pPos, pBlockState);
		this.rotationalOffset = rotationalOffset;
	}

	public SimpleKineticBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		this(pType, pPos, pBlockState, IKineticBlockEntity.DEFAULT_ROTATIONAL_OFFSET);
	}
	
	public SimpleKineticBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState, double rotationalOffset) {
		super(pType, pPos, pBlockState);
		this.rotationalOffset = rotationalOffset;
	}
	
	@Override
	public CompoundPart[] getVisualParts() {
		Axis axis;
		if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
			axis = getBlockState().getValue(BlockStateProperties.FACING).getAxis();
		} else if (getBlockState().hasProperty(BlockStateProperties.AXIS)) {
			axis = getBlockState().getValue(BlockStateProperties.AXIS);
		} else {
			axis = Axis.Y;
		}
		
		return new CompoundPart[] {
				new CompoundPart(
						this.getBlockState(), 
						axis, 
						this.rotationalOffset, 
						1.0)
		};
	}
	
	@Override
	public void setRPM(int partId, int rpm) {
		this.rpm = rpm;
		this.setChanged();
	}

	@Override
	public int getRPM(int partId) {
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
