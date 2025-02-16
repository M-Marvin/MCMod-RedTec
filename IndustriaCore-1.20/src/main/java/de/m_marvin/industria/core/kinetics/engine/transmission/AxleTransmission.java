package de.m_marvin.industria.core.kinetics.engine.transmission;

import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionType;
import net.minecraft.core.BlockPos;

public class AxleTransmission implements IKineticBlock.TransmissionType {

	public static final TransmissionType ATTACHMENT = new AxleTransmission(true);
	public static final TransmissionType AXLE = new AxleTransmission(false);
	
	protected final boolean insert;
	
	public AxleTransmission(boolean insert) {
		this.insert = insert;
	}

	@Override
	public double apply(TransmissionNode a, TransmissionNode b) {
		if (a.offset() != null && b.offset() != null && a.offset() != b.offset()) return 0.0;
		if (b.axis() != a.axis()) return 0.0;
		if (!a.pos().equals(b.pos())) return 0.0;
		if (a.type() instanceof AxleTransmission typeA && b.type() instanceof AxleTransmission typeB) {
			if (typeA.insert == typeB.insert) return 0.0;
			return 1.0;
		}
		return 0.0;
	}

	@Override
	public BlockPos[] pos(TransmissionNode n) {
		return new BlockPos[] { n.pos() };
	}
	
}
