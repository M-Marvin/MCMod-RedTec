package de.redtec.tileentity;

import java.util.ArrayList;
import java.util.List;

import de.redtec.blocks.BlockMChunkLoader;
import de.redtec.gui.ContainerMChunkLoader;
import de.redtec.typeregistys.ModTileEntityType;
import de.redtec.util.blockfeatures.IChunkForceLoading;
import de.redtec.util.blockfeatures.IElectricConnectiveBlock.Voltage;
import de.redtec.util.handler.ElectricityNetworkHandler;
import de.redtec.util.handler.ElectricityNetworkHandler.ElectricityNetwork;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TileEntityMChunkLoader extends TileEntity implements IChunkForceLoading, ITickableTileEntity, INamedContainerProvider {
	
	public static final int CHUNK_RANGE = 8;
	
	public boolean hasPower;
	public boolean isWorking;
	public List<ChunkPos> activeRelativeChunks;
	
	public TileEntityMChunkLoader() {
		super(ModTileEntityType.CHUNK_LOADER);
		this.activeRelativeChunks = new ArrayList<ChunkPos>();
	}

	@Override
	public List<ChunkPos> getLoadHoldChunks() {
		List<ChunkPos> chunks = new ArrayList<ChunkPos>();
		if (this.isWorking) {
			ChunkPos ownChunk = new ChunkPos(this.pos);
			this.activeRelativeChunks.forEach((chunk) -> {
				chunks.add(new ChunkPos(chunk.x + ownChunk.x, + chunk.z + ownChunk.z));
			});
		}
		return chunks;
	}
	
	public boolean canWork() {
		return activeRelativeChunks.size() > 0;
	}
	
	public boolean setChunkCactive(ChunkPos relativChunk, boolean active) {
		if (active && !this.activeRelativeChunks.contains(relativChunk)) {
			this.activeRelativeChunks.add(relativChunk);
			return true;
		} else if (!active && this.activeRelativeChunks.contains(relativChunk)) {
			this.activeRelativeChunks.remove(relativChunk);
			return true;
		}
		return false;
	}
	
	@Override
	public void tick() {
		
		if (!this.world.isRemote()) {
			
			ElectricityNetworkHandler.getHandlerForWorld(world).updateNetwork(world, pos);
			ElectricityNetwork network = ElectricityNetworkHandler.getHandlerForWorld(world).getNetwork(pos);
			this.hasPower = network.canMachinesRun() == Voltage.HightVoltage;
			this.isWorking = canWork() && this.hasPower;
			
			this.world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 2);
			
			boolean active = this.getBlockState().get(BlockMChunkLoader.ACTIVE);
			if (active != this.isWorking) world.setBlockState(pos, this.getBlockState().with(BlockMChunkLoader.ACTIVE, this.isWorking));
			
		} else {
			
			
			
		}
		
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		ListNBT chunkList = new ListNBT();
		this.activeRelativeChunks.forEach((chunk) -> {
			ListNBT chunkNBT = new ListNBT();
			chunkNBT.add(IntNBT.valueOf(chunk.x));
			chunkNBT.add(IntNBT.valueOf(chunk.z));
			chunkList.add(chunkNBT);
		});
		compound.put("ActiveChunks", chunkList);
		compound.putBoolean("hasPower", this.hasPower);
		compound.putBoolean("isWorking", this.isWorking);
		return super.write(compound);
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		this.activeRelativeChunks.clear();
		ListNBT chunkList = nbt.getList("ActiveChunks", 9);
		chunkList.forEach((chunkNBT) -> {
			ChunkPos chunk = new ChunkPos(((IntNBT) ((ListNBT) chunkNBT).get(0)).getInt(), ((IntNBT) ((ListNBT) chunkNBT).get(1)).getInt());
			this.activeRelativeChunks.add(chunk);
		});
		this.hasPower = nbt.getBoolean("hasPower");
		this.isWorking = nbt.getBoolean("isWorking");
		super.read(state, nbt);
	}

	@Override
	public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity palyer) {
		return new ContainerMChunkLoader(id, playerInv, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("block.redtec.chunk_loader");
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(pos, 0, this.serializeNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.deserializeNBT(pkt.getNbtCompound());
	}
	
}
