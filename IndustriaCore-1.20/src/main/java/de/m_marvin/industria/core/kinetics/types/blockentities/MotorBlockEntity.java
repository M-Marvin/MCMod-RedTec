package de.m_marvin.industria.core.kinetics.types.blockentities;

import de.m_marvin.industria.core.kinetics.types.blocks.ShaftBlock;
import de.m_marvin.industria.core.registries.BlockEntityTypes;
import de.m_marvin.industria.core.registries.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MotorBlockEntity extends SimpleKineticBlockEntity {
	
	public MotorBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityTypes.MOTOR.get(), pPos, pBlockState);
	}
	
	@Override
	public CompoundPart[] getVisualParts() {
		Axis axis = this.getBlockState().getValue(BlockStateProperties.FACING).getAxis();
		return new CompoundPart[] {
				new CompoundPart(
						Blocks.SHAFT.get().defaultBlockState().setValue(ShaftBlock.AXIS, axis), 
						axis, 
						DEFAULT_ROTATIONAL_OFFSET, 
						1.0),
				new CompoundPart(
						getBlockState(), 
						axis, 
						0.0, 
						0.0)
		};
	}
	
}
