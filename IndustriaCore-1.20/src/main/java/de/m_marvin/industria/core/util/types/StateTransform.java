package de.m_marvin.industria.core.util.types;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public enum StateTransform implements StringRepresentable {
	
	NONE("none", Mirror.NONE, Rotation.NONE),
	CLOCKWISE_90("clockwise_90", Mirror.NONE, Rotation.CLOCKWISE_90),
	CLOCKWISE_180("clockwise_180", Mirror.NONE, Rotation.CLOCKWISE_180),
	COUNTERCLOCKWISE_90("counterclockwise_90", Mirror.NONE, Rotation.COUNTERCLOCKWISE_90),
	LEFT_RIGHT("left_right", Mirror.LEFT_RIGHT, Rotation.NONE),
	FRONT_BACK("front_back", Mirror.FRONT_BACK, Rotation.NONE);
	
	private final Rotation rotation;
	private final Mirror mirror;
	private final String name;
	
	private StateTransform(String name, Mirror mirror, Rotation rotation) {
		this.mirror = mirror;
		this.rotation = rotation;
		this.name = name;
	}
	
	public Rotation getRotation() {
		return rotation;
	}
	
	public Mirror getMirror() {
		return mirror;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
	
	public static StateTransform of(Mirror mirror) {
		switch (mirror) {
		case LEFT_RIGHT: return LEFT_RIGHT;
		case FRONT_BACK: return FRONT_BACK;
		default: return NONE;
		}
	}
	
	public static StateTransform of(Rotation rotation) {
		switch (rotation) {
		case CLOCKWISE_180: return CLOCKWISE_180;
		case CLOCKWISE_90: return CLOCKWISE_90;
		case COUNTERCLOCKWISE_90: return COUNTERCLOCKWISE_90;
		default: return NONE;
		}
	}
	
}
