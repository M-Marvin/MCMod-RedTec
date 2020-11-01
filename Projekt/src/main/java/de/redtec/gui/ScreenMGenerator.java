package de.redtec.gui;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.redtec.RedTec;
import de.redtec.tileentity.TileEntityMGenerator;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;

public class ScreenMGenerator extends ContainerScreen<ContainerMGenerator> {

public static final ResourceLocation GENERATOR_GUI_TEXTURES = new ResourceLocation(RedTec.MODID, "textures/gui/generator.png");

	public ScreenMGenerator(ContainerMGenerator screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
	}
	
	@SuppressWarnings("deprecation")
	protected void func_230450_a_(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.field_230706_i_.getTextureManager().bindTexture(GENERATOR_GUI_TEXTURES);
		int i = this.guiLeft;
		int j = (this.field_230709_l_ - this.ySize) / 2;
		this.func_238474_b_(p_230450_1_, i, j, 0, 0, this.xSize, this.ySize);
		
		TileEntityMGenerator te = this.container.getTileEntity();
		int burnProgress = (int) (te.burnTime / te.fuelTime * 14);
		int remainingFuel = (int) te.burnTime + (te.hasFuelItems() ? te.getStackInSlot(0).getCount() * ForgeHooks.getBurnTime(te.getStackInSlot(0)) : 0);
		
		this.func_238474_b_(p_230450_1_, i + 53, j + 54 + 14 - burnProgress, 176, 0 + 14 - burnProgress, 14, burnProgress);
		
		int red = new Color(255, 0, 0).getRGB();
		int white = new Color(63, 63, 63).getRGB();
		this.field_230712_o_.func_243248_b(p_230450_1_, new TranslationTextComponent("redtec.generator.remainingFuel"), i + 90, j + 36, white);
		this.field_230712_o_.func_243248_b(p_230450_1_, new StringTextComponent("" + remainingFuel), i + 90, j + 46, remainingFuel < 500 ? red : white);
		
	}
	
}
