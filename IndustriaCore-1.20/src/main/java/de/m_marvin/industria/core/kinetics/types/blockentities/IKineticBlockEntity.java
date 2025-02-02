package de.m_marvin.industria.core.kinetics.types.blockentities;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public interface IKineticBlockEntity {
	
	public static final double DEFAULT_ROTATIONAL_OFFSET = 11.25F / 360F * Math.PI * 2;
	
	public static record CompoundPart(BlockState state, Axis rotationAxis, double axialOffset, double rotationRatio) {}
	
	public void setRPM(int partId, int rpm);
	public int getRPM(int partId);
	
	public default int getTorque() {
		return 0;
	}
	
	public default CompoundPart[] getVisualParts() {
		return new CompoundPart[] {
				new CompoundPart(
						this.getBlockState(), 
						this.getBlockState().getValue(BlockStateProperties.AXIS), 
						DEFAULT_ROTATIONAL_OFFSET, 
						1.0)
		};
	}
	
	// Overridden by BlockEntity
	public BlockState getBlockState();
	
}
 