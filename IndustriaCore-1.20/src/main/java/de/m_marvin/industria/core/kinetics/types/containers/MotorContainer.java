package de.m_marvin.industria.core.kinetics.types.containers;

import de.m_marvin.industria.core.kinetics.types.blockentities.MotorBlockEntity;
import de.m_marvin.industria.core.registries.MenuTypes;
import de.m_marvin.industria.core.util.container.AbstractBlockEntityContainerBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MotorContainer extends AbstractBlockEntityContainerBase<MotorBlockEntity> {

	public MotorContainer(int id, Inventory playerInv, FriendlyByteBuf data) {
		super(MenuTypes.MOTOR.get(), id, playerInv, data);
	}
	
	public MotorContainer(int id, Inventory playerInv, MotorBlockEntity tileEntity) {
		super(MenuTypes.MOTOR.get(), id, playerInv, tileEntity);
	}

	@Override
	public int getSlots() {
		return 0;
	}

	@Override
	public void init() {}
	
}