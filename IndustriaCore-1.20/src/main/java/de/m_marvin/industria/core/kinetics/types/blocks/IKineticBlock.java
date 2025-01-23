package de.m_marvin.industria.core.kinetics.types.blocks;

import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import de.m_marvin.industria.core.kinetics.types.blockentities.IKineticBlockEntity;
import de.m_marvin.industria.core.util.MathUtility;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IKineticBlock {
	
	public static interface TransmissionType {
		public double apply(TransmissionNode a, TransmissionNode b);
		public BlockPos[] pos(TransmissionNode n);
	}
	
	/**
	 * @param pos Position of this node in the world
	 * @param state State of the main block of this kinetic component (can be different from the block at pos!)
	 * @param blockPos Position of the main block of this kinetic component (can be different from pos!)
	 * @param kinetic The Block instance of this kinetic component
	 * @param ratio The RPM ratio for this transmission node
	 * @param axis The axis of rotation of this node
	 * @param type The type of transmission of this node
	 */
	public static record TransmissionNode(BlockPos pos, BlockState state, BlockPos blockPos, IKineticBlock kinetic, double ratio, Axis axis, TransmissionType type) {}

	public static final TransmissionType SHAFT = new TransmissionType() {
		@Override
		public double apply(TransmissionNode a, TransmissionNode b) {
			if (a.type() != this || b.type() != this) return 0.0;
			if (a.axis() != b.axis()) return 0.0;
			if (a.pos().relative(Direction.fromAxisAndDirection(a.axis(), AxisDirection.POSITIVE)).equals(b.pos())) return 1.0;
			if (a.pos().relative(Direction.fromAxisAndDirection(a.axis(), AxisDirection.NEGATIVE)).equals(b.pos())) return 1.0;
			return 0.0;
		}
		@Override
		public BlockPos[] pos(TransmissionNode n) {
			return new BlockPos[] {
				n.pos().relative(Direction.fromAxisAndDirection(n.axis(), AxisDirection.POSITIVE)),
				n.pos().relative(Direction.fromAxisAndDirection(n.axis(), AxisDirection.NEGATIVE))
			};
		};
	};
	
	public static final TransmissionType GEAR_ANGLE = new TransmissionType() {
		public int vaxis(BlockPos vec, Axis axis) {
			switch (axis) {
			default:
			case X: return vec.getX();
			case Y: return vec.getY();
			case Z: return vec.getZ();
			}
		}
		@Override
		public double apply(TransmissionNode a, TransmissionNode b) {
			if (a.type() != GEAR_ANGLE || b.type() != GEAR_ANGLE) return 0.0;
			if (a.axis() == b.axis()) return 0.0;
			if (Stream.of(pos(a)).filter(p -> p.equals(b.pos())).count() == 0) return 0.0;
			if (Stream.of(pos(b)).filter(p -> p.equals(a.pos())).count() == 0) return 0.0;
			int ia = vaxis(b.pos(), b.axis()) - vaxis(a.pos(), b.axis());
			int ib = vaxis(a.pos(), a.axis()) - vaxis(b.pos(), a.axis());
			return -(ia * ib) * a.ratio() / b.ratio();
		}
		@Override
		public BlockPos[] pos(TransmissionNode n) {
			Vec3i v = Vec3i.fromVec(n.pos()).sub(Vec3i.fromVec(n.blockPos()));
			if (v.lengthSqrt() != 0) {
				Direction offset = MathUtility.getVecDirection(v);
				return new BlockPos[] {
					n.pos().relative(offset).relative(n.axis(), +1),
					n.pos().relative(offset).relative(n.axis(), -1)
				};
			} else {
				Direction[] offset = MathUtility.getDirectionsOrthogonal(n.axis());
				return new BlockPos[] {
					n.pos().relative(offset[0]).relative(n.axis(), +1),
					n.pos().relative(offset[0]).relative(n.axis(), -1),
					n.pos().relative(offset[1]).relative(n.axis(), +1),
					n.pos().relative(offset[1]).relative(n.axis(), -1),
					n.pos().relative(offset[2]).relative(n.axis(), +1),
					n.pos().relative(offset[2]).relative(n.axis(), -1),
					n.pos().relative(offset[3]).relative(n.axis(), +1),
					n.pos().relative(offset[3]).relative(n.axis(), -1)
				};
			}
		}
	};
	
	public static final TransmissionType GEAR_DIAG = new TransmissionType() {
		@Override
		public double apply(TransmissionNode a, TransmissionNode b) {
			if (a.type() == GEAR && b.type() == GEAR) {
				return GEAR.apply(a, b);
			} else if (a.type() == GEAR && b.type() == GEAR_DIAG) {
				if (a.axis() != b.axis()) return 0.0;
				if (Stream.of(gearPos(b)).filter(p -> p.equals(a.pos())).count() == 0) return 0.0;
				return -a.ratio() / b.ratio();
			} else if (a.type() == GEAR_DIAG && b.type() == GEAR) {
				if (a.axis() != b.axis()) return 0.0;
				if (Stream.of(gearPos(a)).filter(p -> p.equals(b.pos())).count() == 0) return 0.0;
				return -a.ratio() / b.ratio();
			}
			return 0.0;
		}
		public BlockPos[] gearPos(TransmissionNode n) {
			return MathUtility.getDiagonalPositionsAroundAxis(n.pos(), n.axis());
		}
		@Override
		public BlockPos[] pos(TransmissionNode n) {
			return ArrayUtils.addAll(gearPos(n), MathUtility.getPositionsAroundAxis(n.pos(), n.axis()));
		};
	};
	
	public static final TransmissionType GEAR = new TransmissionType() {
		@Override
		public double apply(TransmissionNode a, TransmissionNode b) {
			if (a.type() == GEAR && b.type() == GEAR) {
				if (a.axis() != b.axis()) return 0.0;
				if (Stream.of(gearPos(a)).filter(p -> p.equals(b.pos())).count() == 0) return 0.0;
				return -a.ratio() / b.ratio();
			} else if (a.type() == GEAR && b.type() == GEAR_DIAG || a.type() == GEAR_DIAG && b.type() == GEAR) {
				return GEAR_DIAG.apply(a, b);
			}
			return 0.0;
		}
		public BlockPos[] gearPos(TransmissionNode n) {
			return MathUtility.getPositionsAroundAxis(n.pos(), n.axis());
		}
		@Override
		public BlockPos[] pos(TransmissionNode n) {
			return ArrayUtils.addAll(gearPos(n), MathUtility.getDiagonalPositionsAroundAxis(n.pos(), n.axis()));
		};
	};
	
	public TransmissionNode[] getTransmissionNodes(Level level, BlockPos pos, BlockState state);
	
	public default int getSourceSpeed(Level level, BlockPos pos, BlockState state) {
		return 0;
	}
	
	public default double getTorque(Level level, BlockPos pos, BlockState state) {
		return 0.0;
	}
	
	public default void setRPM(Level level, BlockPos pos, BlockState state, int rpm) {
		if (level.getBlockEntity(pos) instanceof IKineticBlockEntity kinetic)
			kinetic.setRPM(rpm);
	}
	
	public default int getRPM(Level level, BlockPos pos, BlockState state) {
		if (level.getBlockEntity(pos) instanceof IKineticBlockEntity kinetic)
			return kinetic.getRPM();
		return 0;
	}
	
}
