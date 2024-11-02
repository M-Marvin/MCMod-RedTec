package de.m_marvin.industria.core.contraptions.engine.commands.arguments.contraption;

import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.m_marvin.industria.core.contraptions.ContraptionUtility;
import de.m_marvin.industria.core.contraptions.engine.types.ContraptionHitResult;
import de.m_marvin.industria.core.contraptions.engine.types.contraption.Contraption;
import de.m_marvin.univec.impl.Vec3d;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult.Type;

public class ContraptionArgumentClientOnly {
	
	public static void suggestRaycastedContraption(SuggestionsBuilder builder, ClientSuggestionProvider clientprovider) {
		
		Level level = clientprovider.minecraft.level;
		Player player = clientprovider.minecraft.player;
		Vec3d eyePos = Vec3d.fromVec(player.getEyePosition());
		Vec3d direction = Vec3d.fromVec(player.getViewVector(0));
		double range = player.getBlockReach();
		
		ContraptionHitResult result = ContraptionUtility.clipForContraption(level, eyePos, direction, range);
		
		if (result.getType() != Type.MISS) {
			
			Contraption contraption = result.getContraption();
			SharedSuggestionProvider.suggest(Lists.newArrayList(contraption.getIdString(), contraption.getName().getString()), builder);
			
		}
		
	}
	
}
