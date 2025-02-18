package de.m_marvin.industria.core.electrics.types.blocks;

import java.util.function.Consumer;

import de.m_marvin.industria.core.client.util.TooltipAdditions;
import de.m_marvin.industria.core.conduits.engine.NodePointSupplier;
import de.m_marvin.industria.core.conduits.types.ConduitNode;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.ElectricUtility;
import de.m_marvin.industria.core.electrics.engine.CircuitTemplateManager;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.electrics.types.CircuitTemplate.Plotter;
import de.m_marvin.industria.core.electrics.types.blockentities.VoltageSourceBlockEntity;
import de.m_marvin.industria.core.parametrics.BlockParametrics;
import de.m_marvin.industria.core.parametrics.engine.BlockParametricsManager;
import de.m_marvin.industria.core.registries.Circuits;
import de.m_marvin.industria.core.registries.NodeTypes;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.industria.core.util.MathUtility;
import de.m_marvin.industria.core.util.items.ITooltipAdditionsModifier;
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

public class VoltageSourceBlock extends BaseEntityBlock implements IElectricBlock, IElectricInfoProvider, ITooltipAdditionsModifier {
	
	public static final NodePointSupplier NODES = NodePointSupplier.define()
			.addNode(NodeTypes.ELECTRIC, 8, new Vec3i(8, 8, 0))
			.addModifier(BlockStateProperties.FACING, NodePointSupplier.FACING_MODIFIER_DEFAULT_NORTH);
	
	public VoltageSourceBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public boolean showTooltipType(String tooltipTypeName) {
		return tooltipTypeName != TooltipAdditions.TOOLTIP_ELECTRICS;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return defaultBlockState().setValue(BlockStateProperties.FACING, MathUtility.getFacingDirection(pContext.getPlayer()).getOpposite());
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

		if (level.getBlockEntity(position) instanceof VoltageSourceBlockEntity source) {

			String[] sourceLanes = source.getNodeLanes();
			ElectricUtility.plotJoinTogether(plotter, level, this, position, instance, 0, sourceLanes[0], sourceLanes[1]);
			
			if (source.getPower() > 0) {
				Plotter templateSource = CircuitTemplateManager.getInstance().getTemplate(Circuits.VOLTAGE_SOURCE).plotter();
				templateSource.setProperty("nominal_voltage", source.getVoltage());
				templateSource.setProperty("power_limit", source.getPower());
				templateSource.setNetworkLocalNode("VDC", position, sourceLanes[0], 0);
				templateSource.setNetworkLocalNode("GND", position, sourceLanes[1], 0);
				templateSource.setNetworkLocalNode("SHUNT", position, "SHUNT", 1);
				plotter.accept(templateSource);
			}
			
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
		if (level.getBlockEntity(pos) instanceof VoltageSourceBlockEntity source) {
			String[] wireLanes = source.getNodeLanes();
			return ElectricUtility.getVoltageBetweenLocal(level, pos, wireLanes[0], 0, wireLanes[1], 0).orElseGet(() -> 0.0);
		}
		return 0.0;
	}
	
	@Override
	public double getCurrentPower(Level level, BlockPos pos, BlockState instance) {
		if (level.getBlockEntity(pos) instanceof VoltageSourceBlockEntity source) {
			String[] wireLanes = source.getNodeLanes();
			double shuntVoltage = ElectricUtility.getVoltageBetweenLocal(level, pos, "SHUNT", 1, wireLanes[0], 0).orElse(0.0);
			double sourceVoltage = ElectricUtility.getVoltageBetweenLocal(level, pos, wireLanes[0], 0, wireLanes[1], 0).orElse(0.0);
			double sourceCurrent = shuntVoltage / Circuits.SHUNT_RESISTANCE;
			BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(this);
			double powerUsed = Math.min(sourceVoltage * sourceCurrent, parametrics.getPowerMax());
			return Math.max(powerUsed > 1.0 ? parametrics.getPowerMin() : 0, powerUsed);
		}
		return 0.0;
	}
	
	@Override
	public double getMaxPowerGeneration(Level level, BlockPos pos, BlockState instance) {
		if (level.getBlockEntity(pos) instanceof VoltageSourceBlockEntity source) {
			return source.getPower();
		}
		return 0.0;
	}
	
	@Override
	public String[] getWireLanes(Level level, BlockPos pos, BlockState instance, NodePos node) {
		if (level.getBlockEntity(pos) instanceof VoltageSourceBlockEntity powerSource) {
			return powerSource.getNodeLanes();
		}
		return new String[0];
	}

	@Override
	public void setWireLanes(Level level, BlockPos pos, BlockState instance, NodePos node, String[] laneLabels) {
		if (level.getBlockEntity(pos) instanceof VoltageSourceBlockEntity powerSource) {
			powerSource.getNodeLanes(laneLabels);
		}
	}
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		return GameUtility.openElectricBlockEntityUI(pLevel, pPos, pPlayer, pHand);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new VoltageSourceBlockEntity(pPos, pState);
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
