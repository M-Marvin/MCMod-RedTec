package de.redtec.fluids;

import java.util.Random;

import de.redtec.RedTec;
import de.redtec.fluids.util.GasFluid;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;

public class FluidSteam extends GasFluid implements IBucketPickupHandler {
	
	@Override
	public Item getFilledBucket() {
		return RedTec.steam_bucket;
	}
	
	@Override
	protected BlockState getBlockState(FluidState state) {
		return RedTec.steam.getDefaultState();
	}
	
	@Override
	public Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state) {
		worldIn.removeBlock(pos, false);
		return this;
	}
	
	@Override
	protected FluidAttributes createAttributes() {
		return FluidAttributes.builder(
			new ResourceLocation(RedTec.MODID, "block/steam_still"), 
			new ResourceLocation(RedTec.MODID, "block/steam_flow"))
				.gaseous()
				.build(this);
	}
	
	@Override
	public void onMoved(World world, BlockPos pos, Direction moveDirection, FluidState state, Random random) {
		
		if (random.nextInt(20) == 0) {
						
			if (world.getBlockState(pos.offset(moveDirection)).getFluidState().getFluid() == this) world.setBlockState(pos.offset(moveDirection), Blocks.AIR.getDefaultState());
			
			FallingBlockEntity condensetWater = new FallingBlockEntity(world, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, ModFluids.HOT_WATER.getDefaultState().getBlockState());
			condensetWater.fallTime = -1000;
			world.addEntity(condensetWater);
			
		}
		
	}
	
}