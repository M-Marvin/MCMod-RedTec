package de.m_marvin.industria.core.electrics.types.blockentities;

import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.ElectricUtility;
import de.m_marvin.industria.core.electrics.types.containers.JunctionBoxContainer;
import de.m_marvin.industria.core.electrics.types.containers.JunctionBoxContainer.ExternalNodeConstructor;
import de.m_marvin.industria.core.electrics.types.containers.JunctionBoxContainer.InternalNodeConstructor;
import de.m_marvin.industria.core.electrics.types.containers.VoltageSourceContainer;
import de.m_marvin.industria.core.parametrics.BlockParametrics;
import de.m_marvin.industria.core.parametrics.engine.BlockParametricsManager;
import de.m_marvin.industria.core.registries.BlockEntityTypes;
import de.m_marvin.industria.core.registries.Blocks;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.industria.core.util.types.PlanarDirection;
import de.m_marvin.univec.impl.Vec2i;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VoltageSourceBlockEntity extends BlockEntity implements MenuProvider, IJunctionEdit {
	
	protected String[] nodeLanes = new String[] {"L", "N"};
	protected int voltage;
	protected int power;
	
	public VoltageSourceBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityTypes.VOLTAGE_SOURCE.get(), pPos, pBlockState);
		BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(Blocks.VOLTAGE_SOURCE.get());
		this.voltage = parametrics.getNominalVoltage();
		this.power = parametrics.getNominalPower();
	}
	
	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		return GameUtility.openJunctionScreenOr(this, pContainerId, pPlayer, pPlayerInventory, () -> new VoltageSourceContainer(pContainerId, pPlayerInventory, this));
	}
	
	public String[] getNodeLanes() {
		return this.nodeLanes;
	}
	
	public void getNodeLanes(String[] laneLabels) {
		if (laneLabels.length == 2) {
			this.nodeLanes = laneLabels;
			this.setChanged();
		}
	}
	
	public int getVoltage() {
		return voltage;
	}
	
	public void setVoltageAndPower(int voltage, int power) {
		BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(Blocks.VOLTAGE_SOURCE.get());
		this.power = Math.max(parametrics.getPowerMin(), Math.min(parametrics.getPowerMax(), power));
		this.voltage = Math.max(parametrics.getVoltageMin(), Math.min(parametrics.getVoltageMax(), voltage));
		this.setChanged();
		ElectricUtility.updateNetwork(level, worldPosition);
	}
	
	public int getPower() {
		return power;
	}
	
	@Override
	public Component getDisplayName() {
 		return this.getBlockState().getBlock().getName();
	}
	
	@Override
	protected void saveAdditional(CompoundTag pTag) {
		super.saveAdditional(pTag);
		pTag.putString("LiveWireLane", this.nodeLanes[0]);
		pTag.putString("NeutralWireLane", this.nodeLanes[1]);
		pTag.putInt("Voltage", this.voltage);
		pTag.putInt("Power", this.power);
	}
	
	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		this.nodeLanes[0] = pTag.contains("LiveWireLane") ? pTag.getString("LiveWireLane") : "L";
		this.nodeLanes[1] = pTag.contains("NeutralWireLane") ? pTag.getString("NeutralWireLane") : "N";
		this.voltage = pTag.getInt("Voltage");
		this.power = pTag.getInt("Power");
	}
	
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		tag.putString("LiveWireLane", this.nodeLanes[0]);
		tag.putString("NeutralWireLane", this.nodeLanes[1]);
		tag.putInt("Voltage", this.voltage);
		tag.putInt("Power", this.power);
		return tag;
	}
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.load(tag);
	}
	
	@Override
	public <B extends BlockEntity & IJunctionEdit> void setupScreenConduitNodes(JunctionBoxContainer<B> abstractJunctionBoxScreen, NodePos[] conduitNodes,ExternalNodeConstructor externalNodeConstructor, InternalNodeConstructor internalNodeConstructor) {
		externalNodeConstructor.construct(new Vec2i(69, 8), 	PlanarDirection.Y_POS, 	conduitNodes[0]);
		internalNodeConstructor.construct(new Vec2i(69, 112), 	PlanarDirection.Y_NEG, 	0);
	}

	@Override
	public boolean connectsOnlyToInternal() {
		return true;
	}
	
	@Override
	public Level getJunctionLevel() {
		return this.level;
	}
	
	@Override
	public BlockPos getJunctionBlockPos() {
		return this.worldPosition;
	}
	
}
