package de.industria.tileentity;

import de.industria.ModItems;
import de.industria.gui.ContainerMCoalHeater;
import de.industria.typeregistys.ModTileEntityType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;

public class TileEntityMCoalHeater extends TileEntityMHeaterBase implements INamedContainerProvider {
	
	public int fuelTime;
	public float burnTime;
	
	public TileEntityMCoalHeater() {
		super(ModTileEntityType.COAL_HEATER, 2);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void updateWorkState() {

		boolean fullOfAsh = (this.getItem(1).getItem() != Item.byBlock(ModItems.ash) || this.getItem(1).getCount() >= 64) && !this.getItem(1).isEmpty();
		
		if (!fullOfAsh) {
			
			if (this.burnTime > 0) {
				this.burnTime -= 1;
				isWorking = true;
				if (this.burnTime % 60 == 0) {
					ItemStack ashStack = this.getItem(1);
					if (ashStack.isEmpty()) {
						this.setItem(1, new ItemStack(ModItems.ash, 1));
					} else if (ashStack.getItem() == Item.byBlock(ModItems.ash) && ashStack.getCount() < 64) {
						ashStack.grow(1);
					}
				}
			} else if (hasFuelItems()) {
				this.burnTime = ForgeHooks.getBurnTime(this.itemstacks.get(0));
				this.fuelTime = (int) this.burnTime;
				this.itemstacks.get(0).shrink(1);
				isWorking = true;
			}
			
		}
		
	}
	
	@Override
	public boolean canWork() {
		return this.burnTime > 0 || this.hasFuelItems();
	}
	
	@SuppressWarnings("deprecation")
	public boolean hasFuelItems() {
		return this.itemstacks.get(0).isEmpty() ? false : ForgeHooks.getBurnTime(this.itemstacks.get(0)) > 0;
	}
	
	@Override
	public CompoundNBT save(CompoundNBT compound) {
		compound.putFloat("burnTime", this.burnTime);
		compound.putInt("fuelTime", this.fuelTime);
		return super.save(compound);
	}
	
	@Override
	public void load(BlockState state, CompoundNBT compound) {
		this.burnTime = compound.getFloat("burnTime");
		this.fuelTime = compound.getInt("fuelTime");
		super.load(state, compound);
	}
	
	@Override
	public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player) {
		return new ContainerMCoalHeater(id, playerInv, this);
	}
	
	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[] {0, 1};
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
		return index == 0 && ForgeHooks.getBurnTime(itemStackIn) > 0;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return index == 1;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("block.industria.coal_heater");
	}
	
}
