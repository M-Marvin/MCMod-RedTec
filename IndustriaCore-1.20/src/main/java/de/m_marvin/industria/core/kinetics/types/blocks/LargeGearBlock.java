package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.kinetics.types.blockentities.SimpleKineticBlockEntity;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.KineticReference;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import de.m_marvin.industria.core.registries.Blocks;
import de.m_marvin.industria.core.registries.Tags;
import de.m_marvin.industria.core.util.MathUtility;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import de.m_marvin.industria.core.util.types.AxisOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LargeGearBlock extends BaseEntityBlock implements IKineticBlock {
	
	public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;
	public static final EnumProperty<AxisOffset> POS = Blocks.PROP_GEAR_POS;
	
	public static final VoxelShape SHAPE = VoxelShapeUtility.box(-4, 6, -4, 20, 10, 20);
	
	public LargeGearBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(AXIS, POS);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		int offset = pState.getValue(POS) == AxisOffset.CENTER ? 0 : pState.getValue(POS) == AxisOffset.FRONT ? +5 : -5;
		return VoxelShapeUtility.transformation()
				.centered()
				.offset(0, offset, 0)
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
		return this.defaultBlockState().setValue(AXIS, axis).setValue(POS, AxisOffset.CENTER);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new SimpleKineticBlockEntity(pPos, pState, 5.625F / 360F * Math.PI * 2);
	}
	
	@Override
	public TransmissionNode[] getTransmissionNodes(LevelAccessor level, BlockPos pos, BlockState state) {
		AxisOffset offset = state.getValue(POS);
		if (offset == AxisOffset.CENTER) {
			return new TransmissionNode[] {
					new TransmissionNode(KineticReference.simple(pos), pos, 2.0, state.getValue(AXIS), AxisOffset.CENTER, GEAR_DIAG),
					new TransmissionNode(KineticReference.simple(pos), pos, 2.0, state.getValue(AXIS), AxisOffset.CENTER, GEAR_ANGLE),
					new TransmissionNode(KineticReference.simple(pos), pos, 1.0, state.getValue(AXIS), AxisOffset.CENTER, ATTACHMENT)
			};
		} else {
			return new TransmissionNode[] {
					new TransmissionNode(KineticReference.simple(pos), pos, 2.0, state.getValue(AXIS), offset, GEAR_DIAG),
					new TransmissionNode(KineticReference.simple(pos), pos, 1.0, state.getValue(AXIS), offset, ATTACHMENT)
			};
		}
		
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		if (pState.getValue(POS) == AxisOffset.CENTER) {
			return pState.setValue(AXIS, MathUtility.rotate(pRotation, pState.getValue(AXIS)));
		} else {
			Direction d = Direction.fromAxisAndDirection(pState.getValue(AXIS), pState.getValue(POS).getAxisDirection());
			d = pRotation.rotate(d);
			return pState.setValue(AXIS, d.getAxis()).setValue(POS, AxisOffset.fromAxisDirection(d.getAxisDirection()));
		}
	}
	
	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		if (pState.getValue(POS) == AxisOffset.CENTER) {
			return pState;
		} else {
			return pState.setValue(POS, pState.getValue(POS) == AxisOffset.BACK ? AxisOffset.FRONT : AxisOffset.BACK);
		}
	}
	
}
