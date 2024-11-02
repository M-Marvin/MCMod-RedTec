package de.m_marvin.industria.core.util.mixin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.m_marvin.industria.core.conduits.engine.ConduitHandlerCapability;
import de.m_marvin.industria.core.conduits.types.ConduitPos;
import de.m_marvin.industria.core.conduits.types.blocks.IConduitConnector;
import de.m_marvin.industria.core.conduits.types.conduits.ConduitEntity;
import de.m_marvin.industria.core.registries.Capabilities;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.industria.core.util.MathUtility;
import de.m_marvin.industria.core.util.StructureTemplateExtended;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateExtender implements StructureTemplateExtended {
	
	@Shadow
	private final List<StructureTemplate.Palette> palettes = Lists.newArrayList();
	@Shadow
	private final List<StructureTemplate.StructureEntityInfo> entityInfoList = Lists.newArrayList();
	@Shadow
	private Vec3i size = Vec3i.ZERO;
	private final List<CompoundTag> conduitData = Lists.newArrayList();
	
	@Shadow
	public static BlockPos calculateRelativePosition(StructurePlaceSettings pDecorator, BlockPos pPos) { return null; }

	@Shadow
	private static void addToLists(StructureTemplate.StructureBlockInfo pBlockInfo, List<StructureTemplate.StructureBlockInfo> pNormalBlocks, List<StructureTemplate.StructureBlockInfo> pBlocksWithNbt, List<StructureTemplate.StructureBlockInfo> pBlocksWithSpecialShape) {}
	
	@Shadow
	private static List<StructureTemplate.StructureBlockInfo> buildInfoList(List<StructureTemplate.StructureBlockInfo> pNormalBlocks, List<StructureTemplate.StructureBlockInfo> pBlocksWithNbt, List<StructureTemplate.StructureBlockInfo> pBlocksWithSpecialShape) { return null; };
	
	@Override
	public BlockPos fillFromLevelPosIterable(Level pLevel, Iterable<BlockPos> pIterator, Block pToIgnore) {
		
		Optional<BlockPos> blockpos1o = StreamSupport.stream(pIterator.spliterator(), false).reduce(MathUtility::getMinCorner);
		Optional<BlockPos> blockpos2o = StreamSupport.stream(pIterator.spliterator(), false).reduce(MathUtility::getMaxCorner);
		
		if (blockpos1o.isPresent() && blockpos2o.isPresent()) {

			List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
			List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();
			List<StructureTemplate.StructureBlockInfo> list2 = Lists.newArrayList();
			BlockPos blockpos1 = blockpos1o.get();
			BlockPos blockpos2 = blockpos2o.get();
			
			this.size = blockpos2.subtract(blockpos1).offset(1, 1, 1);
			for(BlockPos blockpos3 : pIterator) {
				BlockPos blockpos4 = blockpos3.subtract(blockpos1);
				
				BlockState blockstate = pLevel.getBlockState(blockpos3);
				if (pToIgnore == null || !blockstate.is(pToIgnore)) {
				   BlockEntity blockentity = pLevel.getBlockEntity(blockpos3);
				   StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo;
				   if (blockentity != null) {
					  structuretemplate$structureblockinfo = new StructureTemplate.StructureBlockInfo(blockpos4, blockstate, blockentity.saveWithId());
				   } else {
					  structuretemplate$structureblockinfo = new StructureTemplate.StructureBlockInfo(blockpos4, blockstate, (CompoundTag)null);
				   }
				   addToLists(structuretemplate$structureblockinfo, list, list1, list2);
				}
			 }

			List<StructureTemplate.StructureBlockInfo> list3 = buildInfoList(list, list1, list2);
			this.palettes.clear();
			
			try {
				Constructor<Palette> cnstr = StructureTemplate.Palette.class.getDeclaredConstructor(List.class);
				cnstr.setAccessible(true);
				this.palettes.add(cnstr.newInstance(list3));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			
			this.entityInfoList.clear();
			
			fillInConduits(pLevel, blockpos1, blockpos2);
			
		}
		
		return blockpos1o.get();
		
	}
	
	@Inject(at = @At("RETURN"), method = "fillFromWorld(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Vec3i;ZLnet/minecraft/world/level/block/Block;)V")
	private void fillFromWorld( Level pLevel, BlockPos pPos, Vec3i pSize, boolean pWithEntities, @Nullable Block pToIgnore, CallbackInfo callback) {
		
		if (pSize.getX() >= 1 && pSize.getY() >= 1 && pSize.getZ() >= 1) {
			
			BlockPos opositePos = pPos.offset(pSize).offset(-1, -1, -1);
			BlockPos minPos = new BlockPos(Math.min(pPos.getX(), opositePos.getX()), Math.min(pPos.getY(), opositePos.getY()), Math.min(pPos.getZ(), opositePos.getZ()));
			BlockPos maxPos = new BlockPos(Math.max(pPos.getX(), opositePos.getX()), Math.max(pPos.getY(), opositePos.getY()), Math.max(pPos.getZ(), opositePos.getZ()));
			
			fillInConduits(pLevel, minPos, maxPos);
		}
		
	}
	
	private void fillInConduits(Level pLevel, BlockPos minPos, BlockPos maxPos) {

		List<ConduitEntity> conduitsInBounds = Lists.newArrayList();
		for (StructureTemplate.Palette palette : this.palettes) {
			
			ConduitHandlerCapability handler = GameUtility.getLevelCapability(pLevel, Capabilities.CONDUIT_HANDLER_CAPABILITY);
			
			palette.blocks().stream()
				.flatMap(s -> {
					if (s.state().getBlock() instanceof IConduitConnector) {
						return handler.getConduitsAtBlock(s.pos().offset(minPos)).stream();
					}
					return Stream.of();
				})
				.forEach(conduitsInBounds::add);
			
			
		}
		
		this.conduitData.clear();
		conduitsInBounds.stream()
			.distinct()
			.filter(c -> {
				BlockPos np1 = c.getPosition().getNodeApos();
				BlockPos np2 = c.getPosition().getNodeBpos();
				return MathUtility.isBetweenInclusive(minPos, maxPos, np1) && MathUtility.isBetweenInclusive(minPos, maxPos, np2);
			})
			.map(c -> c.save(minPos))
			.forEach(this.conduitData::add);
		
	}
	
	@Inject(at = @At("RETURN"), method = "placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z")
	private void placeInWorld(ServerLevelAccessor pServerLevel, BlockPos pOffset, BlockPos pPos, StructurePlaceSettings pSettings, RandomSource pRandom, int pFlags, CallbackInfoReturnable<Boolean> callback) {
		
		if (callback.getReturnValue() && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1 && !this.conduitData.isEmpty()) {
			
			ServerLevel level = pServerLevel.getLevel();
			ConduitHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.CONDUIT_HANDLER_CAPABILITY);
			
			for (CompoundTag conduitNbt : this.conduitData) {
				
				ConduitEntity conduitEntity = ConduitEntity.load(conduitNbt);
				BlockPos np1 = calculateRelativePosition(pSettings, conduitEntity.getPosition().getNodeApos()).offset(pOffset);
				BlockPos np2 = calculateRelativePosition(pSettings, conduitEntity.getPosition().getNodeBpos()).offset(pOffset);
				ConduitPos position = new ConduitPos(np1, np2, conduitEntity.getPosition().getNodeAid(), conduitEntity.getPosition().getNodeBid());
				
				Optional<ConduitEntity> replacedConduitEntity = handler.getConduit(position);
				if (replacedConduitEntity.isPresent() && replacedConduitEntity.get().getConduit() != conduitEntity.getConduit()) {
					handler.removeConduit(replacedConduitEntity.get());
				}
				
				GameUtility.triggerClientSync(level, np1);
				GameUtility.triggerClientSync(level, np2);

				GameUtility.triggerUpdate(level, np1);
				GameUtility.triggerUpdate(level, np2);
				
				if (handler.placeConduit(position, conduitEntity.getConduit(), conduitEntity.getLength())) {
					Optional<ConduitEntity> placedConduitEntity = handler.getConduit(position);
					if (placedConduitEntity.isPresent()) {
						placedConduitEntity.get().loadAdditional(conduitNbt);
					}
				}
				
			}
			
		}
		
	}
	
	@Inject(at = @At("RETURN"), method = "save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;")
	private void save(CompoundTag pTag, CallbackInfoReturnable<CompoundTag> callback) {
		ListTag conduitsNbt = new ListTag();
		this.conduitData.forEach(conduitsNbt::add);
		pTag.put("conduits", conduitsNbt);
	}
	
	@Inject(at = @At("RETURN"), method = "load(Lnet/minecraft/core/HolderGetter;Lnet/minecraft/nbt/CompoundTag;)V")
	private void load(HolderGetter<Block> pBlockGetter, CompoundTag pTag, CallbackInfo callback) {
		this.conduitData.clear();
		ListTag conduitsNbt = pTag.getList("conduits", 10);
		conduitsNbt.stream().forEach(t -> this.conduitData.add((CompoundTag) t));
	}
	
}
