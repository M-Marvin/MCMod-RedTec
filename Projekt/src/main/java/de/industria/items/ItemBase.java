package de.industria.items;

import de.industria.Industria;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;

public class ItemBase extends Item {
	
	public ItemBase(Item.Properties properties) {
		super(properties);
	}
	
	public ItemBase(String name, Item.Properties properties) {
		super(properties);
		this.setRegistryName(Industria.MODID, name);
	}
	
	public ItemBase(String name, ItemGroup itemGroup) {
		super(new Properties().tab(itemGroup));
		this.setRegistryName(Industria.MODID, name);
	}
	
	public ItemBase(String name, ItemGroup itemGroup, int maxStackSize) {
		super(new Properties().tab(itemGroup).stacksTo(maxStackSize));
		this.setRegistryName(Industria.MODID, name);
	}

	public ItemBase(String name, ItemGroup itemGroup, int maxStackSize, Item containItem) {
		super(new Properties().tab(itemGroup).stacksTo(maxStackSize).craftRemainder(containItem));
		this.setRegistryName(Industria.MODID, name);
	}
	
	public ItemBase(String name, ItemGroup itemGroup, Rarity rarity) {
		super(new Properties().tab(itemGroup).rarity(rarity));
		this.setRegistryName(Industria.MODID, name);
	}
	
	public ItemBase(String name, ItemGroup itemGroup, int maxStackSize, Rarity rarity) {
		super(new Properties().tab(itemGroup).stacksTo(maxStackSize).rarity(rarity));
		this.setRegistryName(Industria.MODID, name);
	}
	
}
