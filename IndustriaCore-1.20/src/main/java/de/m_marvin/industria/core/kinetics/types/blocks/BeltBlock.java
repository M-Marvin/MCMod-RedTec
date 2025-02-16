package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.kinetics.types.blockentities.SimpleKineticBlockEntity;
import de.m_marvin.industria.core.registries.Blocks;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import de.m_marvin.industria.core.util.types.DiagonalDirection;
import de.m_marvin.industria.core.util.types.DiagonalPlanarDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
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
		return VoxelShapeUtility.transformation()
				.centered()
				.rotateFromAxisY(pState.getValue(AXIS))
				.uncentered()
				.transform(Shapes.join(VoxelShapeUtility.box(0, 0, 0, 16, 16, 16), Shapes.or(ShaftBlock.SHAPE, BeltShaftBlock.SHAPE), BooleanOp.ONLY_FIRST));
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new SimpleKineticBlockEntity(pPos, pState);
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
