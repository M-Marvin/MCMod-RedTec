package de.m_marvin.industria.core.contraptions.engine.commands.arguments.contraption;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import de.m_marvin.industria.core.contraptions.engine.types.contraption.ServerContraption;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class ContraptionArgument implements ArgumentType<ContraptionSelector> {
	private static final Collection<String> EXAMPLES = Arrays.asList("Contraption_Name", "{42}", "@n", "@n[mass=1000..]");
	public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_CONTRAPTION = new SimpleCommandExceptionType(Component.translatable("industriacore.argument.contraption.toomany"));
	public static final SimpleCommandExceptionType NO_CONTRAPTIONS_FOUND = new SimpleCommandExceptionType(Component.translatable("industriacore.argument.contraption.notfound.entity"));
	public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(Component.translatable("industriacore.argument.contraption.selector.not_allowed"));
	final boolean single;
	
	protected ContraptionArgument(boolean pSingle) {
		this.single = pSingle;
	}

	public static ContraptionArgument contraption() {
		return new ContraptionArgument(true);
	}

	public static ContraptionArgument contraptions() {
		return new ContraptionArgument(false);
	}

	public static ServerContraption getContraption(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
		return pContext.getArgument(pName, ContraptionSelector.class).findSingleContraption(pContext.getSource());
	}

	public static Collection<ServerContraption> getContraptions(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
		Collection<ServerContraption> collection = getOptionalContraptions(pContext, pName);
		if (collection.isEmpty()) {
			throw NO_CONTRAPTIONS_FOUND.create();
		} else {
			return collection;
		}
	}

	public static Collection<ServerContraption> getOptionalContraptions(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
		return pContext.getArgument(pName, ContraptionSelector.class).findContraptions(pContext.getSource());
	}

	public ContraptionSelector parse(StringReader pReader) throws CommandSyntaxException {
		ContraptionSelectorParser entityselectorparser = new ContraptionSelectorParser(pReader);
		ContraptionSelector entityselector = entityselectorparser.parse();
		if (entityselector.getMaxResults() > 1 && this.single) {
			pReader.setCursor(0);
			throw ERROR_NOT_SINGLE_CONTRAPTION.createWithContext(pReader);
		} else {
			return entityselector;
		}
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
		S s = pContext.getSource();
		if (s instanceof SharedSuggestionProvider sharedsuggestionprovider) {
			StringReader stringreader = new StringReader(pBuilder.getInput());
			stringreader.setCursor(pBuilder.getStart());
			ContraptionSelectorParser entityselectorparser = new ContraptionSelectorParser(stringreader, net.minecraftforge.common.ForgeHooks.canUseEntitySelectors(sharedsuggestionprovider));

			try {
				entityselectorparser.parse();
			} catch (CommandSyntaxException commandsyntaxexception) {
			}

			return entityselectorparser.fillSuggestions(pBuilder, (builder) -> {
				
				if (s instanceof ClientSuggestionProvider clientprovider) {
					ContraptionArgumentClientOnly.suggestRaycastedContraption(builder, clientprovider);
				}
				
			});
		} else {
			return Suggestions.empty();
		}
	}

	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class Info implements ArgumentTypeInfo<ContraptionArgument, ContraptionArgument.Info.Template> {

		public void serializeToNetwork(ContraptionArgument.Info.Template pTemplate, FriendlyByteBuf pBuffer) {
			int i = 0;
			if (pTemplate.single) {
				i |= 1;
			}
			pBuffer.writeByte(i);
		}

		public ContraptionArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
			byte b0 = pBuffer.readByte();
			return new ContraptionArgument.Info.Template((b0 & 1) != 0);
		}

		public void serializeToJson(ContraptionArgument.Info.Template pTemplate, JsonObject pJson) {
			pJson.addProperty("amount", pTemplate.single ? "single" : "multiple");
		}

		public ContraptionArgument.Info.Template unpack(ContraptionArgument pArgument) {
			return new ContraptionArgument.Info.Template(pArgument.single);
		}

		public final class Template implements ArgumentTypeInfo.Template<ContraptionArgument> {
			final boolean single;

			Template(boolean pSingle) {
				this.single = pSingle;
			}

			public ContraptionArgument instantiate(CommandBuildContext pContext) {
				return new ContraptionArgument(this.single);
			}

			public ArgumentTypeInfo<ContraptionArgument, ?> type() {
				return Info.this;
			}
		}
	}
}
