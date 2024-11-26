package de.m_marvin.industria.core.kinetics.types.blocks;

import de.m_marvin.industria.core.kinetics.types.KineticContactPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IKineticBlock {

	public KineticContactPoint[] getContactPoints(Level level, BlockPos pos, BlockState state);
	
}
