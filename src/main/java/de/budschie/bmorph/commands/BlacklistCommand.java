package de.budschie.bmorph.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import de.budschie.bmorph.capabilities.blacklist.BlacklistData;
import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class BlacklistCommand
{
	public static void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("morphblacklist")
				.requires(src -> src.hasPermissionLevel(2))
				.then(Commands.literal("add").then(Commands.argument("entity", EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(BlacklistCommand::addBlacklist)))
				.then(Commands.literal("remove").then(Commands.argument("entity", ResourceLocationArgument.resourceLocation()).suggests(RemovableSuggestionProvider.INSTANCE).executes(BlacklistCommand::removeBlacklist)))
				.then(Commands.literal("list").executes(BlacklistCommand::listBlacklist)));
	}
	
	private static int addBlacklist(CommandContext<CommandSource> ctx)
	{
		BlacklistData blacklist = ConfigManager.INSTANCE.get(BlacklistData.class);
		
		ResourceLocation arg = ctx.getArgument("entity", ResourceLocation.class);
		
		if(blacklist.isInBlacklist(arg))
		{
			ctx.getSource().sendErrorMessage(new StringTextComponent("Failed to add entry " + arg.toString() + " to the entity blacklist because that entry already exists."));
		}
		else
		{
			blacklist.addBlacklist(arg);
			
			ctx.getSource().sendFeedback(new StringTextComponent(TextFormatting.GREEN + "Successfully added " + arg.toString() + " to the blacklist!"), true);
			
			blacklist.writeToFile();
		}
		
		return 0;
	}
	
	private static int listBlacklist(CommandContext<CommandSource> ctx)
	{
		BlacklistData blacklist = ConfigManager.INSTANCE.get(BlacklistData.class);
		
		String conc = String.join(", ", blacklist.getBlacklist().stream().map(rs -> rs.toString()).toArray(size -> new String[size]));
		
		StringBuilder builder = new StringBuilder();
		builder.append(TextFormatting.GREEN);
		
		boolean isSingular = blacklist.getBlacklist().size() == 1;
		
		builder.append("There ").append(isSingular ? "is" : "are").append(" currently ").append(blacklist.getBlacklist().size()).append(isSingular ? " entry" : " entries").append(" in the blacklist: ").append(conc);
		
		ctx.getSource().sendFeedback(new StringTextComponent(builder.toString()), false);
		
		return 0;
	}
	
	private static int removeBlacklist(CommandContext<CommandSource> ctx)
	{
		ResourceLocation arg = ctx.getArgument("entity", ResourceLocation.class);
		
		BlacklistData blacklist = ConfigManager.INSTANCE.get(BlacklistData.class);
		
		if(blacklist.isInBlacklist(arg))
		{
			blacklist.removeBlacklist(arg);
			ctx.getSource().sendFeedback(new StringTextComponent(TextFormatting.GREEN + "Successfully removed " + arg.toString() + " from the entity blacklist."), false);
			blacklist.writeToFile();
		}
		else
		{
			ctx.getSource().sendErrorMessage(new StringTextComponent("Failed to remove entry " + arg.toString() + " from the entity blacklist because that entry doesn't exist yet."));
		}
		
		return 0;
	}
}
