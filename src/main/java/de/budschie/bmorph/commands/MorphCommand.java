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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphCommand
{
	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("morph")
				.requires(sender -> sender.hasPermissionLevel(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
							Commands.argument("entity", EntitySummonArgument.entitySummon())
							.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
							.executes(ctx -> 
							{
								return createEntityMorph(ctx.getArgument("player", EntitySelector.class).selectPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), new CompoundNBT());
							})
							.then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
							.executes(ctx -> 
							{
								return createEntityMorph(ctx.getArgument("player", EntitySelector.class).selectPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundNBT.class));
							})))
						));
		
		dispatcher.register(Commands.literal("morphplayer")
				.requires(sender -> sender.hasPermissionLevel(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
						Commands.argument("playername", StringArgumentType.word())
						.executes(ctx -> 
						{
							List<ServerPlayerEntity> players = ctx.getArgument("player", EntitySelector.class).selectPlayers(ctx.getSource());
							
							for(ServerPlayerEntity player : players)
								MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.PLAYER.createMorph(EntityType.PLAYER, ServerSetup.server.getPlayerProfileCache().getGameProfileForUsername(ctx.getArgument("playername", String.class)))), Optional.empty(), player);
							
							return 0;
						}))));
		
		dispatcher.register(Commands.literal("demorph")
				.requires(sender -> sender.hasPermissionLevel(2))
				.executes(ctx ->
				{
					ServerPlayerEntity player = ctx.getSource().asPlayer();
					
					MorphUtil.morphToServer(Optional.empty(), Optional.empty(), player);
					
					return 0;
				})
				.then(Commands.argument("player", EntityArgument.players()).executes(ctx ->
				{
					List<ServerPlayerEntity> players = ctx.getArgument("player", EntitySelector.class).selectPlayers(ctx.getSource());
					
					for(ServerPlayerEntity player : players)
						MorphUtil.morphToServer(Optional.empty(), Optional.empty(), player);
					
					return 0;
				})));
		
		dispatcher.register(Commands.literal("addmorph")
				.requires(sender -> sender.hasPermissionLevel(2))
				.then(
						Commands.argument("player", EntityArgument.players()).then(						
									Commands.argument("entity", EntitySummonArgument.entitySummon())
									.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
									.executes(ctx -> 
									{
										return addMorph(ctx.getArgument("player", EntitySelector.class).selectPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), new CompoundNBT());
									})
									.then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
									.executes(ctx -> 
									{
										return addMorph(ctx.getArgument("player", EntitySelector.class).selectPlayers(ctx.getSource()), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundNBT.class));
									}))
								)
						));
		
		dispatcher.register(Commands.literal("morph_list_ability")
				.requires(sender -> sender.hasPermissionLevel(2))
				.executes(ctx -> listAbilities(ctx.getSource(), ctx.getSource().asPlayer()))
				.then(Commands.argument("player", EntityArgument.player()).executes(ctx -> listAbilities(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
	}
	
	private static int listAbilities(CommandSource sender, PlayerEntity player)
	{
		IMorphCapability cap = MorphUtil.getCapOrNull(player);
		
		if(cap == null || cap.getCurrentAbilities().size() <= 0)
		{
			sender.sendErrorMessage(new StringTextComponent("The given player is currently not morphed."));
		}
		else
		{
			StringBuilder builder = new StringBuilder(TextFormatting.GREEN + "The given player currently has following abilities: ").append(TextFormatting.WHITE);
			
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
			
			sender.sendFeedback(new StringTextComponent(builder.toString()), true);
		}
		
		return 0;
	}
	
	private static int addMorph(List<ServerPlayerEntity> entities, ResourceLocation rs, CompoundNBT nbtData)
	{
		MorphItem morphItemToAdd = MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true);
		
		for(ServerPlayerEntity entity : entities)
		{
			IMorphCapability capability = entity.getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve().get();
						
			if(capability.getMorphList().contains(morphItemToAdd))
				entity.sendMessage(new StringTextComponent(TextFormatting.RED + "You may not add a morph to your list that is already present."), new UUID(0, 0));
			else
			{
				entity.sendMessage(new StringTextComponent("Added " + rs.toString() + " with its NBT data to your morph list."), new UUID(0, 0));
				
				capability.addToMorphList(morphItemToAdd);
				capability.syncMorphAcquisition(entity, morphItemToAdd);
			}
		}
		
		return 0;
	}
	
	private static int createEntityMorph(List<ServerPlayerEntity> entities, ResourceLocation rs, CompoundNBT nbtData)
	{
		for(ServerPlayerEntity entity : entities)
		{
			if(rs.toString().equals("bmorph:morph_entity"))
				throw new IllegalArgumentException("You may not morph yourself into the morph entity.");
			
			nbtData.putString("id", rs.toString());
			
			MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null, true)), Optional.empty(), entity);
		}
		
		return 0;
	}
}
