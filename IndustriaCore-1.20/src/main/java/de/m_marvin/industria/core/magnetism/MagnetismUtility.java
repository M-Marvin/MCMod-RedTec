package de.m_marvin.industria.core.magnetism;

import de.m_marvin.industria.IndustriaCore;
import de.m_marvin.industria.core.magnetism.engine.MagnetismHandlerCapability;
import de.m_marvin.industria.core.magnetism.engine.network.SUpdateMagneticFieldPackage;
import de.m_marvin.industria.core.magnetism.types.MagneticField;
import de.m_marvin.industria.core.magnetism.types.MagneticFieldInfluence;
import de.m_marvin.industria.core.magnetism.types.blocks.IMagneticBlock;
import de.m_marvin.industria.core.parametrics.engine.BlockParametricsManager;
import de.m_marvin.industria.core.registries.Capabilities;
import de.m_marvin.industria.core.registries.Tags;
import de.m_marvin.industria.core.util.GameUtility;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

public class MagnetismUtility {
	
	private MagnetismUtility() {}
	
	public static void removeFieldInfluence(Level level, BlockPos pos) {
		MagnetismHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.MAGNETISM_HANDLER_CAPABILITY);
		handler.removeFieldInfluence(pos);
	}

	public static void setFieldInfluence(Level level, MagneticFieldInfluence influence) {
		MagnetismHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.MAGNETISM_HANDLER_CAPABILITY);
		handler.setFieldInfluence(influence);
	}
	
	public static void updateField(Level level, BlockPos pos) {
		MagnetismHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.MAGNETISM_HANDLER_CAPABILITY);
		if (!level.isClientSide()) {
			IndustriaCore.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), new SUpdateMagneticFieldPackage(pos));
		}
		handler.updateField(pos);
	}
	
	public static MagneticField getMagneticFieldAt(Level level, BlockPos pos) {
		MagnetismHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.MAGNETISM_HANDLER_CAPABILITY);
		return handler.getFieldAt(pos);
	}
	
	public static MagneticFieldInfluence getMagneticInfluenceOf(Level level, BlockPos pos) {
		MagnetismHandlerCapability handler = GameUtility.getLevelCapability(level, Capabilities.MAGNETISM_HANDLER_CAPABILITY);
		return handler.getInfluenceOf(pos);
	}
	
	public static Vec3d getBlockField(Level level, BlockState state, BlockPos pos) {
		if (state.is(Tags.Blocks.MAGNETIC)) {
			if (state.getBlock() instanceof IMagneticBlock magnetic) {
				return magnetic.getFieldVector(level, state, pos);
			} else {
				return BlockParametricsManager.getInstance().getParametrics(state.getBlock()).getMagneticVector();
			}
		}
		return new Vec3d(0, 0, 0);
	}

	public static double getBlockCoefficient(Level level, BlockState state, BlockPos pos) {
		if (state.is(Tags.Blocks.MAGNETIC)) {
			if (state.getBlock() instanceof IMagneticBlock magnetic) {
				return magnetic.getCoefficient(level, state, pos);
			} else {
				return BlockParametricsManager.getInstance().getParametrics(state.getBlock()).getMagneticCoefficient();
			}
		}
		return 1.0;
	}
	
}
