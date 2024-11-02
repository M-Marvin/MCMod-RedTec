package de.m_marvin.industria.core.contraptions.engine.commands.arguments.contraption;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.m_marvin.industria.core.contraptions.ContraptionUtility;
import de.m_marvin.industria.core.contraptions.engine.types.contraption.ServerContraption;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ContraptionSelector {
	
	public static final BiConsumer<Vec3, List<? extends ServerContraption>> ORDER_ARBITRARY = (p_261404_, p_261405_) -> {
	};
		
	private final int maxResults;
	private final boolean worldLimited;
	private final Predicate<ServerContraption> predicate;
	private final MinMaxBounds.Doubles range;
	private final MinMaxBounds.Doubles mass;
	private final MinMaxBounds.Doubles size;
	private final Optional<Boolean> isStatic;
	private final Function<Vec3, Vec3> position;
	@Nullable
	private final AABB aabb;
	private final BiConsumer<Vec3, List<? extends ServerContraption>> order;
	private final boolean currentContraption;
	@Nullable
	private final String contraptionName;
	@Nullable
	private final OptionalLong contraptionId;
	private final boolean usesSelector;
	 
	public ContraptionSelector(int pMaxResults, boolean pWorldLimited, Predicate<ServerContraption> predicate, MinMaxBounds.Doubles pRange, MinMaxBounds.Doubles pMass, MinMaxBounds.Doubles pSize, Optional<Boolean> pStatic, Function<Vec3, Vec3> pPositions, @Nullable AABB pAabb, BiConsumer<Vec3, List<? extends ServerContraption>> order, boolean pCurrentContraption, @Nullable String pContraptionName, @Nullable OptionalLong contraptionId, boolean pUsesSelector) {
		this.maxResults = pMaxResults;
		this.worldLimited = pWorldLimited;
		this.predicate = predicate;
		this.range = pRange;
		this.mass = pMass;
		this.size = pSize;
		this.isStatic = pStatic;
		this.position = pPositions;
		this.aabb = pAabb;
		this.order = order;
		this.currentContraption = pCurrentContraption;
		this.contraptionName = pContraptionName;
		this.contraptionId = contraptionId;
		this.usesSelector = pUsesSelector;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public boolean isWorldLimited() {
		return worldLimited;
	}

	public Predicate<ServerContraption> getPredicate() {
		return predicate;
	}

	public MinMaxBounds.Doubles getRange() {
		return range;
	}

	public MinMaxBounds.Doubles getMass() {
		return mass;
	}

	public MinMaxBounds.Doubles getSize() {
		return size;
	}

	public Optional<Boolean> getIsStatic() {
		return isStatic;
	}

	public Function<Vec3, Vec3> getPosition() {
		return position;
	}

	public AABB getAabb() {
		return aabb;
	}

	public BiConsumer<Vec3, List<? extends ServerContraption>> getOrder() {
		return order;
	}

	public boolean isCurrentContraption() {
		return currentContraption;
	}

	public String getContraptionName() {
		return contraptionName;
	}

	public OptionalLong getContraptionId() {
		return contraptionId;
	}

	public boolean isUsesSelector() {
		return usesSelector;
	}
	
	private Predicate<ServerContraption> getPredicateWithAdditonal(Vec3 position) {
		Predicate<ServerContraption> predicate = this.predicate;
		if (this.aabb != null) {
			AABB aabb = this.aabb.move(position);
			predicate = predicate.and((contraption) -> {
				return aabb.intersects(contraption.getContraptionHitboxInWorldBoundsV());
			});
		}
		
		if (!this.range.isAny()) {
			predicate = predicate.and((contraption) -> {
				return this.range.matchesSqr(position.distanceToSqr(contraption.getPosition().getPosition().writeTo(new Vec3(0, 0, 0))));
			});
		}
		
		if (!this.mass.isAny()) {
			predicate = predicate.and((contraption) -> {
				return this.mass.matches(contraption.getMass());
			});
		}

		if (!this.size.isAny()) {
			predicate = predicate.and((contraption) -> {
				return this.size.matches(contraption.getSize());
			});
		}

		if (this.isStatic.isPresent()) {
			predicate = predicate.and((contraption) -> {
				return this.isStatic.get() == contraption.isStatic();
			});
		}
		
		return predicate;
	}
	
	private void checkPermissions(CommandSourceStack pSource) throws CommandSyntaxException {
		if (this.usesSelector && !net.minecraftforge.common.ForgeHooks.canUseEntitySelectors(pSource)) {
			throw EntityArgument.ERROR_SELECTORS_NOT_ALLOWED.create();
		}
	}

	public ServerContraption findSingleContraption(CommandSourceStack source) throws CommandSyntaxException {
		this.checkPermissions(source);
		List<ServerContraption> list = findContraptions(source);
		if (list.isEmpty()) {
			throw ContraptionArgument.NO_CONTRAPTIONS_FOUND.create();
		} else if (list.size() > 1) {
			throw ContraptionArgument.ERROR_NOT_SINGLE_CONTRAPTION.create();
		} else {
			return list.get(0);
		}
	}
	
	private void addContraptions(List<ServerContraption> list, ServerLevel level, Vec3 position, Predicate<ServerContraption> predicate) {
		int i = this.order == ORDER_ARBITRARY ? this.maxResults : Integer.MAX_VALUE;
		if (list.size() < i) {
			List<ServerContraption> list2 = ContraptionUtility.getAllContraptions(level, true);
			list.addAll(list2.stream().filter(predicate).toList());
		}
	}
	
	public List<ServerContraption> findContraptions(CommandSourceStack source) throws CommandSyntaxException {
		this.checkPermissions(source);
		
		if (this.contraptionName != null) {
			return ContraptionUtility.getContraptionsWithName(source.getLevel(), this.contraptionName);
		} else if (this.contraptionId.isPresent()) {
			ServerContraption contraption = ContraptionUtility.getContraptionById(source.getLevel(), this.contraptionId.getAsLong());
			return contraption == null ? Collections.emptyList() : Lists.newArrayList(contraption);
		} else {
			
			Vec3 position = this.position.apply(source.getPosition());
			Predicate<ServerContraption> predicate = this.getPredicateWithAdditonal(position);
			
			if (this.currentContraption) {
				ServerContraption contraption = ContraptionUtility.getContraptionOfBlock(source.getLevel(), BlockPos.containing(source.getPosition().x, source.getPosition().y, source.getPosition().z));
				return contraption == null ? Collections.emptyList() : Lists.newArrayList(contraption);
			} else {
				
				List<ServerContraption> list = Lists.newArrayList();
				if (this.isWorldLimited()) {
					this.addContraptions(list, source.getLevel(), position, predicate);
				} else {
					for (ServerLevel serverLevel : source.getServer().getAllLevels()) {
						this.addContraptions(list, serverLevel, position, predicate);
					}
				}
				return this.sortAndLimit(position, list);
			}
		}
		
	}
	
	private List<ServerContraption> sortAndLimit(Vec3 position, List<ServerContraption> contraptions) {
		if (contraptions.size() > 1) {
			this.order.accept(position, contraptions);
		}
		return contraptions.subList(0, Math.min(this.maxResults, contraptions.size()));
	}
	
}
