package de.m_marvin.industria.core.kinetics.engine.transmission;

import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;

public class ShaftTransmission implements IKineticBlock.TransmissionType {
	
	public static final IKineticBlock.TransmissionType SHAFT = new ShaftTransmission();
	
	private ShaftTransmission() {}
	
	@Override
	public double apply(TransmissionNode a, TransmissionNode b) {
		if (a.type() != SHAFT || b.type() != SHAFT) return 0.0;
		if (a.axis() != b.axis()) return 0.0;
		if (a.arg() instanceof AxisDirection da && !a.pos().relative(Direction.fromAxisAndDirection(a.axis(), da)).equals(b.pos())) return 0.0;
		if (b.arg() instanceof AxisDirection db && !b.pos().relative(Direction.fromAxisAndDirection(b.axis(), db)).equals(a.pos())) return 0.0;
		return 1.0;
	}
	
	@Override
	public BlockPos[] pos(TransmissionNode n) {
		if (n.arg() instanceof AxisDirection d) {
			return new BlockPos[] {
				n.pos().relative(Direction.fromAxisAndDirection(n.axis(), d))
			};
		} else {
			return new BlockPos[] {
				n.pos().relative(Direction.fromAxisAndDirection(n.axis(), AxisDirection.POSITIVE)),
				n.pos().relative(Direction.fromAxisAndDirection(n.axis(), AxisDirection.NEGATIVE))
			};
		}
		
	}
	
}
