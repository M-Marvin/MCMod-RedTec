package de.m_marvin.industria.content.blocks.machines;

import java.util.function.Consumer;

import de.m_marvin.industria.content.blockentities.machines.IonicThrusterBlockEntity;
import de.m_marvin.industria.content.blocks.AbstractThrusterBlock;
import de.m_marvin.industria.core.conduits.engine.NodePointSupplier;
import de.m_marvin.industria.core.conduits.types.ConduitNode;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.contraptions.ContraptionUtility;
import de.m_marvin.industria.core.contraptions.engine.types.contraption.ServerContraption;
import de.m_marvin.industria.core.electrics.ElectricUtility;
import de.m_marvin.industria.core.electrics.engine.CircuitTemplateManager;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.electrics.types.CircuitTemplate.Plotter;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricBlock;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricInfoProvider;
import de.m_marvin.industria.core.parametrics.BlockParametrics;
import de.m_marvin.industria.core.parametrics.engine.BlockParametricsManager;
import de.m_marvin.industria.core.parametrics.properties.IntegerParameter;
import de.m_marvin.industria.core.registries.Circuits;
import de.m_marvin.industria.core.registries.NodeTypes;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.industria.core.util.VoxelShapeUtility;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IonicThrusterBlock extends AbstractThrusterBlock implements IElectricBlock, IElectricInfoProvider {
	
	public static final NodePointSupplier NODES = NodePointSupplier.define()
			.addNode(NodeTypes.ELECTRIC, 2, new Vec3i(8, 16, 5))
			.addModifier(BlockStateProperties.ATTACH_FACE, NodePointSupplier.ATTACH_FACE_MODIFIER_DEFAULT_WALL)
			.addModifier(BlockStateProperties.HORIZONTAL_FACING, NodePointSupplier.FACING_MODIFIER_DEFAULT_NORTH);
	
	public static final VoxelShape SHAPE = VoxelShapeUtility.box(1, 1, 0, 15, 15, 15);
	
	public static final IntegerParameter PARAMETER_MAX_THRUSTER_THRUST = new IntegerParameter("maxThrusterThrust", 1000);
	
	public IonicThrusterBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (pState.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.WALL) {
			return VoxelShapeUtility.transformation()
					.centered()
					.rotateY(pState.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue() * 90)
					.uncentered()
					.transform(SHAPE);
		} else {
			VoxelShape s = VoxelShapeUtility.transformation()
					.centered()
					.rotateX(pState.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.CEILING ? -90 : 90)
					.rotateY(pState.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue() * 90)
					.uncentered()
					.transform(SHAPE);
			return s;
		}
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new IonicThrusterBlockEntity(pPos, pState);
	}
	
	@Override
	public ConduitNode[] getConduitNodes(Level level, BlockPos pos, BlockState state) {
		return NODES.getNodes(state);
	}

	@Override
	public NodePos[] getConnections(Level level, BlockPos pos, BlockState instance) {
		return NODES.getNodePositions(pos);
	}
	
	@Override
	public void plotCircuit(Level level, BlockState instance, BlockPos position, ElectricNetwork circuit, Consumer<ICircuitPlot> plotter) {
		if (level.getBlockEntity(position) instanceof IonicThrusterBlockEntity thruster) {
			
			String[] thrusterLanes = thruster.getNodeLanes();
			ElectricUtility.plotJoinTogether(plotter, level, this, position, instance, 0, thrusterLanes[0], thrusterLanes[1]);
			
			BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(this);
			int targetVoltage = parametrics.getNominalVoltage();
			int targetPower = parametrics.getNominalPower();
			
			Plotter templateSource = CircuitTemplateManager.getInstance().getTemplate(Circuits.CONSTANT_POWER_LOAD).plotter();
			templateSource.setProperty("nominal_power", targetPower);
			templateSource.setProperty("nominal_voltage", targetVoltage);
			templateSource.setNetworkLocalNode("VDC", position, thrusterLanes[0], 0);
			templateSource.setNetworkLocalNode("GND", position, thrusterLanes[1], 0);
			plotter.accept(templateSource);
			
		}
	}

	@Override
	public double getVoltage(BlockState state, Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof IonicThrusterBlockEntity thruster) {
			String[] wireLanes = thruster.getNodeLanes();
			return ElectricUtility.getVoltageBetweenLocal(level, pos, wireLanes[0], 0, wireLanes[1], 0).orElse(0.0);
		}
		return 0.0;
	}
	
	@Override
	public double getCurrentPower(Level level, BlockPos pos, BlockState instance) {
		if (level.getBlockEntity(pos) instanceof IonicThrusterBlockEntity thruster) {
			String[] wireLanes = thruster.getNodeLanes();
			BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(this);
			double voltage = ElectricUtility.getVoltageBetweenLocal(level, pos, wireLanes[0], 0, wireLanes[1], 0).orElse(0.0);
			return -parametrics.getPowerV(voltage);
		}
		return 0.0;
	}
	
	@Override
	public double getMaxPowerGeneration(Level level, BlockPos pos, BlockState instance) {
		return 0;
	}
	
	@Override
	public void onNetworkNotify(Level level, BlockState instance, BlockPos position) {
		GameUtility.triggerClientSync(level, position);
		level.scheduleTick(position, this, 1);
	}
	
	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pLevel.getBlockEntity(pPos) instanceof IonicThrusterBlockEntity) {
			
			if (!pLevel.isClientSide()) {
				
				double powerP = Math.max(0, BlockParametricsManager.getInstance().getParametrics(this).getPowerPercentageP(getPower(pState, pLevel, pPos)));
				int maxThrust = getThrust(pLevel, pPos, pState);
				double thrust = powerP * maxThrust;
				
				// TODO finish thruster
				ServerContraption contraption = ContraptionUtility.getContraptionOfBlock(pLevel, pPos);
				if (contraption != null) {
					ThrusterInducer inducer = contraption.getAttachment(ThrusterInducer.class);
					inducer.setThruster(pPos, thrust);
				}
				
			}
			
		}
	}
	
	@Override
	public String[] getWireLanes(Level level, BlockPos pos, BlockState instance, NodePos node) {
		if (level.getBlockEntity(pos) instanceof IonicThrusterBlockEntity thruster) {
			return thruster.getNodeLanes();
		}
		return new String[0];
	}

	@Override
	public void setWireLanes(Level level, BlockPos pos, BlockState instance, NodePos node, String[] laneLabels) {
		if (level.getBlockEntity(pos) instanceof IonicThrusterBlockEntity thruster) {
			thruster.setNodeLanes(laneLabels);
		}
	}
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		return GameUtility.openJunctionBlockEntityUI(pLevel, pPos, pPlayer, pHand);
	}
	
	@Override
	public int getThrust(Level level, BlockPos pos, BlockState state) {
		BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(this);
		return parametrics.getParameter(PARAMETER_MAX_THRUSTER_THRUST);
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(BlockStateProperties.HORIZONTAL_FACING, pRotation.rotate(pState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}
	
	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.setValue(BlockStateProperties.HORIZONTAL_FACING, pMirror.mirror(pState.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}
	
}
