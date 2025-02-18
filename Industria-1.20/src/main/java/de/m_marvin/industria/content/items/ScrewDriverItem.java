package de.m_marvin.industria.content.items;

import de.m_marvin.industria.content.registries.ModTags;
import de.m_marvin.industria.core.scrollinput.type.items.IScrollOverride;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class ScrewDriverItem extends Item implements IScrollOverride {
	
	public ScrewDriverItem(Properties properties) {
		super(properties);
	}
		
	@Override
	public InteractionResult useOn(UseOnContext context) {
		return super.useOn(context);
	}
	
	public boolean canScrewDriverPickup(BlockState state) {
		return state.is(ModTags.Blocks.BLOCK_SCREW_DRIVER_PICKUP);
	}
	
	@Override
	public void onScroll(UseOnContext context, double delta) {
//		BlockPos targetedPos = context.getClickedPos();
//		BlockState targetedBlock = context.getLevel().getBlockState(targetedPos);
//		if (targetedBlock.getBlock() instanceof IScrewDriveable) {
//			Industria.NETWORK.sendToServer(new CScrewDriverAdjustmentPackage(context.getHitResult(), delta, context.getHand()));
//			adjustTargetedBlock(targetedBlock, context, delta);
//		}
	}
	
	public void adjustTargetedBlock(BlockState targetedBlock, UseOnContext context, double scrollDelta) {
//		IScrewDriveable actor = (IScrewDriveable) targetedBlock.getBlock();
//		InteractionResult result = actor.onScrewDriveAdjusting(targetedBlock, context, scrollDelta);
//		
//		if (result.shouldSwing()) {
//			
//		}
	}
	
	@Override
	public boolean overridesScroll(UseOnContext context, ItemStack stack) {
		if (context != null && context.getHitResult() != null) {
//			BlockPos targetedPos = context.getClickedPos();
//			Direction targetedFace = context.getClickedFace();
//			Vec3 targetedVec = context.getClickLocation();
//			BlockState targetedBlock = context.getLevel().getBlockState(targetedPos);
//			if (targetedBlock.getBlock() instanceof IScrewDriveable) {
//				IScrewDriveable actor = (IScrewDriveable) targetedBlock.getBlock();
//				return actor.isAdjustable(targetedBlock, targetedFace, targetedVec);
//			}
		}
		return false;		
	}
	
}