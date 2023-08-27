package de.budschie.bmorph.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import de.budschie.bmorph.capabilities.blacklist.BlacklistData;
import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;

public class BlacklistCommand
{
	public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
	{
		dispatcher.register(Commands.literal("morphblacklist")
				.requires(src -> src.hasPermission(2))
				.then(Commands.literal("add").then(Commands.argument("entity", ResourceArgument.resource(context, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(BlacklistCommand::addBlacklist)))
				.then(Commands.literal("remove").then(Commands.argument("entity", ResourceLocationArgument.id()).suggests(RemovableSuggestionProvider.INSTANCE).executes(BlacklistCommand::removeBlacklist)))
				.then(Commands.literal("list").executes(BlacklistCommand::listBlacklist)));
	}
	
	private static int addBlacklist(CommandContext<CommandSourceStack> ctx)
	{
		BlacklistData blacklist = ConfigManager.INSTANCE.get(BlacklistData.class);
		
		ResourceLocation arg = ctx.getArgument("entity", ResourceLocation.class);
		
		if(blacklist.isInBlacklist(arg))
		{
			ctx.getSource().sendFailure(MutableComponent.create(new LiteralContents("Failed to add entry " + arg.toString() + " to the entity blacklist because that entry already exists.")));
		}
		else
		{
			blacklist.addBlacklist(arg);
			
			ctx.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Successfully added " + arg.toString() + " to the blacklist!")).withStyle(ChatFormatting.GREEN), true);
			
			blacklist.writeToFile(ctx.getSource().getServer());
		}
		
		return 0;
	}
	
	private static int listBlacklist(CommandContext<CommandSourceStack> ctx)
	{
		BlacklistData blacklist = ConfigManager.INSTANCE.get(BlacklistData.class);
		
		String conc = String.join(", ", blacklist.getBlacklist().stream().map(rs -> rs.toString()).toArray(size -> new String[size]));
		
		StringBuilder builder = new StringBuilder();
		builder.append(ChatFormatting.GREEN);
		
		boolean isSingular = blacklist.getBlacklist().size() == 1;
		
		builder.append("There ").append(isSingular ? "is" : "are").append(" currently ").append(blacklist.getBlacklist().size()).append(isSingular ? " entry" : " entries").append(" in the blacklist: ").append(conc);
		
		ctx.getSource().sendSuccess(MutableComponent.create(new LiteralContents(builder.toString())), false);
		
		return 0;
	}
	
	private static int removeBlacklist(CommandContext<CommandSourceStack> ctx)
	{
		ResourceLocation arg = ctx.getArgument("entity", ResourceLocation.class);
		
		BlacklistData blacklist = ConfigManager.INSTANCE.get(BlacklistData.class);
		
		if(blacklist.isInBlacklist(arg))
		{
			blacklist.removeBlacklist(arg);
			ctx.getSource().sendSuccess(MutableComponent.create(new LiteralContents("Successfully removed " + arg.toString() + " from the entity blacklist.")).withStyle(ChatFormatting.GREEN), false);
			blacklist.writeToFile(ctx.getSource().getServer());
		}
		else
		{
			ctx.getSource().sendFailure(MutableComponent.create(new LiteralContents("Failed to remove entry " + arg.toString() + " from the entity blacklist because that entry doesn't exist yet.")));
		}
		
		return 0;
	}
}
