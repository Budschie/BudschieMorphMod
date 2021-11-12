package de.budschie.bmorph.commands;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphCommand
{
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("morph")
				.requires(sender -> sender.hasPermission(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
							Commands.argument("entity", EntitySummonArgument.id())
							.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
							.executes(ctx -> 
							{
								return createEntityMorph(ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), new CompoundTag());
							})
							.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
							.executes(ctx -> 
							{
								return createEntityMorph(ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundTag.class));
							})))
						));
		
		dispatcher.register(Commands.literal("morphplayer")
				.requires(sender -> sender.hasPermission(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
						Commands.argument("playername", StringArgumentType.word())
						.executes(ctx -> 
						{
							List<ServerPlayer> players = ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource());
							
							for(ServerPlayer player : players)
								// TODO: Risky
								MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.PLAYER.createMorph(EntityType.PLAYER, ServerSetup.server.getProfileCache().get(ctx.getArgument("playername", String.class)).get())), Optional.empty(), player);
							
							return 0;
						}))));
		
		dispatcher.register(Commands.literal("demorph")
				.requires(sender -> sender.hasPermission(2))
				.executes(ctx ->
				{
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					
					MorphUtil.morphToServer(Optional.empty(), Optional.empty(), player);
					
					return 0;
				})
				.then(Commands.argument("player", EntityArgument.players()).executes(ctx ->
				{
					List<ServerPlayer> players = ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource());
					
					for(ServerPlayer player : players)
						MorphUtil.morphToServer(Optional.empty(), Optional.empty(), player);
					
					return 0;
				})));
		
		dispatcher.register(Commands.literal("addmorph")
				.requires(sender -> sender.hasPermission(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
									Commands.argument("entity", EntitySummonArgument.id())
									.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
									.executes(ctx -> 
									{
										return addMorph(ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), new CompoundTag());
									})
									.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
									.executes(ctx -> 
									{
										return addMorph(ctx.getArgument("player", EntitySelector.class).findPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundTag.class));
									}))
								)
						));
		
		dispatcher.register(Commands.literal("morph_list_ability")
				.requires(sender -> sender.hasPermission(2))
				.executes(ctx -> listAbilities(ctx.getSource(), ctx.getSource().getPlayerOrException()))
				.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> listAbilities(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}
	
	private static int listAbilities(CommandSourceStack sender, Player player)
	{
		IMorphCapability cap = MorphUtil.getCapOrNull(player);
		
		if(cap == null || !cap.getCurrentMorph().isPresent())
		{
			sender.sendFailure(new TextComponent("The given player is currently not morphed."));
		}
		else if(cap.getCurrentAbilities().size() <= 0)
		{
			sender.sendSuccess(new TextComponent(ChatFormatting.AQUA + "The given player has no abilities, but they are morphed."), true);
		}
		else
		{
			StringBuilder builder = new StringBuilder(ChatFormatting.GREEN + "The given player currently has following abilities: ").append(ChatFormatting.WHITE);
			
			for(int i = 0; i < cap.getCurrentAbilities().size(); i++)
			{
				Ability currentAbility = cap.getCurrentAbilities().get(i);
				
				builder.append(currentAbility.getResourceLocation().toString()).append('(').append(currentAbility.getConfigurableAbility().getRegistryName().toString()).append(')');
				
				if(i == (cap.getCurrentAbilities().size() - 2))
				{
					builder.append(" and ");
				}
				else if(i != (cap.getCurrentAbilities().size() - 1))
				{
					builder.append(", ");
				}
			}
			
			sender.sendSuccess(new TextComponent(builder.toString()), true);
		}
		
		return 0;
	}
	
	private static int addMorph(List<ServerPlayer> entities, ResourceLocation rs, CompoundTag nbtData)
	{
		MorphItem morphItemToAdd = MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true);
		
		for(ServerPlayer entity : entities)
		{
			IMorphCapability capability = entity.getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve().get();
						
			if(capability.getMorphList().contains(morphItemToAdd))
				entity.sendMessage(new TextComponent(ChatFormatting.RED + "You may not add a morph to your list that is already present."), new UUID(0, 0));
			else
			{
				entity.sendMessage(new TextComponent("Added " + rs.toString() + " with its NBT data to your morph list."), new UUID(0, 0));
				
				capability.addToMorphList(morphItemToAdd);
				capability.syncMorphAcquisition(entity, morphItemToAdd);
			}
		}
		
		return 0;
	}
	
	private static int createEntityMorph(List<ServerPlayer> entities, ResourceLocation rs, CompoundTag nbtData)
	{
		for(ServerPlayer entity : entities)
		{
			if(rs.toString().equals("bmorph:morph_entity"))
				throw new IllegalArgumentException("You may not morph yourself into the morph entity.");
			
			nbtData.putString("id", rs.toString());
			
			MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true)), Optional.empty(), entity);
		}
		
		return 0;
	}
}
