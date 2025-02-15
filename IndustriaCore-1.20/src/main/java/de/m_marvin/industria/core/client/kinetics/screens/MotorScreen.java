package de.m_marvin.industria.core.client.kinetics.screens;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.client.util.screens.AbstractContainerWidgetScreen;
import de.m_marvin.industria.core.kinetics.engine.network.CEditMotorPackage;
import de.m_marvin.industria.core.kinetics.types.blockentities.MotorBlockEntity;
import de.m_marvin.industria.core.kinetics.types.containers.MotorContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MotorScreen extends AbstractContainerWidgetScreen<MotorContainer> {
	
	protected EditBox rpmField;
	protected EditBox torqueField;
	
	public MotorScreen(MotorContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	@Override
	protected void init() {
		super.init();
		
		this.rpmField = new EditBox(font, this.leftPos + 95, this.topPos + 30, 80, 20, Component.translatable("industriacore.power_source.voltage"));
		this.rpmField.setMaxLength(5);
		this.rpmField.setValue(Double.toString(this.menu.getBlockEntity().getSourceRPM()));
		this.addRenderableWidget(this.rpmField);
		
		this.torqueField = new EditBox(font, this.leftPos + 5, this.topPos + 30, 80, 20, Component.translatable("industriacore.power_source.power"));
		this.torqueField.setMaxLength(5);
		this.torqueField.setValue(Double.toString(this.menu.getBlockEntity().getSourceTorque()));
		this.addRenderableWidget(this.torqueField);
		
		this.titleLabelY = 0;
		this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
		
	}
	
	public void setMotor(double rpm, double torque) {
		MotorBlockEntity motor = this.menu.getBlockEntity();
		motor.setSourceRPM(rpm);
		motor.setSourceTorque(torque);
		IndustriaCore.NETWORK.sendToServer(new CEditMotorPackage(this.menu.getBlockEntity().getBlockPos(), rpm, torque));
	}
	
	@Override
	public void onClose() {
		double rpm = this.menu.getBlockEntity().getSourceRPM();
		double torque = this.menu.getBlockEntity().getSourceTorque();
		try {
			rpm = Double.parseDouble(this.rpmField.getValue());
		} catch (NumberFormatException e) {}
		try {
			torque = Double.parseDouble(this.torqueField.getValue());
		} catch (NumberFormatException e) {}
		setMotor(rpm, torque);
		super.onClose();
	}
	
	@Override
	protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
		pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, true);
		pGuiGraphics.drawString(this.font, Component.translatable("industriacore.ui.motor.torque"), 5, 20, 0xFFFFFF, true);
		pGuiGraphics.drawString(this.font, Component.translatable("industriacore.ui.motor.rpm"), 95, 20, 0xFFFFFF, true);
	}
	
	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		renderBackground(pGuiGraphics);
	}
	
	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (pKeyCode == 256) {
			this.rpmField.setFocused(false);
			this.torqueField.setFocused(false);
		}
		if (this.rpmField.keyPressed(pKeyCode, pScanCode, pModifiers) || this.rpmField.canConsumeInput()) return true;
		if (this.torqueField.keyPressed(pKeyCode, pScanCode, pModifiers) || this.torqueField.canConsumeInput()) return true;
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
	
}
