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
import net.minecraft.world.level.block.Mirror;

public class MirroringArgument implements ArgumentType<Mirror> {
	
	public static final Collection<String> EXAMPLES = Stream.of("none", "left_right", "front_back").toList();
	public static final DynamicCommandExceptionType ERROR_INVALID_MIRROR = new DynamicCommandExceptionType((object) -> {
		return Component.translatable("industriacore.argument.mirror.invalid", object);
	});
	
	public static MirroringArgument mirroring() {
		return new MirroringArgument();
	}

	public static Mirror getMirror(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, Mirror.class);
	}
	
	@Override
	public Mirror parse(StringReader reader) throws CommandSyntaxException {
		String mirror = reader.readString();
		switch (mirror) {
		case "none": return Mirror.NONE;
		case "left_right": return Mirror.LEFT_RIGHT;
		case "front_back": return Mirror.FRONT_BACK;
		default: throw ERROR_INVALID_MIRROR.create(mirror);
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
