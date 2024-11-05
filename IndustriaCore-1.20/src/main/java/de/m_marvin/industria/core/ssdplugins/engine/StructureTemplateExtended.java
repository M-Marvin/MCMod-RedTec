package de.m_marvin.industria.core.ssdplugins.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureTemplateExtended extends StructureTemplate implements IStructureTemplateExtended {
	
	@Override
	public BlockPos fillFromLevelPosIterable(Level pLevel, Iterable<BlockPos> pIterator, Block pToIgnore) {
		return ((IStructureTemplateExtended) this).fillFromLevelPosIterable(pLevel, pIterator, pToIgnore);
	}
	
}
