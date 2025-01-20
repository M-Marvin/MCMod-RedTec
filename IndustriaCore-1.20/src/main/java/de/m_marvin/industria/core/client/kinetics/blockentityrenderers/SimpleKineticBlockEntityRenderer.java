package de.m_marvin.industria.core.client.kinetics.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;

import de.m_marvin.industria.core.kinetics.types.blockentities.IKineticBlockEntity;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock;
import de.m_marvin.industria.core.kinetics.types.blocks.IKineticBlock.TransmissionNode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.data.ModelData;

public class SimpleKineticBlockEntityRenderer<T extends BlockEntity & IKineticBlockEntity> implements BlockEntityRenderer<T> {
	
	protected BlockRenderDispatcher dispatcher;
	
	public SimpleKineticBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.dispatcher = context.getBlockRenderDispatcher();
	}
	
	@Override
	public void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
		
		pPoseStack.pushPose();
		
		BlockState state = pBlockEntity.getBlockState();
		BakedModel model = dispatcher.getBlockModel(state);
		ModelData data = ModelData.EMPTY;
		
		int rpm = pBlockEntity.getRPM();
		double t = pBlockEntity.getLevel().getGameTime() + pPartialTick;
		float rotation = (float) ((float) (t / 3000 * rpm) * 2 * Math.PI);
		
		if (state.getBlock() instanceof IKineticBlock kinetic) {
			TransmissionNode[] nodes = kinetic.getTransmissionNodes(pBlockEntity.getLevel(), pBlockEntity.getBlockPos(), state);
			if (nodes.length == 0) return;
			Axis axis = nodes[0].axis();

			pPoseStack.translate(0.5, 0.5, 0.5);
			switch (axis) {
			case X: pPoseStack.mulPose(com.mojang.math.Axis.XP.rotation(rotation)); break;
			case Y: pPoseStack.mulPose(com.mojang.math.Axis.YP.rotation(rotation)); break;
			case Z: pPoseStack.mulPose(com.mojang.math.Axis.ZP.rotation(rotation)); break;
			}
			pPoseStack.translate(-0.5, -0.5, -0.5);
			
			for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(state, RandomSource.create(42), data))
				this.dispatcher.getModelRenderer().renderModel(pPoseStack.last(), pBuffer.getBuffer(net.minecraftforge.client.RenderTypeHelper.getEntityRenderType(rt, false)), state, model, 1F, 1F, 1F, pPackedLight, pPackedOverlay, data, rt);
			
			pPoseStack.popPose();
		}
		
	}
	
}
