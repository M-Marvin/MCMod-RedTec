package de.m_marvin.industria.core.contraptions.engine.commands.arguments.vec3relative;

import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.commands.arguments.coordinates.Coordinates;

public interface Vec3Relative extends Coordinates {
	
	Vec3d getPosition(Vec3d sourceOffset, Vec3d sourceOrientation);
	Vec3d getRotation(Vec3d sourceRotation, Vec3d sourceOrientation);
	
}
