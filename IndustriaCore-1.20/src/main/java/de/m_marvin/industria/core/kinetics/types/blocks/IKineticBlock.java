package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.kinetics.engine.transmission.GearTransmissions;
import de.m_marvin.industria.core.kinetics.engine.transmission.ShaftTransmission;
import de.m_marvin.industria.core.kinetics.types.blockentities.IKineticBlockEntity;
import de.m_marvin.industria.core.util.types.AxisOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface IKineticBlock {
	
	public static interface TransmissionType {
		public double apply(TransmissionNode a, TransmissionNode b);
		public BlockPos[] pos(TransmissionNode n);
	}
	
	public static record KineticReference(
			BlockPos pos, 
			int partId
	) {
		
		public BlockState state(Level level) {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof IKineticBlock block && partId > 0) {
				return block.getState(level, pos, partId, state);
			}
			return state;
		}
		
		public static KineticReference simple(BlockPos pos) {
			return new KineticReference(pos, 0);
		}
		
		public static KineticReference subPart(BlockPos pos, int partId) {
			return new KineticReference(pos, partId);
		}
		
		public CompoundTag serialize() {
			CompoundTag nbt = new CompoundTag();
			nbt.put("Position", NbtUtils.writeBlockPos(pos));
			nbt.putInt("PartId", partId);
			return nbt;
		}
		
		public static KineticReference deserialize(CompoundTag nbt) {
			BlockPos position = NbtUtils.readBlockPos(nbt.getCompound("Position"));
			int partId = nbt.getInt("PartId");
			return new KineticReference(position, partId);
		}
		
	}
	
	/**
	 * @param reference The reference to the main block to which this node belongs
	 * @param pos Position of this node in the world
	 * @param ratio The RPM ratio for this transmission node
	 * @param axis The axis of rotation of this node
	 * @param offset Where the node is placed along the axis (front/center/back)
	 * @param type The type of transmission of this node
	 */
	public static record TransmissionNode(
			KineticReference reference,
			BlockPos pos, 
			double ratio, 
			Axis axis, 
			AxisOffset offset, 
			TransmissionType type
	) {
		public TransmissionNode withReference(KineticReference reference) {
			return new TransmissionNode(reference, pos, ratio, axis, offset, type);
		}
	}

	/** Shaft which connects in both directions **/
	public static final TransmissionType SHAFT = ShaftTransmission.SHAFT;
	/** Shaft which connects only in the axis positive direction **/
	public static final TransmissionType SHAFT_POS = ShaftTransmission.SHAFT_POS;
	/** Shaft which connects only in the axis negative direction **/
	public static final TransmissionType SHAFT_NEG = ShaftTransmission.SHAFT_NEG;
	/** Gear which connects on an 90 degree orthogonal axis to other gears of the same type **/
	public static final TransmissionType GEAR_ANGLE = GearTransmissions.GEAR_ANGLE;
	/** Gear which connects diagonally on the same plane to gears with same axis **/
	public static final TransmissionType GEAR_DIAG = GearTransmissions.GEAR_DIAG;
	/** Gear which connects only to touching neighbor gears with the same axis **/
	public static final TransmissionType GEAR = GearTransmissions.GEAR;
	
	public TransmissionNode[] getTransmissionNodes(LevelAccessor level, BlockPos pos, BlockState state);

	public default BlockState getState(LevelAccessor level, BlockPos pos, int partId, BlockState state) {
		return level.getBlockState(pos);
	}
	
	public default int getSourceSpeed(LevelAccessor level, BlockPos pos, int partId, BlockState state) {
		return 0;
	}
	
	public default double getTorque(LevelAccessor level, BlockPos pos, int partId, BlockState state) {
		return 0.0;
	}

	public default void setRPM(LevelAccessor level, BlockPos pos, int partId, BlockState state, int rpm) {
		if (level.getBlockEntity(pos) instanceof IKineticBlockEntity kinetic)
			kinetic.setRPM(partId, rpm);
	}

	public default int getRPM(LevelAccessor level, BlockPos pos, int partId, BlockState state) {
		if (level.getBlockEntity(pos) instanceof IKineticBlockEntity kinetic)
			return kinetic.getRPM(partId);
		return 0;
	}
	
}
