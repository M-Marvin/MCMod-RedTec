package de.m_marvin.industria.core.util.mixin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateExtender {
	
	@Shadow
	private final List<StructureTemplate.Palette> palettes = Lists.newArrayList();
	@Shadow
	private Vec3i size = Vec3i.ZERO;
	private final List<CompoundTag> conduitData = Lists.newArrayList();
	
	@Shadow
	public static BlockPos calculateRelativePosition(StructurePlaceSettings pDecorator, BlockPos pPos) { return null; }
	
	@Inject(at = @At("RETURN"), method = "fillFromWorld(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Vec3i;ZLnet/minecraft/world/level/block/Block;)V")
	private void fillFromWorld( Level pLevel, BlockPos pPos, Vec3i pSize, boolean pWithEntities, @Nullable Block pToIgnore, CallbackInfo callback) {
		
		if (pSize.getX() >= 1 && pSize.getY() >= 1 && pSize.getZ() >= 1) {
			
			BlockPos opositePos = pPos.offset(pSize).offset(-1, -1, -1);
			BlockPos minPos = new BlockPos(Math.min(pPos.getX(), opositePos.getX()), Math.min(pPos.getY(), opositePos.getY()), Math.min(pPos.getZ(), opositePos.getZ()));
			BlockPos maxPos = new BlockPos(Math.max(pPos.getX(), opositePos.getX()), Math.max(pPos.getY(), opositePos.getY()), Math.max(pPos.getZ(), opositePos.getZ()));
			
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
