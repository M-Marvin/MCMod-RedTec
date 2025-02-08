package de.m_marvin.industria.core.kinetics.types.blocks;

import java.util.List;

import de.m_marvin.industria.core.kinetics.types.blockentities.CompoundBlockEntity;
import de.m_marvin.industria.core.magnetism.MagnetismUtility;
import de.m_marvin.industria.core.magnetism.types.blocks.IMagneticBlock;
import de.m_marvin.industria.core.registries.Blocks;
import de.m_marvin.industria.core.util.MathUtility;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import de.m_marvin.industria.core.util.types.StateTransform;
import de.m_marvin.industria.core.util.virtualblock.VirtualBlock;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;

public class CompoundBlock extends BaseEntityBlock implements IKineticBlock, IMagneticBlock {
	
	public static final EnumProperty<StateTransform> TRANSFORM = Blocks.PROP_TRANSFORM;
	
	public static final VoxelShape DEFAULT_SHAPE = VoxelShapeUtility.box(1, 1, 1, 15, 15, 15);
	
	public CompoundBlock(Properties pProperties) {
		super(pProperties);
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(TRANSFORM);
	}
	
	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(TRANSFORM, StateTransform.of(pRotation));
	}
	
	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.setValue(TRANSFORM, StateTransform.of(pMirror));
	}
	
	@Override
	public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
		if (level.getBlockEntity(pos) instanceof CompoundBlockEntity blockEntity) {
			blockEntity.applyTransform();
		}
		super.onBlockStateChange(level, pos, oldState, newState);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public TransmissionNode[] getTransmissionNodes(LevelAccessor level, BlockPos pos, BlockState state) {
		if (level.getBlockEntity(pos) instanceof CompoundBlockEntity blockEntity) {
			return blockEntity.getTransmissionNodes();
		}
		return new TransmissionNode[0];
	}

	@Override
	public BlockState getPartState(LevelAccessor level, BlockPos pos, int partId, BlockState state) {
		if (level.getBlockEntity(pos) instanceof CompoundBlockEntity blockEntity) {
			return blockEntity.getPartState(partId);
		}
		return state;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new CompoundBlockEntity(pPos, pState);
	}
	
	
	/* Block Function Redirects  */

	@Override
	public Vec3d getFieldVector(Level level, BlockState state, BlockPos blockPos) {
		if (level != null && level.getBlockEntity(blockPos) instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> MagnetismUtility.getBlockField(p.getLevel(), p.getState(), p.getPos()))
				.reduce((a, b) -> a.add(b)).orElse(new Vec3d(0, 0, 0));
		}
		return new Vec3d(0, 0, 0);
	}
	
	@Override
	public double getCoefficient(Level level, BlockState state, BlockPos pos) {
		if (level != null && level.getBlockEntity(pos) instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> MagnetismUtility.getBlockCoefficient(p.getLevel(), p.getState(), p.getPos()))
				.reduce((a, b) -> a * b).orElse(0.0);
		}
		return 0.0;
	}
	
	@Override
	public void onInductionNotify(Level level, BlockState state, BlockPos pos, Vec3d inductionVector) {
		// TODO Auto-generated method stub
		IMagneticBlock.super.onInductionNotify(level, state, pos, inductionVector);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().canSurvive(p.getState(), p.getLevel(), p.getPos()))
				.reduce((a, b) -> a && b).orElse(true);
		}
		return super.canSurvive(pState, pLevel, pPos);
	}
	
	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
		if (world.getBlockEntity(pos) instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().canSustainPlant(p.getState(), p.getLevel(), p.getPos(), facing, plantable))
				.reduce((a, b) -> a && b).orElse(true);
		}
		return super.canSustainPlant(state, world, pos, facing, plantable);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			for (var p : compound.getParts().values()) {
				BlockState state = p.getState().updateShape(pDirection, pNeighborState, p.getLevel(), p.getPos(), pNeighborPos);
				if (state.equals(p.getState())) continue;
				if (state.isAir()) {
					p.getLevel().destroyBlock(p.getPos(), true);
				} else {
					p.getLevel().setBlock(p.getPos(), state, 3);
				}
			}
		}
		return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
	}
	
	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		ClipContext clip = MathUtility.getPlayerPOVClipContext(level, player, Fluid.ANY, player.getBlockReach());
		BlockHitResult hit = level.clip(clip);
		if (hit.getType() == Type.MISS) return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
		if (level.getBlockEntity(pos) instanceof CompoundBlockEntity compound) {
			for (VirtualBlock<Block, BlockEntity> part : compound.getParts().values()) {
				VoxelShape shape = part.getState().getShape(part.getLevel(), part.getPos());
				BlockHitResult hit2 = shape.clip(clip.getFrom(), clip.getTo(), part.getPos());
				if (hit2 != null && hit2.getType() != Type.MISS && hit.getLocation().distanceTo(hit2.getLocation()) < 0.01) {
					
					Block bblock = part.getBlock();
					BlockState bstate = part.getState();
					BlockEntity bentity = part.getBlockEntity();
					
					if (part.getBlock().onDestroyedByPlayer(part.getState(), part.getLevel(), part.getPos(), player, willHarvest, fluid) && !player.isCreative())
						bblock.playerDestroy(part.getLevel(), player, part.getPos(), bstate, bentity, player.getMainHandItem());
					
					if (compound.isEmpty()) {
						level.removeBlock(pos, false);
					}
					
					return false;
					
				}
			}
		}
		return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
	}
	
	@Override
	public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult hit, Projectile pProjectile) {}
	
	@Override
	public boolean isSignalSource(BlockState pState) {
		return true;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState pState, net.minecraft.world.level.storage.loot.LootParams.Builder pParams) {
		BlockEntity blockEntity = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
					.flatMap(part -> {
						LootParams.Builder lootparams$builder = (new LootParams.Builder(pParams.getLevel()))
								.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(compound.getBlockPos()))
								.withParameter(LootContextParams.BLOCK_STATE, part.getState())
								.withOptionalParameter(LootContextParams.BLOCK_ENTITY, part.getBlockEntity())
								.withOptionalParameter(LootContextParams.THIS_ENTITY, pParams.getOptionalParameter(LootContextParams.THIS_ENTITY))
								.withParameter(LootContextParams.TOOL, pParams.getParameter(LootContextParams.TOOL));
						return part.getState().getDrops(lootparams$builder).stream();
					})
					.toList();
		}
		return super.getDrops(pState, pParams);
	}
	
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		ClipContext clip = MathUtility.getPlayerPOVClipContext(level, player, Fluid.ANY, player.getBlockReach());
		BlockHitResult hit = level.clip(clip);
		if (hit.getType() == Type.MISS) return super.getCloneItemStack(state, target, level, pos, player);
		if (level.getBlockEntity(pos) instanceof CompoundBlockEntity compound) {
			for (VirtualBlock<Block, BlockEntity> part : compound.getParts().values()) {
				VoxelShape shape = part.getState().getShape(part.getLevel(), part.getPos());
				BlockHitResult hit2 = shape.clip(clip.getFrom(), clip.getTo(), part.getPos());
				if (hit2 != null && hit2.getType() != Type.MISS && hit.getLocation().distanceTo(hit2.getLocation()) < 0.01) {
					return part.getBlock().getCloneItemStack(part.getState(), hit, part.getLevel(), part.getPos(), player);
				}
			}
		}
		return super.getCloneItemStack(state, target, level, pos, player);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
		ClipContext clip = MathUtility.getPlayerPOVClipContext(pLevel, pPlayer, Fluid.ANY, pPlayer.getBlockReach());
		BlockHitResult hit = pLevel.clip(clip);
		if (hit.getType() == Type.MISS) return super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			for (VirtualBlock<Block, BlockEntity> part : compound.getParts().values()) {
				VoxelShape shape = part.getState().getShape(part.getLevel(), part.getPos());
				BlockHitResult hit2 = shape.clip(clip.getFrom(), clip.getTo(), part.getPos());
				if (hit2 != null && hit2.getType() != Type.MISS && hit.getLocation().distanceTo(hit2.getLocation()) < 0.01) {
					return part.getBlock().getDestroyProgress(part.getState(), pPlayer, part.getLevel(), part.getPos());
				}
			}
		}
		return super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
		if (level.getBlockEntity(pos) instanceof CompoundBlockEntity compound) {
			return (float) compound.getParts().values().stream()
				.mapToDouble(p -> p.getBlock().getExplosionResistance(state, level, pos, explosion))
				.max().orElse(-1);
		}
		return super.getExplosionResistance(state, level, pos, explosion);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		ClipContext clip = MathUtility.getPlayerPOVClipContext(pLevel, pPlayer, Fluid.ANY, pPlayer.getBlockReach());
		BlockHitResult hit = pLevel.clip(clip);
		if (hit.getType() == Type.MISS) return InteractionResult.PASS;
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			for (VirtualBlock<Block, BlockEntity> part : compound.getParts().values()) {
				VoxelShape shape = part.getState().getShape(part.getLevel(), part.getPos());
				BlockHitResult hit2 = shape.clip(clip.getFrom(), clip.getTo(), part.getPos());
				if (hit2 != null && hit2.getType() != Type.MISS && hit.getLocation().distanceTo(hit2.getLocation()) < 0.01) {
					return part.getBlock().use(part.getState(), part.getLevel(), part.getPos(), pPlayer, pHand, pHit);
				}
			}
		}
		return InteractionResult.PASS;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
		ClipContext clip = MathUtility.getPlayerPOVClipContext(pLevel, pPlayer, Fluid.ANY, pPlayer.getBlockReach());
		BlockHitResult hit = pLevel.clip(clip);
		if (hit.getType() == Type.MISS) return;
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			for (VirtualBlock<Block, BlockEntity> part : compound.getParts().values()) {
				VoxelShape shape = part.getState().getShape(part.getLevel(), part.getPos());
				BlockHitResult hit2 = shape.clip(clip.getFrom(), clip.getTo(), part.getPos());
				if (hit2 != null && hit2.getType() != Type.MISS && hit.getLocation().distanceTo(hit2.getLocation()) < 0.01) {
					part.getBlock().attack(part.getState(), part.getLevel(), part.getPos(), pPlayer);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			compound.getParts().values().forEach(part -> {
				try {
					part.getBlock().onRemove(part.getState(), part.getLevel(), part.getPos(), pNewState.is(this) ? part.getState() : pNewState, pMovedByPiston);
				} catch (Throwable e) {
					// Ignore this, this means the block does stuff that is not supported by the fake level, so we can't do much anyway ...
					// Should not happen with the mod blocks (gears, shafts, etc)
				}
			});
		}
		super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			compound.getParts().values().stream()
				.forEach(p -> p.getBlock().neighborChanged(p.getState(), p.getLevel(), p.getPos(), pNeighborBlock, pNeighborPos, pMovedByPiston));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().triggerEvent(p.getState(), p.getLevel(), p.getPos(), pId, pParam))
				.reduce((a, b) -> a || b).orElse(false);
		}
		return super.triggerEvent(pState, pLevel, pPos, pId, pParam);
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
				.map(p -> p.getBlock().getOcclusionShape(p.getState(), p.getLevel(), p.getPos()))
				.reduce(Shapes::or).orElseGet(() -> super.getOcclusionShape(pState, pLevel, pPos));
		}
		return DEFAULT_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			VoxelShape shape = compound.getParts().values().stream()
				.map(p -> p.getBlock().getBlockSupportShape(p.getState(), p.getLevel(), p.getPos()))
				.reduce(Shapes::or).orElseGet(() -> super.getBlockSupportShape(pState, pLevel, pPos));
			return shape.isEmpty() ? DEFAULT_SHAPE : shape;
		}
		return DEFAULT_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			VoxelShape shape = compound.getParts().values().stream()
				.map(p -> p.getBlock().getInteractionShape(p.getState(), p.getLevel(), p.getPos()))
				.reduce(Shapes::or).orElseGet(() -> super.getInteractionShape(pState, pLevel, pPos));
			return shape.isEmpty() ? DEFAULT_SHAPE : shape;
		}
		return DEFAULT_SHAPE;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
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
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			compound.getParts().values().stream()
				.mapToInt(p -> p.getBlock().getAnalogOutputSignal(p.getState(), p.getLevel(), p.getPos()))
				.max().orElse(0);
		}
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			VoxelShape shape = compound.getParts().values().stream()
				.map(p -> p.getBlock().getShape(p.getState(), p.getLevel(), p.getPos(), pContext))
				.reduce(Shapes::or).orElseGet(() -> DEFAULT_SHAPE);
			return shape.isEmpty() ? DEFAULT_SHAPE : shape;
		}
		return DEFAULT_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			VoxelShape shape = compound.getParts().values().stream()
				.map(p -> p.getBlock().getCollisionShape(p.getState(), p.getLevel(), p.getPos(), pContext))
				.reduce(Shapes::or).orElseGet(() -> super.getCollisionShape(pState, pLevel, pPos, pContext));
			return shape.isEmpty() ? DEFAULT_SHAPE : shape;
		}
		return DEFAULT_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			VoxelShape shape = compound.getParts().values().stream()
				.map(p -> p.getBlock().getVisualShape(p.getState(), p.getLevel(), p.getPos(), pContext))
				.reduce(Shapes::or).orElseGet(() -> super.getVisualShape(pState, pLevel, pPos, pContext));
			return shape.isEmpty() ? DEFAULT_SHAPE : shape;
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
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			compound.getParts().values().stream()
				.forEach(p -> p.getBlock().tick(p.getState(), (ServerLevel) p.getLevel(), p.getPos(), pRandom));
		}
		pLevel.setBlock(pPos, pState.setValue(TRANSFORM, StateTransform.NONE), 3);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
				.mapToInt(p -> p.getBlock().getSignal(p.getState(), p.getLevel(), p.getPos(), pDirection))
				.max().orElse(0);
		}
		return 0;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
		if (pLevel.getBlockEntity(pPos) instanceof CompoundBlockEntity compound) {
			return compound.getParts().values().stream()
				.mapToInt(p -> p.getBlock().getDirectSignal(p.getState(), p.getLevel(), p.getPos(), pDirection))
				.max().orElse(0);
		}
		return 0;
	}
	
}
