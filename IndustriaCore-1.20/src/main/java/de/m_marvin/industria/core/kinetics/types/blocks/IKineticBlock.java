package de.m_marvin.industria.core.kinetics.types.blocks;

import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import de.m_marvin.industria.core.util.MathUtility;
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
	
	public TransmissionNode[] getTransmitionNodes(Level level, BlockPos pos, BlockState state);
	
}
