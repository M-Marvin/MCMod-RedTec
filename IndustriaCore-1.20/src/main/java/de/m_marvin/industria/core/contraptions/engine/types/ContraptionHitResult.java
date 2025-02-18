package de.m_marvin.industria.core.contraptions.engine.types;

import de.m_marvin.industria.core.contraptions.engine.types.contraption.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ContraptionHitResult extends HitResult {

	private final Contraption contraption;
	private final BlockPos shipBlock;
	private final boolean miss;
		
	protected ContraptionHitResult(boolean miss, Vec3 location, BlockPos shipBlock, Contraption contraption) {
		super(location);
		this.miss = miss;
		this.shipBlock = shipBlock;
		this.contraption = contraption;
	}
	
	@Override
	public Type getType() {
		return this.miss ? Type.MISS : Type.BLOCK;
	}
	
	public BlockPos getShipBlock() {
		return shipBlock;
	}
	
	public Contraption getContraption() {
		return contraption;
	}
	
	public static ContraptionHitResult miss(Vec3 location) {
		return new ContraptionHitResult(true, location, null, null);
	}
	
	public static ContraptionHitResult hit(Vec3 location, BlockPos shipBlock, Contraption contraption) {
		return new ContraptionHitResult(false, location, shipBlock, contraption);
	}
	
}
