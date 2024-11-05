package de.m_marvin.industria.core.ssdplugins.engine;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureDataPlugin<T extends Tag> {

	@FunctionalInterface
	public static interface IWorldFillAreaFunction<T> {
		public Optional<T> fillFromWorldArea(Level level, BlockPos origin, Vec3i size, VanillaTemplateData vanillaData);
	}

	@FunctionalInterface
	public static interface IWorldFillIterableFunction<T> {
		public Optional<T> fillFromWorldPosIterable(Level level, Iterable<BlockPos> posItr, VanillaTemplateData vanillaData);
	}
	
	@FunctionalInterface
	public static interface IWorldPlaceFunction<T> {
		public boolean placeInWorld(ServerLevel level, BlockPos offset, StructurePlaceSettings settings, RandomSource random, int flags, VanillaTemplateData vanillaData, T pluginData);
	}
	
	public static BlockPos calculateRelativePosition(StructurePlaceSettings pDecorator, BlockPos pPos) {
		return StructureTemplate.calculateRelativePosition(pDecorator, pPos);
	}

	public static record VanillaTemplateData(
			List<StructureTemplate.Palette> blockPalettes,
			List<StructureTemplate.StructureEntityInfo> entityInfo,
			Vec3i size) {}
	
	private final IWorldFillAreaFunction<T> fillAreaFunc;
	private final IWorldFillIterableFunction<T> fillIterableFunc;
	private final IWorldPlaceFunction<T> placeFunc;
	
	public StructureDataPlugin(IWorldFillAreaFunction<T> fillAreaFunc, IWorldFillIterableFunction<T> fillIterableFunc, IWorldPlaceFunction<T> placeFunc) {
		this.fillAreaFunc = fillAreaFunc;
		this.fillIterableFunc = fillIterableFunc;
		this.placeFunc = placeFunc;
	}
	
	public IWorldFillAreaFunction<T> getFillAreaFunc() {
		return fillAreaFunc;
	}
	
	public IWorldFillIterableFunction<T> getFillIterableFunc() {
		return fillIterableFunc;
	}
	
	public IWorldPlaceFunction<T> getPlaceFunc() {
		return placeFunc;
	}
	
}
