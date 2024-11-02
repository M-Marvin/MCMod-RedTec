package de.m_marvin.industria.core.contraptions.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.m_marvin.industria.core.contraptions.engine.types.ServerContraption;

public class ContraptionAttachment {
	
	@JsonIgnore
	protected ServerContraption contraption;
	
	public void setContrapion(ServerContraption contraption) {
		this.contraption = contraption;
	}
	
}
