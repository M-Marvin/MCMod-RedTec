package de.redtec.util;

import java.util.HashMap;
import java.util.Map.Entry;

import de.redtec.RedTec;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class MinecartBoostHandler extends WorldSavedData {
	
	protected HashMap<AbstractMinecartEntity, Long> boostedMinecarts = new HashMap<AbstractMinecartEntity, Long>();
	protected World world;
	
	protected static MinecartBoostHandler clientInstance;
	
	private boolean isServerInstace;
	
	public MinecartBoostHandler(IWorld world) {
		this(true);
		this.world = (World) world;
	}
	
	public MinecartBoostHandler(boolean serverInstance) {
		super("minecart_boosts");
		this.isServerInstace = serverInstance;
	}
	
	public static MinecartBoostHandler getHandlerForWorld(IWorld world) {
		
		if (!world.isRemote()) {
			DimensionSavedDataManager storage = ((ServerWorld) world).getSavedData();
			MinecartBoostHandler handler = storage.getOrCreate(() -> new MinecartBoostHandler(world), "minecart_boosts");
			return handler;
		} else {
			if (clientInstance == null) clientInstance = new MinecartBoostHandler(false);
			return clientInstance;
		}
		
	}
	
	public boolean isServerInstace() {
		return isServerInstace;
	}
	
	public void setBoosted(AbstractMinecartEntity cart) {
		this.boostedMinecarts.put(cart, cart.world.getGameTime());
	}
	
	public void stopBoosted(AbstractMinecartEntity cart) {
		this.boostedMinecarts.remove(cart);
	}
	
	public boolean isBoosted(AbstractMinecartEntity cart) {
		return this.boostedMinecarts.containsKey(cart);
	}
	
	@SuppressWarnings("unchecked")
	public void updateMinecarts() {
		
		for (Entry<AbstractMinecartEntity, Long> entry : ((HashMap<AbstractMinecartEntity, Long>) this.boostedMinecarts.clone()).entrySet()) {
			
			long tagAge = world.getGameTime() - entry.getValue();
			if (tagAge > 350) {
				this.stopBoosted(entry.getKey());
			}
			
			Block railBlock1 = world.getBlockState(entry.getKey().getPosition()).getBlock();
			Block railBlock2 = world.getBlockState(entry.getKey().getPosition().down()).getBlock();
			if (railBlock1 != RedTec.steel_rail && railBlock1 != RedTec.inductive_rail && railBlock2 != RedTec.steel_rail && railBlock2 != RedTec.inductive_rail && (railBlock1 instanceof AbstractRailBlock || railBlock2 instanceof AbstractRailBlock)) {
				
				this.stopBoosted(entry.getKey());
			}
			
		}
		
	}
	
	@Override
	public void read(CompoundNBT nbt) {
//		ListNBT carts = nbt.getList("Minecarts", 10);
//		for (int i = 0; i < carts.size(); i++) {
//			CompoundNBT cart = carts.getCompound(i);
//			String minecart = this.world.get
//			this.boostedMinecarts.put(minecart, cart.getLong("BoostTime"));
//		}
		// TODO How to load Entity from UUID ?!?!?!?
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT nbt = new CompoundNBT();
		ListNBT carts = new ListNBT();
		for (Entry<AbstractMinecartEntity, Long> entry : this.boostedMinecarts.entrySet()) {
			CompoundNBT cart = new CompoundNBT();
			cart.putUniqueId("UUID", entry.getKey().getUniqueID());
			cart.putLong("BoostTime", entry.getValue());
			carts.add(cart);
		}
		nbt.put("Minecarts", carts);
		return nbt;
	}
	
}