package de.m_marvin.industria.core.kinetics.types.blocks;

import java.util.Collections;
import java.util.List;

import de.m_marvin.industria.core.kinetics.types.blockentities.CompoundKineticBlockEntity;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import de.m_marvin.industria.core.util.types.virtualblock.VirtualBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CompoundKineticBlock extends BaseEntityBlock implements IKineticBlock {
	
	public static final VoxelShape DEFAULT_SHAPE = VoxelShapeUtility.box(1, 1, 1, 15, 15, 15);
	
	public CompoundKineticBlock(Properties pProperties) {
		super(pProperties);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public TransmissionNode[] getTransmissionNodes(LevelAccessor level, BlockPos pos, BlockState state) {
		if (level.getBlockEntity(pos) instanceof CompoundKineticBlockEntity blockEntity) {
			return blockEntity.getTransmissionNodes();
		}
		return new TransmissionNode[0];
	}

	@Override
	public BlockState getPartState(LevelAccessor level, BlockPos pos, int partId, BlockState state) {
		if (level.getBlockEntity(pos) instanceof CompoundKineticBlockEntity blockEntity) {
			return blockEntity.getPartState(partId);
		}
		return state;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new CompoundKineticBlockEntity(pPos, pState);
	}
	
	
	
	
	/* Block Function Redirects  */
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		// TODO depends on aim
		return InteractionResult.PASS;
	}

	@Override
	public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
		// TODO depends on aim
	}

	@Override
	public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
		// TODO depends on aim
		float f = pState.getDestroySpeed(pLevel, pPos);
		if (f == -1.0F) {
			return 0.0F;
		} else {
			int i = net.minecraftforge.common.ForgeHooks.isCorrectToolForDrops(pState, pPlayer) ? 30 : 100;
			return pPlayer.getDigSpeed(pState, pPos) / f / (float)i;
		}
	}

	@Override
	public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
		// TODO depends on aim
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
		if (pState.hasBlockEntity() && (!pState.is(pNewState.getBlock()) || !pNewState.hasBlockEntity())) {
			pLevel.removeBlockEntity(pPos);
		}

	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		// FIXME compound rotation
		return pState;
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		// FIXME compound rotation
		return pState;
	}

//	@Override
//	public boolean isSignalSource(BlockState pState) {
//		return false;
//	}
//
//	@Override
//	public boolean hasAnalogOutputSignal(BlockState pState) {
//		return false;
//	}
//	
//	@Override
//	public abstract Item asItem();
//
//	@Override
//	protected abstract Block asBlock();
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			compound.getParts().values().stream()
				.forEach(p -> p.getBlock().neighborChanged(p.getState(), p.getLevel(), p.getPos(), pNeighborBlock, pNeighborPos, pMovedByPiston));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().triggerEvent(p.getState(), p.getLevel(), p.getPos(), pId, pParam))
				.reduce((a, b) -> a || b).orElse(false);
		}
		return super.triggerEvent(pState, pLevel, pPos, pId, pParam);
	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
		ResourceLocation resourcelocation = this.getLootTable();
		if (resourcelocation == BuiltInLootTables.EMPTY) {
			return Collections.emptyList();
		} else {
			LootParams lootparams = pParams.withParameter(LootContextParams.BLOCK_STATE, pState).create(LootContextParamSets.BLOCK);
			ServerLevel serverlevel = lootparams.getLevel();
			LootTable loottable = serverlevel.getServer().getLootData().getLootTable(resourcelocation);
			return loottable.getRandomItems(lootparams);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().getOcclusionShape(p.getState(), p.getLevel(), p.getPos()))
				.reduce(Shapes::or).orElseGet(() -> super.getOcclusionShape(pState, pLevel, pPos));
		}
		return DEFAULT_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().getBlockSupportShape(p.getState(), p.getLevel(), p.getPos()))
				.reduce(Shapes::or).orElseGet(() -> super.getBlockSupportShape(pState, pLevel, pPos));
		}
		return DEFAULT_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().getInteractionShape(p.getState(), p.getLevel(), p.getPos()))
				.reduce(Shapes::or).orElseGet(() -> super.getInteractionShape(pState, pLevel, pPos));
		}
		return DEFAULT_SHAPE;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			for (VirtualBlock<Block, BlockEntity> p : compound.getParts().values()) {
				if (p.getBlock().propagatesSkylightDown(p.getState(), p.getLevel(), p.getPos())) return true;
			}
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			compound.getParts().values().stream()
				.mapToInt(p -> p.getBlock().getAnalogOutputSignal(p.getState(), p.getLevel(), p.getPos()))
				.max().orElse(0);
		}
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().getShape(p.getState(), p.getLevel(), p.getPos(), pContext))
				.reduce(Shapes::or).orElseGet(() -> super.getShape(pState, pLevel, pPos, pContext));
		}
		return DEFAULT_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().getCollisionShape(p.getState(), p.getLevel(), p.getPos(), pContext))
				.reduce(Shapes::or).orElseGet(() -> super.getCollisionShape(pState, pLevel, pPos, pContext));
		}
		return DEFAULT_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().getVisualShape(p.getState(), p.getLevel(), p.getPos(), pContext))
				.reduce(Shapes::or).orElseGet(() -> super.getVisualShape(pState, pLevel, pPos, pContext));
		}
		return DEFAULT_SHAPE;
	}

	@Override
	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		this.tick(pState, pLevel, pPos, pRandom);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			compound.getParts().values().stream()
				.forEach(p -> p.getBlock().tick(p.getState(), (ServerLevel) p.getLevel(), p.getPos(), pRandom));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			compound.getParts().values().stream()
				.mapToInt(p -> p.getBlock().getSignal(p.getState(), p.getLevel(), p.getPos(), pDirection))
				.max().orElse(0);
		}
		return 0;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundKineticBlockEntity compound) {
			compound.getParts().values().stream()
				.mapToInt(p -> p.getBlock().getDirectSignal(p.getState(), p.getLevel(), p.getPos(), pDirection))
				.max().orElse(0);
		}
		return 0;
	}
	
}
