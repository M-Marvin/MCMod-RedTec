package de.industria.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.industria.blocks.BlockRSignalProcessorContact;
import de.industria.tileentity.TileEntityMFuseBox;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class TileEntityFuseBoxRenderer extends TileEntityRenderer<TileEntityMFuseBox> {

	private ItemRenderer itemRenderer;
	
	public TileEntityFuseBoxRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		this.itemRenderer = Minecraft.getInstance().getItemRenderer();
	}

	@Override
	public void render(TileEntityMFuseBox tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		
		if (!tileEntityIn.getFuse().isEmpty()) {
			
			ItemStack fuseStack = tileEntityIn.getFuse();
			BlockState state = tileEntityIn.getBlockState();
						
			matrixStackIn.pushPose();
				
				Direction direction = state.getValue(BlockRSignalProcessorContact.FACING);
				matrixStackIn.translate(0.5F, 0.5F, 0.5F);
				matrixStackIn.mulPose(direction.getRotation());
				matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90));
				matrixStackIn.scale(0.665F, 0.665F, 0.665F);
				matrixStackIn.translate(6 * 0.0625F, 0, -5 * 0.0625F);
				
				if (!fuseStack.isEmpty()) {
					
					this.itemRenderer.renderStatic(fuseStack, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
					
				}
				
			matrixStackIn.popPose();
			
		}
		
	}

}
