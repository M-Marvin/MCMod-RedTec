package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.kinetics.types.KineticContactPoint;
import de.m_marvin.industria.core.registries.Tags;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GearBlock extends Block implements IKineticBlock {
	
	public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;
	
	public static final VoxelShape SHAPE = Shapes.or(VoxelShapeUtility.box(6, 0, 6, 10, 16, 10), VoxelShapeUtility.box(0, 6, 0, 16, 10, 16));
	
	public GearBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public KineticContactPoint[] getContactPoints(Level level, BlockPos pos, BlockState state) {
		// TODO Auto-generated method stub
		return null;
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
	
}
