package de.m_marvin.industria.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface StructureTemplateExtended {
	
	public BlockPos fillFromLevelPosIterable(Level pLevel, Iterable<BlockPos> pIterator, Block pToIgnore);
	
}
