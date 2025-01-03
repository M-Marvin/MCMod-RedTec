package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.kinetics.types.blockentities.SimpleKineticBlockEntity;
import de.m_marvin.industria.core.registries.Tags;
import de.m_marvin.industria.core.util.MathUtility;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GearBlock extends BaseEntityBlock implements IKineticBlock {
	
	public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;
	
	public static final VoxelShape SHAPE = Shapes.or(VoxelShapeUtility.box(6, 0, 6, 10, 16, 10), VoxelShapeUtility.box(0, 6, 0, 16, 10, 16));
	
	public GearBlock(Properties pProperties) {
		super(pProperties);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public float getTransmition(Level level, BlockPos pos, BlockState state, BlockPos otherPos, BlockState otherState, ContactType type) {
		if (MathUtility.rasterDistance(pos, otherPos) == 1) {
			Direction f = MathUtility.getPosRelativeFacing(pos, otherPos);
			if (f.getAxis() != state.getValue(AXIS) && type == ContactType.GEAR) {
				return -1;
			} else if (f.getAxis() == state.getValue(AXIS) && type == ContactType.SHAFT) {
				return 1;
			}
		}
		return 0;
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(AXIS);
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
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		Axis axis = pContext.getClickedFace().getAxis();
		for (Direction d : Direction.values()) {
			BlockState state = pContext.getLevel().getBlockState(pContext.getClickedPos().relative(d));
			if (state.is(Tags.Blocks.KINETICS)) {
				// TODO placement helper
			}
		}
		return this.defaultBlockState().setValue(AXIS, axis);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new SimpleKineticBlockEntity(pPos, pState);
	}
	
}
