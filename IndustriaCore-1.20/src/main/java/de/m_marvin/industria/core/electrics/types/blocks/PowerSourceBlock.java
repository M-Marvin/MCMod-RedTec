package de.m_marvin.industria.core.electrics.types.blocks;

import java.util.function.Consumer;

import de.m_marvin.industria.core.conduits.engine.NodePointSupplier;
import de.m_marvin.industria.core.conduits.types.ConduitNode;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.ElectricUtility;
import de.m_marvin.industria.core.electrics.circuits.CircuitTemplate;
import de.m_marvin.industria.core.electrics.circuits.CircuitTemplateManager;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.electrics.types.blockentities.PowerSourceBlockEntity;
import de.m_marvin.industria.core.parametrics.BlockParametrics;
import de.m_marvin.industria.core.parametrics.BlockParametricsManager;
import de.m_marvin.industria.core.registries.Circuits;
import de.m_marvin.industria.core.registries.NodeTypes;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

public class PowerSourceBlock extends BaseEntityBlock implements IElectricBlock, IElectricInfoProvider {
	
	public static final NodePointSupplier NODES = NodePointSupplier.define()
			.addNode(NodeTypes.ELECTRIC, 8, new Vec3i(8, 8, 0))
			.addModifier(BlockStateProperties.FACING, NodePointSupplier.FACING_MODIFIER_DEFAULT_NORTH);
	
	public PowerSourceBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return defaultBlockState().setValue(BlockStateProperties.FACING, GameUtility.getFacingDirection(pContext.getPlayer()).getOpposite());
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.FACING);
	}
	
	@Override
	public ConduitNode[] getConduitNodes(Level level, BlockPos pos, BlockState state) {
		return NODES.getNodes(state);
	}
	
	@Override
	public void plotCircuit(Level level, BlockState instance, BlockPos position, ElectricNetwork circuit, Consumer<ICircuitPlot> plotter) {

		if (level.getBlockEntity(position) instanceof PowerSourceBlockEntity source) {

			String[] sourceLanes = source.getNodeLanes();
			ElectricUtility.plotJoinTogether(plotter, level, this, position, instance, 0, sourceLanes[0], 1, sourceLanes[1]);
			
			CircuitTemplate templateSource = CircuitTemplateManager.getInstance().getTemplate(Circuits.CURRENT_LIMITED_VOLTAGE_SOURCE);
			templateSource.setProperty("nominal_current", source.getPower() / (double) source.getVoltage());
			templateSource.setProperty("nominal_voltage", source.getVoltage());
			templateSource.setNetworkNode("SHUNT", new NodePos(position, 0), 2, "power_shunt");
			templateSource.setNetworkNode("VDC", new NodePos(position, 0), 0, sourceLanes[0]);
			templateSource.setNetworkNode("GND", new NodePos(position, 0), 1, sourceLanes[1]);
			plotter.accept(templateSource);
			
		}
		
	}

	@Override
	public void onNetworkNotify(Level level, BlockState instance, BlockPos position) {
		GameUtility.triggerUpdate(level, position);
	}
	
	@Override
	public NodePos[] getConnections(Level level, BlockPos pos, BlockState instance) {
		return NODES.getNodePositions(pos);
	}
	
	@Override
	public double getVoltage(BlockState state, Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof PowerSourceBlockEntity source) {
			String[] wireLanes = source.getNodeLanes();
			return ElectricUtility.getVoltageBetween(level, new NodePos(pos, 0), new NodePos(pos, 0), 0, 1, wireLanes[0], wireLanes[1]);
		}
		return 0.0;
	}
	
	@Override
	public double getPower(BlockState state, Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof PowerSourceBlockEntity source) {
			String[] wireLanes = source.getNodeLanes();
			double shuntVoltage = ElectricUtility.getVoltageBetween(level, new NodePos(pos, 0), new NodePos(pos, 0), 2, 0, "power_shunt", wireLanes[0]);
			BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(this);
			double powerUsed = (shuntVoltage / Circuits.SHUNT_RESISTANCE) * parametrics.getNominalVoltage();
			return Math.max(powerUsed > 1.0 ? parametrics.getPowerMin() : 0, powerUsed);
		}
		return 0.0;
	}
	
	@Override
	public BlockParametrics getParametrics(BlockState state, Level level, BlockPos pos) {
		return BlockParametricsManager.getInstance().getParametrics(this);
	}
	
	@Override
	public String[] getWireLanes(Level level, BlockPos pos, BlockState instance, NodePos node) {
		if (level.getBlockEntity(pos) instanceof PowerSourceBlockEntity powerSource) {
			return powerSource.getNodeLanes();
		}
		return new String[0];
	}

	@Override
	public void setWireLanes(Level level, BlockPos pos, BlockState instance, NodePos node, String[] laneLabels) {
		if (level.getBlockEntity(pos) instanceof PowerSourceBlockEntity powerSource) {
			powerSource.getNodeLanes(laneLabels);
		}
	}
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		return GameUtility.openElectricBlockEntityUI(pLevel, pPos, pPlayer, pHand);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new PowerSourceBlockEntity(pPos, pState);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
	
	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.setValue(BlockStateProperties.FACING, pMirror.mirror(pState.getValue(BlockStateProperties.FACING)));
	}
	
	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(BlockStateProperties.FACING, pRotation.rotate(pState.getValue(BlockStateProperties.FACING)));
	}
	
}
