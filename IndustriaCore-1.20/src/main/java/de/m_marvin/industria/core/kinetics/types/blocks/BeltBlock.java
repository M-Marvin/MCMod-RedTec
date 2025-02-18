package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.compound.types.blockentities.CompoundBlockEntity;
import de.m_marvin.industria.core.compound.types.blocks.CompoundBlock;
import de.m_marvin.industria.core.kinetics.types.blockentities.BeltBlockEntity;
import de.m_marvin.industria.core.registries.Blocks;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import de.m_marvin.industria.core.util.types.DiagonalDirection;
import de.m_marvin.industria.core.util.types.DiagonalPlanarDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeltBlock extends BaseEntityBlock implements IKineticBlock {
	
	public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;
	public static final EnumProperty<DiagonalPlanarDirection> ORIENTATION = Blocks.PROP_PLANAR_ORIENTATION;
	public static final BooleanProperty IS_END = Blocks.PROP_IS_END;
	
	public BeltBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(AXIS);
		pBuilder.add(ORIENTATION);
		pBuilder.add(IS_END);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		
		final VoxelShape SHAPE_STRAIGHT = Shapes.or(VoxelShapeUtility.box(1, 3, 0, 15, 4, 16), VoxelShapeUtility.box(1, 12, 0, 15, 13, 16));
		final VoxelShape SHAPE_STREIGHT_END = Shapes.or(VoxelShapeUtility.box(1, 3, 0, 15, 4, 16), VoxelShapeUtility.box(1, 12, 0, 15, 13, 16));
		
		VoxelShape shape = pState.getValue(IS_END) ? SHAPE_STREIGHT_END : SHAPE_STRAIGHT;
		
		Axis axis = pState.getValue(AXIS);
		
		return VoxelShapeUtility.transformation()
				.centered()
				.rotateFromAxisX(axis)
				.uncentered()
				.transform(shape);
	}
	
	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		DiagonalDirection direction1 = DiagonalDirection.fromPlanarAndAxis(pState.getValue(ORIENTATION), pState.getValue(AXIS));
		DiagonalDirection direction2 = direction1.getOposite();
		
		BlockPos pos1 = pPos.offset(direction1.getNormal().x, direction1.getNormal().y, direction1.getNormal().z);
		boolean valid1 = CompoundBlock.performOnAllAndCombine(pLevel, pos1, 
				() -> isValidConnectedBelt(pLevel.getBlockState(pos1), direction2), 
				(compound, part) -> isValidConnectedBelt(part.getState(), direction2), 
				CompoundBlock::trueIfAny);
		
		if (!valid1) return false;
		
		if (!pState.getValue(IS_END)) {
			BlockPos pos2 = pPos.offset(direction2.getNormal().x, direction2.getNormal().y, direction2.getNormal().z);
			boolean valid2 = CompoundBlock.performOnAllAndCombine(pLevel, pos2, 
					() -> isValidConnectedBelt(pLevel.getBlockState(pos2), direction1), 
					(compound, part) -> isValidConnectedBelt(part.getState(), direction1), 
					CompoundBlock::trueIfAny);
			
			return valid2;
		}
		
		return true;
	}
	
	public boolean isValidConnectedBelt(BlockState pState, DiagonalDirection direction) {
		if (pState.getBlock() instanceof BeltBlock) {
			DiagonalDirection d = DiagonalDirection.fromPlanarAndAxis(pState.getValue(ORIENTATION), pState.getValue(AXIS));
			if (direction == d) return true;
			if (direction == d.getOposite() && !pState.getValue(IS_END)) return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {		
		DiagonalDirection direction1 = DiagonalDirection.fromPlanarAndAxis(pState.getValue(ORIENTATION), pState.getValue(AXIS));
		BlockPos pos1 = pPos.offset(direction1.getNormal().x, direction1.getNormal().y, direction1.getNormal().z);
		DiagonalDirection direction2 = direction1.getOposite();
		BlockPos pos2 = pPos.offset(direction2.getNormal().x, direction2.getNormal().y, direction2.getNormal().z);
		
		if (pNeighborPos.equals(pos1) || (!pState.getValue(IS_END) && pNeighborPos.equals(pos2)))
			pLevel.scheduleTick(pPos, this, 1);
		
		super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
		updateDiagonalBelts(pState, pLevel, pPos);
		super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
	}
	
	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (!canSurvive(pState, pLevel, pPos))
			pLevel.destroyBlock(pPos, true);
	}
	
	public void updateDiagonalBelts(BlockState pState, Level pLevel, BlockPos pPos) {
		DiagonalDirection direction1 = DiagonalDirection.fromPlanarAndAxis(pState.getValue(ORIENTATION), pState.getValue(AXIS));
		BlockPos pos1 = pPos.offset(direction1.getNormal().x, direction1.getNormal().y, direction1.getNormal().z);
		pLevel.scheduleTick(pos1, this, 1);
		
		if (!pState.getValue(IS_END)) {
			DiagonalDirection direction2 = direction1.getOposite();
			BlockPos pos2 = pPos.offset(direction2.getNormal().x, direction2.getNormal().y, direction2.getNormal().z);
			pLevel.scheduleTick(pos2, this, 1);
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new BeltBlockEntity(pPos, pState);
	}

	@Override
	public TransmissionNode[] getTransmissionNodes(LevelAccessor level, BlockPos pos, BlockState state) {
		Axis axis = state.getValue(AXIS);
		DiagonalPlanarDirection orientation = state.getValue(ORIENTATION);
		DiagonalDirection connectDirection = DiagonalDirection.fromPlanarAndAxis(orientation, axis);
		if (state.getValue(IS_END)) {
			return new TransmissionNode[] {
				new TransmissionNode(KineticReference.simple(pos), pos, 1.0, axis, null, connectDirection, BELT),
				new TransmissionNode(KineticReference.simple(pos), pos, 1.0, axis, null, null, BELT_ATTACHMENT)
			};
		} else {
			return new TransmissionNode[] {
				new TransmissionNode(KineticReference.simple(pos), pos, 1.0, axis, null, connectDirection, BELT),
				new TransmissionNode(KineticReference.simple(pos), pos, 1.0, axis, null, connectDirection.getOposite(), BELT),
				new TransmissionNode(KineticReference.simple(pos), pos, 1.0, axis, null, null, BELT_ATTACHMENT)
			};
		}
	}

}
