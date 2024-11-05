package de.m_marvin.industria.core.ssdplugins.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface IStructureTemplateExtended {
	
	public BlockPos fillFromLevelPosIterable(Level pLevel, Iterable<BlockPos> pIterator, Block pToIgnore);
	
}
