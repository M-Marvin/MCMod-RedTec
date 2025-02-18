package de.m_marvin.industria.core.electrics.types.blockentities;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import de.m_marvin.industria.core.conduits.types.ConduitNode;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricBlock;
import de.m_marvin.industria.core.electrics.types.containers.JunctionBoxContainer;
import de.m_marvin.industria.core.electrics.types.containers.JunctionBoxContainer.ExternalNodeConstructor;
import de.m_marvin.industria.core.electrics.types.containers.JunctionBoxContainer.InternalNodeConstructor;
import de.m_marvin.industria.core.registries.BlockEntityTypes;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.industria.core.util.MathUtility;
import de.m_marvin.industria.core.util.types.PlanarDirection;
import de.m_marvin.univec.impl.Vec2i;
import de.m_marvin.univec.impl.Vec3d;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class JunctionBoxBlockEntity extends BlockEntity implements MenuProvider, IJunctionEdit {
	
	public JunctionBoxBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityTypes.JUNCTION_BOX.get(), pPos, pBlockState);
	}

	public JunctionBoxBlockEntity(BlockEntityType<?> blockEntityType,BlockPos pPos, BlockState pBlockState) {
		super(blockEntityType, pPos, pBlockState);
	}
	
	@Override
	public Component getDisplayName() {
		return Component.empty();
	}
	
	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		return GameUtility.openJunctionScreenOr(this, pContainerId, pPlayer, pPlayerInventory, () -> null);
	}
	
	@Override
	protected void saveAdditional(CompoundTag pTag) {
		super.saveAdditional(pTag);
	}
	
	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
	}
	
	@Override
	public NodePos[] getEditCableNodes(Direction playerFacing, Direction playerHorizontalFacing) {
		
		Level level = this.getJunctionLevel();
		BlockPos pos = this.getJunctionBlockPos();
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof IElectricBlock) {
			
			Map<Direction, NodePos> cableNodes = getBlockRelativeCableNodes(state, pos);
			Axis blockAxis = Stream.of(Direction.values()).filter(d -> !cableNodes.containsKey(d)).findAny().orElseGet(() -> Direction.NORTH).getAxis();
			Axis playerAxis = playerFacing.getAxis();
			
			if (blockAxis == playerAxis) {
				if (blockAxis.isHorizontal()) {
					Direction left = playerFacing.getCounterClockWise();
					return new NodePos[] {
						cableNodes.get(Direction.UP),
						cableNodes.get(Direction.DOWN),
						cableNodes.get(left),
						cableNodes.get(left.getOpposite())
					};
				} else if (playerFacing.getAxisDirection() == AxisDirection.POSITIVE) {
					return new NodePos[] {
						cableNodes.get(playerHorizontalFacing.getOpposite()),
						cableNodes.get(playerHorizontalFacing),
						cableNodes.get(playerHorizontalFacing.getCounterClockWise()),
						cableNodes.get(playerHorizontalFacing.getClockWise())
					};
				} else {
					return new NodePos[] {
							cableNodes.get(playerHorizontalFacing),
							cableNodes.get(playerHorizontalFacing.getOpposite()),
							cableNodes.get(playerHorizontalFacing.getCounterClockWise()),
							cableNodes.get(playerHorizontalFacing.getClockWise())
						};
					}
			} else if (blockAxis.isVertical() && playerAxis.isHorizontal()) {
				return new NodePos[] {
					cableNodes.get(playerHorizontalFacing),
					cableNodes.get(playerHorizontalFacing.getOpposite()),
					cableNodes.get(playerHorizontalFacing.getCounterClockWise()),
					cableNodes.get(playerHorizontalFacing.getClockWise())
				};
			} else if (blockAxis.isHorizontal() && playerAxis.isHorizontal()) {
				return new NodePos[] {
					cableNodes.get(Direction.UP),
					cableNodes.get(Direction.DOWN),
					cableNodes.get(playerHorizontalFacing),
					cableNodes.get(playerHorizontalFacing.getOpposite())
				};
			} else {
				Direction left = this.getBlockState().getValue(BlockStateProperties.FACING).getCounterClockWise();
				Direction left2 = playerHorizontalFacing.getCounterClockWise();
				if (left2.getOpposite() == left) left = left2;
				return new NodePos[] {
					cableNodes.get(Direction.UP),
					cableNodes.get(Direction.DOWN),
					cableNodes.get(left),
					cableNodes.get(left.getOpposite())
				};
			}
			
		}
		
		return new NodePos[] {};
		
	}
	
	public Map<Direction, NodePos> getBlockRelativeCableNodes(BlockState state, BlockPos position) {
		if (state.getBlock() instanceof IElectricBlock connectorBlock) {
			Level level = this.getJunctionLevel();
			
			BlockPos masterPos = connectorBlock.getConnectorMasterPos(level, position, state);
			if (!masterPos.equals(position) && level.getBlockState(masterPos).getBlock() instanceof IElectricBlock masterBlock) {
				state = level.getBlockState(masterPos);
				position = masterPos;
				connectorBlock = masterBlock;
			}
			
			NodePos[] nodes = connectorBlock.getConnections(level, position, state);
			ConduitNode[] connections = connectorBlock.getConduitNodes(level, position, state);
			Vec3i center = new Vec3i(8, 8, 8);
			Direction blockFacing = state.getValue(BlockStateProperties.FACING);
			switch (blockFacing.getAxis()) {
				case X: center.setX(blockFacing.getAxisDirection() == AxisDirection.NEGATIVE ? 0 : 16); break;
				case Y: center.setY(blockFacing.getAxisDirection() == AxisDirection.NEGATIVE ? 0 : 16); break;
				case Z: center.setZ(blockFacing.getAxisDirection() == AxisDirection.NEGATIVE ? 0 : 16); break;
			}
			Map<Direction, NodePos> cables = new HashMap<>();
			for (int i = 0; i < connections.length; i++) {
				Vec3d blockPos = Vec3d.fromVec(position);
				Vec3d centerPos = blockPos.add(new Vec3d(center).div(16D));
				Vec3d nodePos = blockPos.add(new Vec3d(connections[i].getOffset()).div(16D));
				Vec3d dVf = nodePos.sub(centerPos);
				Direction d = MathUtility.getVecDirection(dVf);
				cables.put(d, nodes[i]);
			}
			return cables;
		}
		return new HashMap<>();
	}

	@Override
	public <B extends BlockEntity & IJunctionEdit> void setupScreenConduitNodes(JunctionBoxContainer<B> junctionBoxContainer, NodePos[] conduitNodes, ExternalNodeConstructor externalNodeConstructor, InternalNodeConstructor internalNodeConstructor) {
		externalNodeConstructor.construct(new Vec2i(69, 8), 	PlanarDirection.Y_POS, 	conduitNodes[0]);
		externalNodeConstructor.construct(new Vec2i(69, 112), 	PlanarDirection.Y_NEG, 	conduitNodes[1]);
		externalNodeConstructor.construct(new Vec2i(8, 69), 	PlanarDirection.X_NEG, 	conduitNodes[2]);
		externalNodeConstructor.construct(new Vec2i(112, 69), 	PlanarDirection.X_POS, 	conduitNodes[3]);
	}
	
	@Override
	public boolean connectsOnlyToInternal() {
		return false;
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
