package de.m_marvin.industria.core.client.kinetics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import de.m_marvin.industria.IndustriaCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE,modid=IndustriaCore.MODID, value=Dist.CLIENT)
public class KineticBlockRenderer {

	protected static float animationTicks;
	
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onWorldRender(RenderLevelStageEvent event) {
		
		if (event.getStage() == Stage.AFTER_PARTICLES) {
			
			animationTicks = event.getRenderTick() + event.getPartialTick();
			
			BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
			PoseStack matrixStack = event.getPoseStack();
			ClientLevel level = Minecraft.getInstance().level;

			RenderSystem.enableDepthTest();
			
			Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			matrixStack.pushPose();
			matrixStack.translate(-offset.x, -offset.y, -offset.z);

			if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
				
				drawDebugTransmitions(matrixStack, source, level, event.getPartialTick());
				
			}
			
			source.endBatch();
			matrixStack.popPose();
			
			RenderSystem.disableDepthTest();
			
		}
		
	}

	/* private render methods, called by the render event */
	
	private static void drawDebugTransmitions(PoseStack matrixStack, MultiBufferSource bufferSource, ClientLevel clientLevel, float partialTicks) {
		
		matrixStack.pushPose();
		
		VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
		
		float r = 1.0F;
		float g = 0.0F;
		float b = 0.5F;
		
		LevelRenderer.renderLineBox(
        		matrixStack, buffer, 
        		0, 0, 0, 
        		1, 1, 1, 
        		r, g, b, 1F,
        		r, g, b);
		
//		-put("Position", ELEMENT_POSITION)
//		.put("Color", ELEMENT_COLOR)
//		.put("UV0", ELEMENT_UV0)
//		.put("UV2", ELEMENT_UV2)
//		.put("Normal", ELEMENT_NORMAL)
//		.put("Padding", ELEMENT_PADDING)
		
//		buffer.vertex(0, 0, 0).color(255F, 1F, 1F, 255F).uv(0, 0).uv2(222).normal(0, 1, 0).endVertex();
//		buffer.vertex(1, 0, 0).color(1F, 1F, 1F, 255F).uv(1, 0).uv2(0).normal(0, 1, 0).endVertex();
//		buffer.vertex(1, 1, 0).color(1F, 1F, 1F, 255F).uv(1, 1).uv2(0).normal(0, 1, 0).endVertex();
//		buffer.vertex(0, 1, 0).color(1F, 1F, 1F, 255F).uv(0, 1).uv2(0).normal(0, 1, 0).endVertex();
		
		matrixStack.popPose();
		
	}
	
}
