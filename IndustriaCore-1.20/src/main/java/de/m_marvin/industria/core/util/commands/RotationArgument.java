package de.m_marvin.industria.core.util.commands;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Rotation;

public class RotationArgument implements ArgumentType<Rotation> {
	
	public static final Collection<String> EXAMPLES = Stream.of("-270", "-180", "-90", "0", "90", "180", "270").toList();
	public static final DynamicCommandExceptionType ERROR_INVALID_ROTATION = new DynamicCommandExceptionType((object) -> {
		return Component.translatable("industriacore.argument.rotation.invalid", object);
	});
	
	public static RotationArgument rotation() {
		return new RotationArgument();
	}
	
	public static Rotation getRotation(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, Rotation.class);
	}
	
	@Override
	public Rotation parse(StringReader reader) throws CommandSyntaxException {
		int angle = reader.readInt();
		switch (angle) {
		case -270: return Rotation.CLOCKWISE_90;
		case -180: return Rotation.CLOCKWISE_180;
		case -90: return Rotation.COUNTERCLOCKWISE_90;
		case 0: return Rotation.NONE;
		case 90: return Rotation.CLOCKWISE_90;
		case 180: return Rotation.CLOCKWISE_180;
		case 270: return Rotation.COUNTERCLOCKWISE_90;
		default: throw ERROR_INVALID_ROTATION.create(angle);
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		EXAMPLES.forEach(builder::suggest);
		return builder.buildFuture();
	}
	
	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
	
}
