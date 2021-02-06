package de.redtec.fluids;

import java.util.Random;

import de.redtec.fluids.util.BlockModFlowingFluid;
import de.redtec.registys.ModDamageSource;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSulfuricAcid extends BlockModFlowingFluid {
	
	public BlockSulfuricAcid() {
		super("sulfuric_acid", ModFluids.SULFURIC_ACID, AbstractBlock.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops());
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		
		entityIn.attackEntityFrom(ModDamageSource.ACID, 4F);
		if (entityIn instanceof ItemEntity) {
			entityIn.remove();
			worldIn.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
		}
		if (entityIn instanceof LivingEntity) ((LivingEntity) entityIn).addPotionEffect(new EffectInstance(Effects.WITHER, 200, 2));
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {

		if (worldIn.getBlockState(pos.up()).isAir()) {
			float fx = rand.nextFloat() + pos.getX();
			float fy = rand.nextFloat() + pos.getY();
			float fz = rand.nextFloat() + pos.getZ();
			
			worldIn.addParticle(ParticleTypes.CLOUD, fx, fy, fz, 0, 0.1F, 0);
		}
		
		super.animateTick(stateIn, worldIn, pos, rand);
		
	}
	
}