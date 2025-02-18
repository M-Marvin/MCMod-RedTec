package de.m_marvin.industria.core.conduits.types.conduits;

import de.m_marvin.industria.core.conduits.types.ConduitPos;
import de.m_marvin.industria.core.conduits.types.conduits.Conduit.ConduitShape;
import de.m_marvin.industria.core.registries.Conduits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ConduitEntity {
	
	protected ConduitPos position;
	protected double length;
	protected Conduit conduit;
	protected ConduitShape shape;
	
	public ConduitEntity(ConduitPos position, Conduit conduit, double length) {
		this.position = position;
		this.conduit = conduit;
		this.length = length;
	}
	
	public ConduitEntity build(Level level) {
		this.conduit.onBuild(level, position, this);
		this.shape = conduit.buildShape(level, this);
		updateShape(level);
		return this;
	}
	
	public ConduitEntity dismantle(Level level) {
		this.conduit.onDismantle(level, position, this);
		this.conduit.dismantleShape(level, this);
		this.shape = null;
		return this;
	}
	
	public void updateShape(Level level) {
		assert this.shape != null : "Can't update un-build conduit!";
		this.conduit.updatePhysicalNodes(level, this);
	}
	
	public CompoundTag save() {
		return save(BlockPos.ZERO);
	}
	
	public CompoundTag save(BlockPos relative) {
		CompoundTag tag = new CompoundTag();
		tag.put("Position", this.position.writeNBT(new CompoundTag(), relative));
		tag.putString("Conduit", Conduits.CONDUITS_REGISTRY.get().getKey(this.conduit).toString());
		tag.putDouble("Length", this.length);
		if (this.shape != null) tag.put("Shape", this.shape.save());
		this.saveAdditional(tag);
		return tag;
	}
	
	public static ConduitEntity load(CompoundTag tag) {
		return load(tag, BlockPos.ZERO);
	}
	
	public static ConduitEntity load(CompoundTag tag, BlockPos relative) {
		ResourceLocation conduitName = new ResourceLocation(tag.getString("Conduit"));
		Conduit conduit = Conduits.CONDUITS_REGISTRY.get().getValue(conduitName);
		if (conduit == null) return null;
		ConduitPos position = ConduitPos.readNBT(tag.getCompound("Position"), relative);
		double length = tag.getDouble("Length");
		ConduitShape shape = tag.contains("Shape") ? ConduitShape.load(tag.getCompound("Shape")) : null;
		ConduitEntity state = conduit.newConduitEntity(position, conduit, length);
		if (shape != null) state.setShape(shape);
		state.loadAdditional(tag);
		return state;
	}
	
	public void saveAdditional(CompoundTag tag) {};
	public void loadAdditional(CompoundTag tag) {};
	public CompoundTag getUpdateTag() { return new CompoundTag(); };
	public void readUpdateTag(CompoundTag tag) {};
	
	public ConduitShape getShape() {
		return shape;
	}
	
	public void setShape(ConduitShape shape) {
		this.shape = shape;
	}
	
	public Conduit getConduit() {
		return conduit;
	}

	public ConduitPos getPosition() {
		return position;
	}
	
	public double getLength() {
		return length;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConduitEntity other) {
			return 	other.getPosition().equals(this.getPosition()) &
					other.conduit == this.conduit;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "PlacedConduit{conduit=" + Conduits.CONDUITS_REGISTRY.get().getKey(this.conduit) + 
				",length=" + this.length +
				",position=" + this.position.toString() + 
				"}";
 	}

	public int getNodeCount() {
		return 2;
	}
	
}
