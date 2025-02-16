package de.m_marvin.industria.core.kinetics.engine.transmission;

import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionType;
import de.m_marvin.industria.core.util.types.DiagonalDirection;
import net.minecraft.core.BlockPos;

public abstract class BeltTransmissions implements IKineticBlock.TransmissionType {

	public static final TransmissionType BELT = new BeltTransmission();
	public static final TransmissionType ATTACHMENT = new BeltAxle(true);
	public static final TransmissionType AXLE = new BeltAxle(false);
	
	public static class BeltTransmission extends BeltTransmissions {

		private BeltTransmission() {}
		
		@Override
		public double apply(TransmissionNode a, TransmissionNode b) {
			if (a.type() != BELT || b.type() != BELT) return 0.0;
			if (a.offset() != b.offset()) return 0.0;
			if (a.axis() != b.axis()) return 0.0;
			if (a.arg() instanceof DiagonalDirection da && b.arg() instanceof DiagonalDirection db && da.getOposite() != db) return 0.0;
			return 1.0;
		}
		
		@Override
		public BlockPos[] pos(TransmissionNode n) {
			if (n.arg() instanceof DiagonalDirection d)
				return new BlockPos[] { n.pos().offset(d.getNormal().x, d.getNormal().y, d.getNormal().z) };
			return new BlockPos[] {};
		}
		
	}
	
	public static class BeltAxle extends BeltTransmissions {
		
		protected final boolean insert;
		
		public BeltAxle(boolean insert) {
			this.insert = insert;
		}

		@Override
		public double apply(TransmissionNode a, TransmissionNode b) {
			if (a.offset() != b.offset()) return 0.0;
			if (b.axis() != a.axis()) return 0.0;
			if (!a.pos().equals(b.pos())) return 0.0;
			if (a.type() instanceof BeltAxle typeA && b.type() instanceof BeltAxle typeB) {
				if (typeA.insert == typeB.insert) return 0.0;
				return a.ratio() / b.ratio();
			}
			return 0.0;
		}

		@Override
		public BlockPos[] pos(TransmissionNode n) {
			return new BlockPos[] { n.pos() };
		}
		
	}
	
}
