package de.m_marvin.industria.core.client.util.widgets;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract class AbstractTickableWidget extends AbstractWidget {

	public AbstractTickableWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
		super(pX, pY, pWidth, pHeight, pMessage);
	}

	public abstract void tick();
	
}
