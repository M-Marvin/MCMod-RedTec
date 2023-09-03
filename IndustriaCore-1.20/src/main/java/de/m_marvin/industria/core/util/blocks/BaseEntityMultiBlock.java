package de.m_marvin.industria.core.util.blocks;

import java.util.List;
import java.util.stream.IntStream;

import de.m_marvin.industria.core.util.MathUtility;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;

public abstract class BaseEntityMultiBlock extends BaseEntityBlock {
	
	protected static IntegerProperty[] createMultiBlockProperties(int x, int y, int z) {
		return new IntegerProperty[] {
				x > 1 ? IntegerProperty.create("mbpos_x", 0, x - 1) : null,
				y > 1 ? IntegerProperty.create("mbpos_y", 0, y - 1) : null,
				z > 1 ? IntegerProperty.create("mbpos_z", 0, z - 1) : null
		};
	}
	
	protected final int width;
	protected final int height;
	protected final int depth;
	
	protected IntegerProperty[] mbposProperties;
	
	protected BaseEntityMultiBlock(Properties pProperties, int width, int height, int depth) {
		super(pProperties);
		if (width > 3 || height > 3 || depth > 3) throw new IllegalArgumentException("An multi-block canÄt be larger than 6x6x6!");
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	protected void addMultiBlockProperties(Builder<Block, BlockState> pBuilder, IntegerProperty[] multiBlockProperties) {
		this.mbposProperties = multiBlockProperties;
		if (this.mbposProperties[0] != null) pBuilder.add(this.mbposProperties[0]);
		if (this.mbposProperties[1] != null) pBuilder.add(this.mbposProperties[1]);
		if (this.mbposProperties[2] != null) pBuilder.add(this.mbposProperties[2]);
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.HORIZONTAL_FACING);
	}
	
	
	
	public Vec3i getMBPosAtIndex(int i) {
		return new Vec3i(
				i % getWidth(), 
				(i / (getWidth() * getDepth())) % getHeight(), 
				(i / getWidth()) % getDepth()
		);
	}
	
	public BlockPos[] getPlacementPositions(BlockPos center, Direction orientation) {
		int rotation = orientation.get2DDataValue() * -90;
		return IntStream.range(0, getWidth() * getHeight() * getDepth())
			.mapToObj(i -> 
				getMBPosAtIndex(i).sub(
						this.getWidth() / 2, 
						0, 
						this.getDepth() / 2
				)
			)
			.map(pos -> MathUtility.rotatePoint(pos, rotation, true, Axis.Y))
			.map(pos -> 
				new BlockPos(
						pos.x + center.getX(), 
						pos.y + center.getY(), 
						pos.z + center.getZ()
				)
			)
			.toArray(i -> new BlockPos[i]);
	}
	
	public boolean canPlace(BlockPlaceContext context, BlockPos[] positions) {
		for (BlockPos pos : positions) {
			BlockState state = context.getLevel().getBlockState(pos);
			if (!state.canBeReplaced(context)) return false;
		}
		return true;
	}
	
	public BlockState stateAt(Vec3i position, Direction orientation) {
		BlockState state = defaultBlockState();
		if (this.mbposProperties[0] != null) state = state.setValue(this.mbposProperties[0], position.getX());
		if (this.mbposProperties[1] != null) state = state.setValue(this.mbposProperties[1], position.getY());
		if (this.mbposProperties[2] != null) state = state.setValue(this.mbposProperties[2], position.getZ());
		return state.setValue(BlockStateProperties.HORIZONTAL_FACING, orientation);
	}
	
	public Vec3i getMBPos(BlockState state) {
		return new Vec3i(
				this.mbposProperties[0] != null ? state.getValue(this.mbposProperties[0]) : 0,
				this.mbposProperties[1] != null ? state.getValue(this.mbposProperties[1]) : 0,
				this.mbposProperties[2] != null ? state.getValue(this.mbposProperties[2]) : 0
		);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		Direction facing = pContext.getHorizontalDirection().getOpposite();
		BlockPos centerBlock = pContext.getClickedPos();
		BlockPos[] positions = getPlacementPositions(centerBlock, facing);
		
		if (!canPlace(pContext, positions)) return null;
		
		Vec3i centerBlockMBPos = new Vec3i(getWidth() / 2, 0, getDepth() / 2);
		return stateAt(centerBlockMBPos, facing);
	}
	
	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		BlockPos[] positions = getPlacementPositions(pPos, pState.getValue(BlockStateProperties.HORIZONTAL_FACING));
		for (int i = 0; i < positions.length; i++) {
			BlockPos placementPos = positions[i];
			BlockState state = pLevel.getBlockState(placementPos);
			if (state.canBeReplaced()) {
				Vec3i mbPos = getMBPosAtIndex(i);
				pLevel.setBlockAndUpdate(placementPos, stateAt(mbPos, pState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
			}
		}
	}
	
	public BlockPos getCenterBlock(BlockPos pos, BlockState state) {
		Vec3i mbPos = getMBPos(state);
		Direction orientation = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		Vec3i mbOffset = MathUtility.rotatePoint(mbPos, orientation.get2DDataValue() * -90, true, Axis.Y);
		return new BlockPos(pos.getX() - mbOffset.x, pos.getY() - mbOffset.y, pos.getZ() - mbOffset.z);
	}

	public BlockPos getBlockAt(BlockPos centerPos, BlockState state, Vec3i mbPos) {
		Direction orientation = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		Vec3i mbOffset = MathUtility.rotatePoint(mbPos, orientation.get2DDataValue() * -90, true, Axis.Y);
		return new BlockPos(centerPos.getX() + mbOffset.x, centerPos.getY() + mbOffset.y, centerPos.getZ() + mbOffset.z);
	}
	
	protected void breakMultiBlock(Level level, BlockPos pos, BlockState state, boolean removeClicked, boolean makeParticles) {

		Vec3i mbPos = getMBPos(state);
		Direction orientation = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		Vec3i mbOffset = MathUtility.rotatePoint(mbPos, orientation.get2DDataValue() * -90, true, Axis.Y);
		BlockPos originPos = new BlockPos(pos.getX() - mbOffset.x, pos.getY() - mbOffset.y, pos.getZ() - mbOffset.z);
		
		for (int x = 0; x < getWidth(); x++) {
			for (int z = 0; z < getDepth(); z++) {
				for (int y = 0; y < getHeight(); y++) {
					mbOffset = MathUtility.rotatePoint(new Vec3i(x, y, z), orientation.get2DDataValue() * -90, true, Axis.Y);
					BlockPos breakPos = originPos.offset(mbOffset.x, mbOffset.y, mbOffset.z);
					BlockState breakState = level.getBlockState(breakPos);
					if (!removeClicked && breakPos.equals(pos)) continue;
					if (breakState.getBlock() == this) {
						if (makeParticles) {
							level.destroyBlock(breakPos, false);
						} else {
							level.setBlock(breakPos, Blocks.AIR.defaultBlockState(), 3);
						}
					}
				}
			}
		}
		
	}
	
	protected boolean stillValid(Level level, BlockState state, BlockPos pos, BlockPos neighbor) {

		Direction orientation = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		Vec3i mbPos = getMBPos(state);
		Vec3i neighborDirection = Vec3i.fromVec(neighbor).sub(Vec3i.fromVec(pos));
		Vec3i mbNeighbor = MathUtility.rotatePoint(neighborDirection, orientation.get2DDataValue() * 90, true, Axis.Y).add(mbPos);
		
		boolean shouldBeMultiBlock = 
				mbNeighbor.x >= 0 && mbNeighbor.x < getWidth() &&
				mbNeighbor.y >= 0 && mbNeighbor.y < getHeight() &&
				mbNeighbor.z >= 0 && mbNeighbor.z < getDepth();
		if (shouldBeMultiBlock) {
			BlockState neighborState = level.getBlockState(neighbor);
			if (neighborState.getBlock() != this) return false;
		}
		return true;
	}
	
	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
		if (!stillValid(pLevel, pState, pPos, pNeighborPos)) {
			breakMultiBlock(pLevel, pPos, pState, true, true);
		}
	}
	
	public abstract BlockPos getMasterBlockEntityBlock(BlockState state, BlockPos pos);
	
	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState pState, net.minecraft.world.level.storage.loot.LootParams.Builder pParams) {
		BlockPos pos = MathUtility.toBlockPos(Vec3d.fromVec(pParams.getParameter(LootContextParams.ORIGIN)));
		BlockPos blockEntityPos = getMasterBlockEntityBlock(pState, pos);
		BlockEntity blockEntity = pParams.getLevel().getBlockEntity(blockEntityPos);
		return super.getDrops(pState, pParams.withParameter(LootContextParams.BLOCK_ENTITY, blockEntity));
	}
	
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		ItemStack stack = new ItemStack(this);
		if (player.getAbilities().instabuild && Screen.hasControlDown()) {
			BlockEntity blockEntity = level.getBlockEntity(getMasterBlockEntityBlock(state, pos));
			if (blockEntity != null) {
				CompoundTag blockEntityTag = blockEntity.saveWithoutMetadata();
				blockEntityTag.remove("x");
				blockEntityTag.remove("y");
				blockEntityTag.remove("z");
				blockEntityTag.remove("id");
				stack.addTagElement("BlockEntityTag", blockEntityTag);
				
				CompoundTag compoundtag1 = new CompoundTag();
				ListTag listtag = new ListTag();
				listtag.add(StringTag.valueOf("\"(+NBT)\""));
				compoundtag1.put("Lore", listtag);
				stack.addTagElement("display", compoundtag1);
			}
			
		}
		return stack;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getDepth() {
		return depth;
	}
	
}
