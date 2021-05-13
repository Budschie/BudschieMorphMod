package de.budschie.bmorph.commands;

import java.util.Optional;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphCommand
{
	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("morph")
				.requires(sender -> sender.hasPermissionLevel(2))
				.then(Commands.argument("entity", EntitySummonArgument.entitySummon())
						.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
						.executes(ctx -> 
						{
							return createEntityMorph(ctx.getSource().asPlayer(), ctx.getArgument("entity", ResourceLocation.class), new CompoundNBT());
						})
						.then(Commands.argument("nbt", NBTCompoundTagArgument.nbt())
						.executes(ctx -> 
						{
							return createEntityMorph(ctx.getSource().asPlayer(), ctx.getArgument("entity", ResourceLocation.class), ctx.getArgument("nbt", CompoundNBT.class));
						}))
						));
		
		dispatcher.register(Commands.literal("morphplayer")
				.requires(sender -> sender.hasPermissionLevel(2))
				.then(Commands.argument("playername", StringArgumentType.word())
						.executes(ctx -> 
						{
							ServerPlayerEntity player = ctx.getSource().asPlayer();
							
							MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.PLAYER.createMorph(EntityType.PLAYER, ServerSetup.server.getPlayerProfileCache().getGameProfileForUsername(ctx.getArgument("playername", String.class)))), Optional.empty(), player);
							
							return 0;
						})));
		
		dispatcher.register(Commands.literal("demorph")
				.requires(sender -> sender.hasPermissionLevel(2))
				.executes(ctx ->
				{
					ServerPlayerEntity player = ctx.getSource().asPlayer();
					
					MorphUtil.morphToServer(Optional.empty(), Optional.empty(), player);
					
					return 0;
				}));
	}
	
	private static int createEntityMorph(ServerPlayerEntity entity, ResourceLocation rs, CompoundNBT nbtData)
	{
		if(rs.toString().equals("bmorph:morph_entity"))
			throw new IllegalArgumentException("You may not morph yourself into the morph entity.");
		
		nbtData.putString("id", rs.toString());
		
		MorphUtil.morphToServer(Optional.of(MorphManagerHandlers.FALLBACK.createMorph(ForgeRegistries.ENTITIES.getValue(rs), nbtData, null)), Optional.empty(), entity);
		
		return 0;
	}
}
