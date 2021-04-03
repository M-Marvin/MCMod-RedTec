package de.redtec.blocks;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import de.redtec.items.ItemBlockAdvancedInfo.IBlockToolType;
import de.redtec.tileentity.TileEntityRHoverControler;
import de.redtec.util.blockfeatures.IAdvancedBlockInfo;
import de.redtec.util.blockfeatures.IElectricConnectiveBlock;
import de.redtec.util.blockfeatures.ISignalConnectiveBlock;
import de.redtec.util.types.RedstoneControlSignal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockRHoverControler extends BlockContainerBase implements ISignalConnectiveBlock, IElectricConnectiveBlock, IAdvancedBlockInfo {
	
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	
	public BlockRHoverControler() {
		super("hover_controler", Material.IRON, 6, SoundType.ANVIL);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing());
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new TileEntityRHoverControler();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		
		if (tileEntity instanceof TileEntityRHoverControler) {
			if (!worldIn.isRemote()) NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, pos);
			return ActionResultType.CONSUME;
		}
		
		return ActionResultType.PASS;
		
	}
	
	@Override
	public void onReciveSignal(World worldIn, BlockPos pos, RedstoneControlSignal signal, Direction side) {
		
		ItemStack chanelItem = signal.getChanelItem();
		boolean powered = signal.isPowered();
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		
		if (tileEntity instanceof TileEntityRHoverControler) {
			
			((TileEntityRHoverControler) tileEntity).onControll(chanelItem, powered);
			
		}
		
	}
	
	@Override
	public boolean canConectSignalWire(IWorldReader world, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public boolean isStickyBlock(BlockState state) {
		return true;
	}
	
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}
	
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.with(FACING, mirrorIn.mirror(state.get(FACING)));
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof IInventory) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) te);
		}
		super.onBlockHarvested(worldIn, pos, state, player);
	}

	@Override
	public Voltage getVoltage(World world, BlockPos pos, BlockState state, Direction side) {
		return Voltage.NormalVoltage;
	}

	@Override
	public float getNeededCurrent(World world, BlockPos pos, BlockState state, Direction side) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileEntityRHoverControler) {
			return ((TileEntityRHoverControler) tileEntity).needEnergy() ? 10 : 0;
		}
		return 0;
	}

	@Override
	public boolean canConnect(Direction side, World world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public DeviceType getDeviceType() {
		return DeviceType.MASCHINE;
	}
	
	@Override
	public IBlockToolType getBlockInfo() {
		return (stack, info) -> {
			info.add(new TranslationTextComponent("redtec.block.info.needEnergy", (10 * Voltage.NormalVoltage.getVoltage() / 1000F) + "k"));
			info.add(new TranslationTextComponent("redtec.block.info.needVoltage", Voltage.NormalVoltage.getVoltage()));
			info.add(new TranslationTextComponent("redtec.block.info.needCurrent", 10));
		};
	}
	
	@Override
	public Supplier<Callable<ItemStackTileEntityRenderer>> getISTER() {
		return null;
	}
	
}
