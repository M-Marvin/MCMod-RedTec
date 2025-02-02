package de.m_marvin.industria.core.kinetics.engine.transmission;

import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;

public class ShaftTransmission implements IKineticBlock.TransmissionType {
	
	public static final IKineticBlock.TransmissionType SHAFT = new ShaftTransmission(true, true);
	public static final IKineticBlock.TransmissionType SHAFT_POS = new ShaftTransmission(true, false);
	public static final IKineticBlock.TransmissionType SHAFT_NEG = new ShaftTransmission(false, true);
	
	private final boolean connectPositive;
	private final boolean connectNegative;
	
	private ShaftTransmission(boolean connectPositive, boolean connectNegative) {
		this.connectNegative = connectNegative;
		this.connectPositive = connectPositive;
	}
	
	@Override
	public double apply(TransmissionNode a, TransmissionNode b) {
		if (a.type() instanceof ShaftTransmission typeA && b.type() instanceof ShaftTransmission typeB) {
			if (a.axis() != b.axis()) return 0.0;
			if (typeA.connectPositive && typeB.connectNegative && a.pos().relative(Direction.fromAxisAndDirection(a.axis(), AxisDirection.POSITIVE)).equals(b.pos())) return 1.0;
			if (typeA.connectNegative && typeB.connectPositive && a.pos().relative(Direction.fromAxisAndDirection(a.axis(), AxisDirection.NEGATIVE)).equals(b.pos())) return 1.0;
		}
		return 0.0;
	}
	
	@Override
	public BlockPos[] pos(TransmissionNode n) {
		if (this.connectPositive && this.connectNegative) {
			return new BlockPos[] {
					n.pos().relative(Direction.fromAxisAndDirection(n.axis(), AxisDirection.POSITIVE)),
					n.pos().relative(Direction.fromAxisAndDirection(n.axis(), AxisDirection.NEGATIVE))
				};
		} else if (this.connectNegative) {
			return new BlockPos[] {
					n.pos().relative(Direction.fromAxisAndDirection(n.axis(), AxisDirection.NEGATIVE))
				};
		} else {
			return new BlockPos[] {
					n.pos().relative(Direction.fromAxisAndDirection(n.axis(), AxisDirection.POSITIVE))
				};
		}
	}
	
}
