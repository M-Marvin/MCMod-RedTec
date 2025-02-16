 package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.util.VoxelShapeUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeltShaftBlock extends ShaftBlock {

	public static final VoxelShape SHAPE = Shapes.join(VoxelShapeUtility.box(4, 2, 4, 12, 14, 12), ShaftBlock.SHAPE, BooleanOp.ONLY_FIRST);
	
	public BeltShaftBlock(Properties pProperties) {
		super(pProperties);
	}
	
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return VoxelShapeUtility.transformation()
				.centered()
				.rotateFromAxisY(pState.getValue(AXIS))
				.uncentered()
				.transform(SHAPE);
	}
	
	@Override
	public TransmissionNode[] getTransmissionNodes(LevelAccessor level, BlockPos pos, BlockState state) {
		return new TransmissionNode[] {
			new TransmissionNode(KineticReference.simple(pos), pos, 1.0, state.getValue(AXIS), null, null, ATTACHMENT),
			new TransmissionNode(KineticReference.simple(pos), pos, 1.0, state.getValue(AXIS), null, null, BELT_AXLE)
		};
	}
	
}
