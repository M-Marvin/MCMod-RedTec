package de.m_marvin.industria.core.client.electrics.screens;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.client.util.screens.AbstractContainerWidgetScreen;
import de.m_marvin.industria.core.client.util.widgets.PowerInfo;
import de.m_marvin.industria.core.electrics.engine.network.CEditPowerSourcePackage;
import de.m_marvin.industria.core.electrics.types.blockentities.VoltageSourceBlockEntity;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricInfoProvider;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricInfoProvider.ElectricInfo;
import de.m_marvin.industria.core.electrics.types.containers.VoltageSourceContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

public class VoltageSourceScreen extends AbstractContainerWidgetScreen<VoltageSourceContainer> {

	protected ElectricInfo electricInfo;
	protected PowerInfo powerInfo;
	protected EditBox voltageField;
	protected EditBox powerField;
	
	public VoltageSourceScreen(VoltageSourceContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}

	@Override
	protected void init() {
		super.init();
		
		BlockState blockState = this.menu.getBlockEntity().getBlockState();
		if (blockState.getBlock() instanceof IElectricInfoProvider provider) {
			this.electricInfo = provider.getInfo(blockState, this.menu.getBlockEntity().getJunctionLevel(), this.menu.getBlockEntity().getJunctionBlockPos());
		}
		
		if (this.electricInfo == null) return;
		
		this.powerInfo = new PowerInfo(font, this.leftPos, this.topPos + 30, 80, this.electricInfo);
		this.addRenderableWidget(this.powerInfo);
		
		this.voltageField = new EditBox(font, this.leftPos + 95, this.topPos + 30, 80, 20, Component.translatable("industriacore.power_source.voltage"));
		this.voltageField.setMaxLength(5);
		this.voltageField.setValue(Integer.toString(this.menu.getBlockEntity().getVoltage()));
		this.addRenderableWidget(this.voltageField);
		
		this.powerField = new EditBox(font, this.leftPos + 95, this.topPos + 63, 80, 20, Component.translatable("industriacore.power_source.power"));
		this.powerField.setMaxLength(5);
		this.powerField.setValue(Integer.toString(this.menu.getBlockEntity().getPower()));
		this.addRenderableWidget(this.powerField);
		
		this.titleLabelY = 0;
		this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2;
		
	}
	
	public void setPowerSource(int voltage, int power) {
		VoltageSourceBlockEntity powerSource = this.menu.getBlockEntity();
		powerSource.setVoltageAndPower(voltage, power);
		IndustriaCore.NETWORK.sendToServer(new CEditPowerSourcePackage(powerSource.getJunctionBlockPos(), voltage, power));
	}
	
	@Override
	public void onClose() {
		int voltage = this.menu.getBlockEntity().getVoltage();
		int power = this.menu.getBlockEntity().getPower();
		try {
			voltage = Integer.parseInt(this.voltageField.getValue());
		} catch (NumberFormatException e) {}
		try {
			power = Integer.parseInt(this.powerField.getValue());
		} catch (NumberFormatException e) {}
		setPowerSource(voltage, power);
		super.onClose();
	}
	
	@Override
	protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
		pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, true);
		pGuiGraphics.drawString(this.font, Component.translatable("industriacore.ui.voltage_source.voltage"), 95, 20, 0xFFFFFF, true);
		pGuiGraphics.drawString(this.font, Component.translatable("industriacore.ui.voltage_source.power"), 95, 53, 0xFFFFFF, true);
	}
	
	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		renderBackground(pGuiGraphics);
		this.powerInfo.setStatus(this.electricInfo);
	}
	
	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (pKeyCode == 256) {
			this.voltageField.setFocused(false);
			this.powerField.setFocused(false);
		}
		if (this.voltageField.keyPressed(pKeyCode, pScanCode, pModifiers) || this.voltageField.canConsumeInput()) return true;
		if (this.powerField.keyPressed(pKeyCode, pScanCode, pModifiers) || this.powerField.canConsumeInput()) return true;
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
	
}
