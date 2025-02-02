package de.m_marvin.industria.core.util.types.virtualblock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.fml.unsafe.UnsafeHacks;

class ClientLevelRedirect extends ClientLevel {

	private ClientLevelRedirect() {
		super(null, null, null, null, 0, 0, null, null, false, 0);
		this.level = null;
		this.block = null;
	}
	
	public static ClientLevelRedirect newRedirect(VirtualBlock<?, ?> virtualBlock, ClientLevel level) {
		try {
			ClientLevelRedirect redirect = UnsafeHacks.newInstance(ClientLevelRedirect.class);
			Field levelField = ClientLevelRedirect.class.getDeclaredField("level");
			Field blockField = ClientLevelRedirect.class.getDeclaredField("block");
			levelField.setAccessible(true);
			blockField.setAccessible(true);
			levelField.set(redirect, level);
			blockField.set(redirect, virtualBlock);
			return redirect;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Failed to construct ClientLevelRedirect using Unsafe!", e);
		}
	}

	private VirtualBlock<?,?> block;
	private ClientLevel level;
	
	@Override
	public String toString() {
		return "Virtual" + this.level.toString();
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pPos) {
		if (pPos.equals(block.getPos())) 
			return block.blockEntity;
		return level.getBlockEntity(pPos);
	}

	@Override
	public BlockState getBlockState(BlockPos p_45571_) {
		if (p_45571_.equals(block.getPos()))
			return block.state;
		return level.getBlockState(p_45571_);
	}

	@Override
	public boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState) {
		if (pPos.equals(block.getPos()))
			return pState.test(block.state);
		return level.isStateAtPosition(pPos, pState);
	}

	@Override
	public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
		if (pPos.equals(block.getPos())) {
			BlockState rstate = level.getBlockState(pPos);
			level.sendBlockUpdated(pPos, rstate, rstate, pFlags);
			return true;
		}
		return level.setBlock(pPos, pState, pFlags);
	}
	
	@Override
	public boolean removeBlock(BlockPos pPos, boolean pIsMoving) {
		if (pPos.equals(block.getPos())) {
			block.setBlock(Blocks.AIR.defaultBlockState());
			BlockState rstate = level.getBlockState(pPos);
			level.sendBlockUpdated(pPos, rstate, rstate, 3);
			return true;
		}
		return level.removeBlock(pPos, pIsMoving);
	}

	@Override
	public boolean destroyBlock(BlockPos pPos, boolean pDropBlock, Entity pEntity, int pRecursionLeft) {
		
		if (pPos.equals(block.getPos())) {
			/* copied from Level class to call redirected methods */
			BlockState blockstate = this.getBlockState(pPos);
			if (blockstate.isAir()) {
				return false;
			} else {
				FluidState fluidstate = this.getFluidState(pPos);
				if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
					this.levelEvent(2001, pPos, Block.getId(blockstate));
				}

				if (pDropBlock) {
					BlockEntity blockentity = blockstate.hasBlockEntity() ? this.getBlockEntity(pPos) : null;
					Block.dropResources(blockstate, level, pPos, blockentity, pEntity, ItemStack.EMPTY);
				}

				boolean flag = this.setBlock(pPos, fluidstate.createLegacyBlock(), 3, pRecursionLeft);
				if (flag) {
					this.gameEvent(GameEvent.BLOCK_DESTROY, pPos, GameEvent.Context.of(pEntity, blockstate));
				}

				return flag;
			}
		}
		
		return level.destroyBlock(pPos, pDropBlock, pEntity, pRecursionLeft);
	}

	@Override
	public FluidState getFluidState(BlockPos pPos) {
		return level.getFluidState(pPos);
	}

	@Override
	public boolean isFluidAtPosition(BlockPos pPos, Predicate<FluidState> pPredicate) {
		return level.isFluidAtPosition(pPos, pPredicate);
	}

	@Override
	public List<Entity> getEntities(Entity pEntity, AABB pArea, Predicate<? super Entity> pPredicate) {
		return level.getEntities(pEntity, pArea, pPredicate);
	}

	@Override
	public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntityTypeTest, AABB pBounds,
			Predicate<? super T> pPredicate) {
		return level.getEntities(pEntityTypeTest, pBounds, pPredicate);
	}

	@Override
	public List<AbstractClientPlayer> players() {
		return level.players();
	}

	@Override
	public ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
		return level.getChunk(pX, pZ, pRequiredStatus, pNonnull);
	}

	@Override
	public int getHeight(Types pHeightmapType, int pX, int pZ) {
		return level.getHeight(pHeightmapType, pX, pZ);
	}

	@Override
	public int getSkyDarken() {
		return level.getSkyDarken();
	}

	@Override
	public BiomeManager getBiomeManager() {
		return level.getBiomeManager();
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
		return level.getUncachedNoiseBiome(pX, pY, pZ);
	}

	@Override
	public boolean isClientSide() {
		return level.isClientSide();
	}

	@Override
	public int getSeaLevel() {
		return level.getSeaLevel();
	}

	@Override
	public DimensionType dimensionType() {
		return level.dimensionType();
	}

	@Override
	public RegistryAccess registryAccess() {
		return level.registryAccess();
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return level.enabledFeatures();
	}

	@Override
	public float getShade(Direction pDirection, boolean pShade) {
		return level.getShade(pDirection, pShade);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return level.getLightEngine();
	}

	@Override
	public WorldBorder getWorldBorder() {
		return level.getWorldBorder();
	}

	@Override
	public long nextSubTickCount() {
		return level.nextSubTickCount();
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return level.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return level.getFluidTicks();
	}

	@Override
	public ClientLevelData getLevelData() {
		return level.getLevelData();
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos pPos) {
		return level.getCurrentDifficultyAt(pPos);
	}

	@Override
	public MinecraftServer getServer() {
		return level.getServer();
	}

	@Override
	public ClientChunkCache getChunkSource() {
		return level.getChunkSource();
	}

	@Override
	public RandomSource getRandom() {
		return level.getRandom();
	}

	@Override
	public void playSound(Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pSource, float pVolume,
			float pPitch) {
		level.playSound(pPlayer, pPos, pSound, pSource, pVolume, pPitch);
	}

	@Override
	public void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed,
			double pYSpeed, double pZSpeed) {
		level.addParticle(pParticleData, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}

	@Override
	public void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData) {
		level.levelEvent(pPlayer, pType, pPos, pData);
	}

	@Override
	public void gameEvent(GameEvent pEvent, Vec3 pPosition, Context pContext) {
		level.gameEvent(pEvent, pPosition, pContext);
	}

	@Override
	public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
		level.sendBlockUpdated(pPos, pOldState, pNewState, pFlags);
	}

	@Override
	public void playSeededSound(Player pPlayer, double pX, double pY, double pZ, Holder<SoundEvent> pSound,
			SoundSource pSource, float pVolume, float pPitch, long pSeed) {
		level.playSeededSound(pPlayer, pX, pY, pZ, pSound, pSource, pVolume, pPitch, pSeed);
	}

	@Override
	public void playSeededSound(Player pPlayer, Entity pEntity, Holder<SoundEvent> pSound, SoundSource pCategory,
			float pVolume, float pPitch, long pSeed) {
		level.playSeededSound(pPlayer, pEntity, pSound, pCategory, pVolume, pPitch, pSeed);
	}

	@Override
	public String gatherChunkSourceStats() {
		return level.gatherChunkSourceStats();
	}

	@Override
	public Entity getEntity(int pId) {
		return level.getEntity(pId);
	}

	@Override
	public MapItemSavedData getMapData(String pMapName) {
		return level.getMapData(pMapName);
	}

	@Override
	public void setMapData(String pMapName, MapItemSavedData pData) {
		level.setMapData(pMapName, pData);
	}

	@Override
	public int getFreeMapId() {
		return level.getFreeMapId();
	}

	@Override
	public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {
		level.destroyBlockProgress(pBreakerId, pPos, pProgress);
	}

	@Override
	public Scoreboard getScoreboard() {
		return level.getScoreboard();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return level.getRecipeManager();
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return null;
	}
	
}