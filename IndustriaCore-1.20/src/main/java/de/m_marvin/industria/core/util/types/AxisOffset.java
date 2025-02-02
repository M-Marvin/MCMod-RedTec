package de.m_marvin.industria.core.util.types;

import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.StringRepresentable;

public enum AxisOffset implements StringRepresentable {
	FRONT("front"),
	CENTER("center"),
	BACK("back");

	private final String name;
	
	private AxisOffset(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String getSerializedName() {
		return name;
	}

	public AxisDirection getAxisDirection() {
		if (this == FRONT) return AxisDirection.POSITIVE;
		if (this == BACK) return AxisDirection.NEGATIVE;
		return null;
	}
	
	public static AxisOffset fromAxisDirection(AxisDirection axis) {
		return axis == AxisDirection.POSITIVE ? FRONT : BACK;
	}
	
}
