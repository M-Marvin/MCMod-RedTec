package de.m_marvin.industria.core.contraptions.engine;

import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import net.minecraft.server.level.ServerLevel;

@SuppressWarnings("deprecation")
public abstract class ForcesInducer extends ContraptionAttachment implements ShipForcesInducer {
	
	public ServerLevel getLevel() {
		return this.contraption.getLevel();
	}
	
}
