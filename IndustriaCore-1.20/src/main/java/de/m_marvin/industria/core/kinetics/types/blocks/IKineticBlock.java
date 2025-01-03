package de.m_marvin.industria.core.kinetics.types.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IKineticBlock {

	public enum ContactType {
		SHAFT,
		GEAR,
		BELT;
	}
	
	public float getTransmition(Level level, BlockPos pos, BlockState state, BlockPos otherPos, BlockState otherState, ContactType type);
	
}
