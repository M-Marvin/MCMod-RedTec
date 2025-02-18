package de.m_marvin.industria.core.conduits.types;

import java.io.StringReader;
import java.util.Objects;
import java.util.Properties;

import de.m_marvin.industria.core.conduits.types.blocks.IConduitConnector;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ConduitPos {
	
	private NodePos nodeA;
	private NodePos nodeB;
	
	public ConduitPos(BlockPos nodeApos, BlockPos nodeBpos, int nodeAid, int nodeBid) {
		this.nodeA = new NodePos(nodeApos, nodeAid);
		this.nodeB = new NodePos(nodeBpos, nodeBid);
	}

	public ConduitPos(NodePos nodeApos, NodePos nodeBpos) {
		this.nodeA = nodeApos;
		this.nodeB = nodeBpos;
	}
	
	public double calculateMinConduitLength(Level level) {
		
		BlockState nodeAstate = level.getBlockState(this.nodeA.getBlock());
		BlockState nodeBstate = level.getBlockState(this.nodeB.getBlock());
		if (nodeAstate.getBlock() instanceof IConduitConnector nodeAconnector && nodeBstate.getBlock() instanceof IConduitConnector nodeBconnector) {
			ConduitNode nodeA = nodeAconnector.getConduitNode(level, this.nodeA.getBlock(), nodeAstate, this.nodeA.getNode());
			ConduitNode nodeB = nodeBconnector.getConduitNode(level, this.nodeB.getBlock(), nodeBstate, this.nodeB.getNode());
			if (nodeA != null && nodeB != null) {
				return nodeA.getWorldPosition(level, this.nodeA.getBlock()).dist(nodeB.getWorldPosition(level, this.nodeB.getBlock()));
			}
		}
		return -1;
	}
	
	public Vec3d calculateWorldNodeA(Level level) {
		BlockState nodeAstate = level.getBlockState(this.nodeA.getBlock());
		if (nodeAstate.getBlock() instanceof IConduitConnector nodeAconnector) {
			ConduitNode nodeA = nodeAconnector.getConduitNode(level, this.nodeA.getBlock(), nodeAstate, this.nodeA.getNode());
			if (nodeA != null && this.nodeA != null) {
				return nodeA.getWorldPosition(level, this.nodeA.getBlock());
			}
		}
		return new Vec3d(0, 0, 0);
	}

	public Vec3d calculateContraptionNodeA(Level level) {
		BlockState nodeAstate = level.getBlockState(this.nodeA.getBlock());
		if (nodeAstate.getBlock() instanceof IConduitConnector nodeAconnector) {
			ConduitNode nodeA = nodeAconnector.getConduitNode(level, this.nodeA.getBlock(), nodeAstate, this.nodeA.getNode());
			if (nodeA != null && this.nodeA != null) {
				return nodeA.getContraptionPosition(this.nodeA.getBlock());
			}
		}
		return new Vec3d(0, 0, 0);
	}

	public Vec3d calculateWorldNodeB(Level level) {
		BlockState nodeBstate = level.getBlockState(this.nodeB.getBlock());
		if (nodeBstate.getBlock() instanceof IConduitConnector nodeBconnector) {
			ConduitNode nodeB = nodeBconnector.getConduitNode(level, this.nodeB.getBlock(), nodeBstate, this.nodeB.getNode());
			if (nodeB != null && this.nodeB != null) {
				return nodeB.getWorldPosition(level, this.nodeB.getBlock());
			}
		}
		return new Vec3d(0, 0, 0);
	}

	public Vec3d calculateContraptionNodeB(Level level) {
		BlockState nodeBstate = level.getBlockState(this.nodeB.getBlock());
		if (nodeBstate.getBlock() instanceof IConduitConnector nodeBconnector) {
			ConduitNode nodeB = nodeBconnector.getConduitNode(level, this.nodeB.getBlock(), nodeBstate, this.nodeB.getNode());
			if (nodeB != null && this.nodeB != null) {
				return nodeB.getContraptionPosition(this.nodeB.getBlock());
			}
		}
		return new Vec3d(0, 0, 0);
	}
	
	public NodePos getNodeA() {
		return nodeA;
	}
	
	public NodePos getNodeB() {
		return nodeB;
	}
	
	public BlockPos getNodeApos() {
		return nodeA.getBlock();
	}
	
	public BlockPos getNodeBpos() {
		return nodeB.getBlock();
	}
	
	public int getNodeAid() {
		return nodeA.getNode();
	}
	
	public int getNodeBid() {
		return nodeB.getNode();
	}
	
	public void write(FriendlyByteBuf buff) {
		this.nodeA.write(buff);
		this.nodeB.write(buff);
	}
	
	public static ConduitPos read(FriendlyByteBuf buff) {
		NodePos nodeA = NodePos.read(buff);
		NodePos nodeB = NodePos.read(buff);
		return new ConduitPos(nodeA, nodeB);
	}
	
	public CompoundTag writeNBT(CompoundTag nbt) {
		return writeNBT(nbt, BlockPos.ZERO);
	}
	
	public CompoundTag writeNBT(CompoundTag nbt, BlockPos relative) {
		nbt.put("NodeA", this.nodeA.writeNBT(new CompoundTag(), relative));
		nbt.put("NodeB", this.nodeB.writeNBT(new CompoundTag(), relative));
		return nbt;
	}
	
	public static ConduitPos readNBT(CompoundTag nbt) {
		return readNBT(nbt, BlockPos.ZERO);
	}
	
	public static ConduitPos readNBT(CompoundTag nbt, BlockPos relative) {
		NodePos nodeA = NodePos.readNBT(nbt.getCompound("NodeA"), relative);
		NodePos nodeB = NodePos.readNBT(nbt.getCompound("NodeB"), relative);
		return new ConduitPos(nodeA, nodeB);
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((this.nodeA != null) ? 0 : this.nodeA.hashCode());
		result = prime * result + ((this.nodeB != null) ? 0 : this.nodeB.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConduitPos) {
			ConduitPos other = (ConduitPos) obj;
			return 	(other.nodeA.equals(nodeA) && other.nodeB.equals(nodeB)) ||
					(other.nodeB.equals(nodeA) && other.nodeA.equals(nodeB));
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ConduitPos{A=" + this.nodeA.toString() + ",B=" + this.nodeB.toString() + "}";
	}
	
	public static class NodePos {
		
		public static final NodePos DUMMY = new NodePos(BlockPos.ZERO, 0);
		
		protected BlockPos block;
		protected int node;
		
		public NodePos(BlockPos block, int node) {
			this.block = block;
			this.node = node;
		}
		
		public BlockPos getBlock() {
			return block;
		}
		
		public int getNode() {
			return node;
		}

		public void write(FriendlyByteBuf buff) {
			buff.writeBlockPos(block);
			buff.writeInt(node);
		}
		
		public static NodePos read(FriendlyByteBuf buff) {
			BlockPos block = buff.readBlockPos();
			int node = buff.readInt();
			return new NodePos(block, node);
		}
		
		public CompoundTag writeNBT(CompoundTag nbt) {
			return writeNBT(nbt, BlockPos.ZERO);
		}
		
		public CompoundTag writeNBT(CompoundTag nbt, BlockPos relative) {
			nbt.put("Pos", NbtUtils.writeBlockPos(this.block.subtract(relative)));
			nbt.putInt("Id", this.node);
			return nbt;
		}
		
		public static NodePos readNBT(CompoundTag nbt) {
			return readNBT(nbt, BlockPos.ZERO);
		}
		
		public static NodePos readNBT(CompoundTag nbt, BlockPos relative) {
			return new NodePos(NbtUtils.readBlockPos(nbt.getCompound("Pos")).offset(relative), nbt.getInt("Id"));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.block, this.node);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NodePos other) {
				return other.block.equals(this.block) && other.node == node;
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "NodePos{block=[" + this.block.getX() + " " + block.getY() + " " + block.getZ() + "],node=" + this.node + "}";
		}
		
		public static String getLaneName(String keyString) {
			try {
				Properties props = new Properties();
				props.load(new StringReader(keyString));
				return props.getProperty("lane");
			} catch (Exception e) {
				return "";
			}
		}
		
		public static NodePos getFromKeyString(String keyString) {
			try {
				Properties props = new Properties();
				props.load(new StringReader(keyString));
				BlockPos position = BlockPos.of(Long.valueOf(props.getProperty("pos")));
				int node = Integer.valueOf(props.getProperty("id"));
				return new NodePos(position, node);
			} catch (Exception e) {
				return null;
			}
		}
		
	}
	
}
