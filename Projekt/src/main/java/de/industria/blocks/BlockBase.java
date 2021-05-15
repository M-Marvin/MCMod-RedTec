package de.industria.blocks;

import de.industria.Industria;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public class BlockBase extends Block {
	
	public BlockBase(String name, Properties properties) {
		super(properties);
		this.setRegistryName(Industria.MODID, name);
	}
	
	public BlockBase(String name, Material material, float hardnessAndResistance, SoundType sound, boolean dropsEver) {
		super(Properties.create(material).hardnessAndResistance(hardnessAndResistance).sound(sound).harvestTool(getDefaultToolType(material)));
		this.setRegistryName(Industria.MODID, name);
	}
	
	public BlockBase(String name, Material material, float hardness, float resistance, SoundType sound, boolean dropsEver) {
		super(Properties.create(material).hardnessAndResistance(hardness, resistance).sound(sound).harvestTool(getDefaultToolType(material)));
		this.setRegistryName(Industria.MODID, name);
	}

	public BlockBase(String name, Material material, float hardnessAndResistance, SoundType sound) {
		super(Properties.create(material).hardnessAndResistance(hardnessAndResistance).sound(sound).harvestTool(getDefaultToolType(material)).setRequiresTool());
		this.setRegistryName(Industria.MODID, name);
	}
	
	public BlockBase(String name, Material material, float hardness, float resistance, SoundType sound) {
		super(Properties.create(material).hardnessAndResistance(hardness, resistance).sound(sound).harvestTool(getDefaultToolType(material)).setRequiresTool());
		this.setRegistryName(Industria.MODID, name);
	}
	
	public static ToolType getDefaultToolType(Material material) {
		if (material == Material.ROCK || material == Material.IRON) return ToolType.PICKAXE;
		if (material == Material.SAND || material == Material.EARTH) return ToolType.SHOVEL;
		if (material == Material.WOOD) return ToolType.AXE;
		if (material == Material.ORGANIC || material == Material.LEAVES) return ToolType.HOE;
		return null;
	}
	
}
