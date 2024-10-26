package de.m_marvin.industria.core.client.util.screens;

import de.m_marvin.industria.core.client.util.widgets.AbstractTickableWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractContainerWidgetScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	public AbstractContainerWidgetScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, pTitle);
	}
	
	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
		if (this.getFocused() != null && this.isDragging() && pButton == 0 ? this.getFocused().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) : false) return true;
		return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
	}
	
	@Override
	protected void containerTick() {
		this.children().forEach(s -> { if (s instanceof AbstractTickableWidget t) t.tick(); });
		super.containerTick();
	}
	
}
