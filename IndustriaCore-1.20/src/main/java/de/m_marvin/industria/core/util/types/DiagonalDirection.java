package de.m_marvin.industria.core.util.types;

import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.m_marvin.univec.impl.Vec3f;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.StringRepresentable;

public enum DiagonalDirection implements StringRepresentable {
	
	UP(				"up", 			new Vec3i(0, +1, 0),	1,	1),
	DOWN(			"down", 		new Vec3i(0, -1, 0),	0,	0),
	NORTH(			"north", 		new Vec3i(0, 0, -1),	2,	5),
	NORTH_UP(		"north_up", 	new Vec3i(0, +1, -1),	10,	7),
	NORTH_DOWN(		"north_down", 	new Vec3i(0, -1, -1),	6,	6),
	SOUTH(			"south", 		new Vec3i(0, 0, +1),	3,	2),
	SOUTH_UP(		"south_up", 	new Vec3i(0, +1, +1),	11,	4),
	SOUTH_DOWN(		"south_down", 	new Vec3i(0, -1, +1),	7,	3),
	EAST(			"east", 		new Vec3i(+1, 0, 0),	4,	11),
	EAST_UP(		"east_up", 		new Vec3i(+1, +1, 0),	12,	13),
	EAST_DOWN(		"east_down", 	new Vec3i(+1, -1, 0),	8,	12),
	WEST(			"west", 		new Vec3i(-1, 0, 0),	5,	8),
	WEST_UP(		"west_up", 		new Vec3i(-1, +1, 0),	13,	10),
	WEST_DOWN(		"west_down", 	new Vec3i(-1, -1, 0),	9,	9),
	NORTH_EAST(		"north_east", 	new Vec3i(+1, 0, -1),	14,	17),
	NORTH_WEST(		"north_west", 	new Vec3i(-1, 0, -1),	15,	16),
	SOUTH_EAST(		"south_east", 	new Vec3i(+1, 0, +1),	16,	15),
	SOUTH_WEST(		"south_west", 	new Vec3i(-1, 0, +1),	17,	14);
	
	private final String name;
	private final Vec3i normal;
	private final Vec3f normalized;
	private final int data3d;
	private final int oposite;
	
	private static final DiagonalDirection[] BY_3D_DATA = Stream.of(values())
			.sorted(Comparator.comparingInt(DiagonalDirection::get3DDataValue))
			.toArray(DiagonalDirection[]::new);
	
	private DiagonalDirection(String name, Vec3i normal, int data3d, int oposite) {
		this.name = name;
		this.normal = normal;
		this.normalized = new Vec3f(normal).normalize();
		this.data3d = data3d;
		this.oposite = oposite;
	}
	
	@Override
	public String getSerializedName() {
		return this.name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Vec3i getNormal() {
		return normal;
	}
	
	public Vec3f getNormalized() {
		return this.normalized;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public int get3DDataValue() {
		return data3d;
	}
	
	public DiagonalDirection from3DDataValue(int data3d) {
		return BY_3D_DATA[data3d % BY_3D_DATA.length];
	}
	
	public DiagonalDirection getOposite() {
		return values()[this.oposite];
	}

	public static DiagonalDirection getNearest(double x, double y, double z) {
		return getNearest((float) x, (float) y, (float) z);
	}
	
	public static DiagonalDirection getNearest(float x, float y, float z) {
		DiagonalDirection direction = NORTH;
		float f = Float.MIN_VALUE;
		
		for (DiagonalDirection d : values()) {
			Vec3f normal = d.getNormalized();
			float f1 = normal.x() * x + normal.y * y + normal.z * z;
			if (f1 > f) {
				f = f1;
				direction = d;
			}
		}
		
		return direction;
	}
	
	private final static DiagonalDirection[] PLANAR_2D_ON_X = IntStream.range(0, DiagonalPlanarDirection.values().length)
			.mapToObj(DiagonalPlanarDirection::from2DDataValue)
			.map(dpd -> getNearest(0, dpd.getNormalized().y, dpd.getNormalized().x))
			.toArray(DiagonalDirection[]::new);
	private final static DiagonalDirection[] PLANAR_2D_ON_Y = IntStream.range(0, DiagonalPlanarDirection.values().length)
			.mapToObj(DiagonalPlanarDirection::from2DDataValue)
			.map(dpd -> getNearest(dpd.getNormalized().x, 0, dpd.getNormalized().y))
			.toArray(DiagonalDirection[]::new);
	private final static DiagonalDirection[] PLANAR_2D_ON_Z = IntStream.range(0, DiagonalPlanarDirection.values().length)
			.mapToObj(DiagonalPlanarDirection::from2DDataValue)
			.map(dpd -> getNearest(dpd.getNormalized().x, dpd.getNormalized().y, 0))
			.toArray(DiagonalDirection[]::new);

	public static DiagonalDirection fromPlanarAndAxis(DiagonalPlanarDirection planar, Axis axis) {
		switch (axis) {
		case X: return PLANAR_2D_ON_X[planar.get2DDataValue()];
		case Y: return PLANAR_2D_ON_Y[planar.get2DDataValue()];
		case Z: return PLANAR_2D_ON_Z[planar.get2DDataValue()];
		default: throw new IncompatibleClassChangeError();
		}
	}
	
	public DiagonalPlanarDirection onPlanarWithAxis(Axis axis) {
		switch (axis) {
		case X: 
			for (int i = 0; i < PLANAR_2D_ON_X.length; i++)
				if (PLANAR_2D_ON_X[i] == this) return DiagonalPlanarDirection.from2DDataValue(i);
			return null;
		case Y: 
			for (int i = 0; i < PLANAR_2D_ON_Y.length; i++)
				if (PLANAR_2D_ON_Y[i] == this) return DiagonalPlanarDirection.from2DDataValue(i);
			return null;
		case Z: 
			for (int i = 0; i < PLANAR_2D_ON_Z.length; i++)
				if (PLANAR_2D_ON_Z[i] == this) return DiagonalPlanarDirection.from2DDataValue(i);
			return null;
		default: throw new IncompatibleClassChangeError();
		}
	}
	
}
