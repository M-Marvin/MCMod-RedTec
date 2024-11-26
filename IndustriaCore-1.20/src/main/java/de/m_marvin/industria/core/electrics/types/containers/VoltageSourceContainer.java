package de.m_marvin.industria.core.electrics.types.containers;

import de.m_marvin.industria.core.electrics.types.blockentities.VoltageSourceBlockEntity;
import de.m_marvin.industria.core.registries.MenuTypes;
import de.m_marvin.industria.core.util.container.AbstractBlockEntityContainerBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class VoltageSourceContainer extends AbstractBlockEntityContainerBase<VoltageSourceBlockEntity> {

	public VoltageSourceContainer(int id, Inventory playerInv, FriendlyByteBuf data) {
		super(MenuTypes.VOLTAGE_SOURCE.get(), id, playerInv, data);
	}
	
	public VoltageSourceContainer(int id, Inventory playerInv, VoltageSourceBlockEntity tileEntity) {
		super(MenuTypes.VOLTAGE_SOURCE.get(), id, playerInv, tileEntity);
	}

	@Override
	public int getSlots() {
		return 0;
	}

	@Override
	public void init() {}
	
}
