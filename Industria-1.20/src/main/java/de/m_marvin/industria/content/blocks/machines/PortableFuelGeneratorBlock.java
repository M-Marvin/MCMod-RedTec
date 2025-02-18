package de.m_marvin.industria.content.blocks.machines;

import java.util.function.Consumer;

import de.m_marvin.industria.content.blockentities.machines.PortableFuelGeneratorBlockEntity;
import de.m_marvin.industria.content.registries.ModBlockEntityTypes;
import de.m_marvin.industria.core.conduits.engine.NodePointSupplier;
import de.m_marvin.industria.core.conduits.types.ConduitNode;
import de.m_marvin.industria.core.conduits.types.ConduitPos.NodePos;
import de.m_marvin.industria.core.electrics.ElectricUtility;
import de.m_marvin.industria.core.electrics.engine.CircuitTemplateManager;
import de.m_marvin.industria.core.electrics.engine.ElectricNetwork;
import de.m_marvin.industria.core.electrics.types.CircuitTemplate.Plotter;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricBlock;
import de.m_marvin.industria.core.electrics.types.blocks.IElectricInfoProvider;
import de.m_marvin.industria.core.parametrics.BlockParametrics;
import de.m_marvin.industria.core.parametrics.engine.BlockParametricsManager;
import de.m_marvin.industria.core.registries.Circuits;
import de.m_marvin.industria.core.registries.NodeTypes;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.univec.impl.Vec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

public class PortableFuelGeneratorBlock extends BaseEntityBlock implements IElectricBlock, IElectricInfoProvider {
	
	public static final NodePointSupplier NODES = NodePointSupplier.define()
			.addNode(NodeTypes.ELECTRIC, 4, new Vec3i(8, 8, 16))
			.addModifier(BlockStateProperties.HORIZONTAL_FACING, NodePointSupplier.FACING_HORIZONTAL_MODIFIER_DEFAULT_NORTH);
	
	public PortableFuelGeneratorBlock(Properties pProperties) {
		super(pProperties);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.MODEL;
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(BlockStateProperties.HORIZONTAL_FACING);
		pBuilder.add(BlockStateProperties.LIT);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.LIT, false);
	}
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		return GameUtility.openElectricBlockEntityUI(pLevel, pPos, pPlayer, pHand);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new PortableFuelGeneratorBlockEntity(pPos, pState);
	}
	
	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
		
		if (pState.getValue(BlockStateProperties.LIT)) {
			
			double d0 = (double)pPos.getX() + 0.5D;
			double d1 = (double)pPos.getY();
			double d2 = (double)pPos.getZ() + 0.5D;
			
			double power = getPower(pState, pLevel, pPos);
			BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(this);
			double loadP = Math.max(0, parametrics.getPowerPercentageP(power) - 1);
			
			if (loadP < pRandom.nextFloat()) return;

			// TODO fuel generator sound
			//pLevel.playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			
			Direction direction = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);
			Direction.Axis direction$axis = direction.getAxis();
			double d4 = pRandom.nextDouble() * 0.5D - 0.25D;
			double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
			double d6 = pRandom.nextDouble() * 0.2D  + 0.2;
			double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
			pLevel.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.1D, 0.0D);
			
		}
		
	}
		
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return createTickerHelper(pBlockEntityType, ModBlockEntityTypes.PORTABLE_FUEL_GENERATOR.get(), PortableFuelGeneratorBlockEntity::tick);
	}
	
	@Override
	public ConduitNode[] getConduitNodes(Level level, BlockPos pos, BlockState state) {
		return NODES.getNodes(state);
	}

	@Override
	public void plotCircuit(Level level, BlockState instance, BlockPos position, ElectricNetwork circuit, Consumer<ICircuitPlot> plotter) {
		
		if (level.getBlockEntity(position) instanceof PortableFuelGeneratorBlockEntity generator) {
			
			String[] wireLanes = generator.getNodeLanes();
			ElectricUtility.plotJoinTogether(plotter, level, this, position, instance, 0, wireLanes[0], wireLanes[1]);
			
			BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(this);
			int targetPower = generator.canRun() ? parametrics.getNominalPower() : 0;
			int targetVoltage = generator.canRun() ? parametrics.getNominalVoltage() : 0;
			
			if (targetPower > 0) {
				Plotter templateSource = CircuitTemplateManager.getInstance().getTemplate(Circuits.VOLTAGE_SOURCE).plotter();
				templateSource.setProperty("nominal_voltage", targetVoltage);
				templateSource.setProperty("power_limit", targetVoltage > 0 ? targetPower : 0);
				templateSource.setNetworkLocalNode("VDC", position, wireLanes[0], 0);
				templateSource.setNetworkLocalNode("GND", position, wireLanes[1], 0);
				templateSource.setNetworkLocalNode("SHUNT", position, "SHUNT", 1);
				plotter.accept(templateSource);
			}
			
		}
		
	}
	
	@Override
	public void onNetworkNotify(Level level, BlockState instance, BlockPos position) {
		GameUtility.triggerClientSync(level, position);
	}
	
	@Override
	public double getVoltage(BlockState state, Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof PortableFuelGeneratorBlockEntity generator) {
			String[] wireLanes = generator.getNodeLanes();
			return ElectricUtility.getVoltageBetweenLocal(level, pos, wireLanes[0], 0, wireLanes[1], 0).orElse(0.0);
		}
		return 0.0;
	}
	
	@Override
	public double getCurrentPower(Level level, BlockPos pos, BlockState instance) {
		if (level.getBlockEntity(pos) instanceof PortableFuelGeneratorBlockEntity generator) {
			String[] wireLanes = generator.getNodeLanes();
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
		BlockParametrics parametrics = BlockParametricsManager.getInstance().getParametrics(this);
		return parametrics.getPowerMax();
	}
	
	@Override
	public NodePos[] getConnections(Level level, BlockPos pos, BlockState instance) {
		return NODES.getNodePositions(pos);
	}

	@Override
	public String[] getWireLanes(Level level, BlockPos pos, BlockState instance, NodePos node) {
		if (level.getBlockEntity(pos) instanceof PortableFuelGeneratorBlockEntity generator) {
			return generator.getNodeLanes();
		}
		return new String[0];
	}

	@Override
	public void setWireLanes(Level level, BlockPos pos, BlockState instance, NodePos node, String[] laneLabels) {
		if (level.getBlockEntity(pos) instanceof PortableFuelGeneratorBlockEntity generator) {
			generator.setNodeLanes(laneLabels);
		}
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
