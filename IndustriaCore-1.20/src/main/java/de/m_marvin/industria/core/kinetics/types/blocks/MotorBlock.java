package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.kinetics.types.blockentities.MotorBlockEntity;
import de.m_marvin.industria.core.registries.Tags;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MotorBlock extends BaseEntityBlock implements IKineticBlock {
	// TODO incomplete, just for testing
	
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	
	public static final VoxelShape SHAPE = Shapes.or(VoxelShapeUtility.box(6, 0, 6, 10, 16, 10), VoxelShapeUtility.box(3, 1, 3, 13, 15, 13));
	
	public MotorBlock(Properties pProperties) {
		super(pProperties);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public TransmissionNode[] getTransmissionNodes(LevelAccessor level, BlockPos pos, BlockState state) {
		return new TransmissionNode[] {
				new TransmissionNode(KineticReference.simple(pos), pos, 1.0, state.getValue(FACING).getAxis(), null, SHAFT)
		};
	}
	
	@Override
	public double getTorque(LevelAccessor level, BlockPos pos, int partId, BlockState state) {
		return 100;
	}
	
	@Override
	public int getSourceSpeed(LevelAccessor level, BlockPos pos, int partId, BlockState state) {
		return 128;
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING);
	}
	
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return VoxelShapeUtility.transformation()
				.centered()
				.rotateFromAxisY(pState.getValue(FACING).getAxis())
				.uncentered()
				.transform(SHAPE);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		Direction facing = pContext.getClickedFace();
		for (Direction d : Direction.values()) {
			BlockState state = pContext.getLevel().getBlockState(pContext.getClickedPos().relative(d));
			if (state.is(Tags.Blocks.KINETICS)) {
				// TODO placement helper
			}
		}
		return this.defaultBlockState().setValue(FACING, facing);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new MotorBlockEntity(pPos, pState);
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
	}
	
	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.setValue(FACING, pMirror.mirror(pState.getValue(FACING)));
	}
	
}
