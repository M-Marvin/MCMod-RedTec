package de.industria.blocks;

import java.util.ArrayList;

import de.industria.Industria;
import de.industria.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBurnedTrapdoor extends TrapDoorBlock
{
	
	public static final BooleanProperty PERSISTANT = BooleanProperty.create("persistant");
	public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 1, 8);
	
	public BlockBurnedTrapdoor() {
		super(Properties.create(Material.WOOD).hardnessAndResistance(0.8F, 0.2F).sound(SoundType.LADDER).harvestTool(BlockBase.getDefaultToolType(Material.WOOD)).setRequiresTool().notSolid());
		this.setRegistryName(Industria.MODID, "burned_trapdoor");
		this.setDefaultState(this.stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH).with(OPEN, Boolean.valueOf(false)).with(HALF, Half.BOTTOM).with(POWERED, Boolean.valueOf(false)).with(WATERLOGGED, Boolean.valueOf(false)).with(PERSISTANT, true).with(DISTANCE, 1));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(PERSISTANT, DISTANCE);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = super.getStateForPlacement(context).with(PERSISTANT, true);
		return updateState(context.getWorld(), context.getPos(), state);
	}
	
	public BlockState getNotPersistant() {
		return this.getDefaultState().with(PERSISTANT, false);
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		BlockState newState = updateState(worldIn, pos, state);
		if (newState != state) {
			worldIn.setBlockState(pos, newState);
		}
		if (state.get(DISTANCE) == 8 && !state.get(PERSISTANT)) {
			dropBlock(worldIn, pos, newState);
			return;
		}
		super.neighborChanged(newState, worldIn, pos, blockIn, fromPos, isMoving);
	}
	
	public int getDustDropAmount(World world, BlockPos pos, BlockState state) {
		return world.rand.nextInt(2) + 1;
	}
	
	public void dropBlock(World world, BlockPos pos, BlockState state) {
		world.destroyBlock(pos, false);
		BlockState ashDrops = ModItems.ash.getDefaultState().with(BlockFallingDust.LAYERS, getDustDropAmount(world, pos, state));
		world.setBlockState(pos, ashDrops);
	}
	
	public BlockState updateState(World world, BlockPos pos, BlockState state) {
		int distance = BlockBurnedBlock.scann(world, pos, new ArrayList<BlockPos>(), 0);
		if (distance != state.get(DISTANCE)) {
			state = state.with(DISTANCE, distance);
		}
		return state;
	}
	
}
